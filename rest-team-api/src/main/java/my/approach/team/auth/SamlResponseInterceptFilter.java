package my.approach.team.auth;

import my.approach.team.idm.nx.sso.sp.opensaml.domain.ImmutableSSOUserInfo;
import my.approach.team.idm.nx.sso.sp.opensaml.domain.SSOUserInfo;
import my.approach.team.idm.nx.sso.sp.opensaml.saml.SamlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.*;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static my.approach.team.auth.SamlUtils.MOFT_IDM_SSO_USERINFO;
import static my.approach.team.auth.SamlUtils.RELAY_STATE;
import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
public class SamlResponseInterceptFilter extends GenericFilterBean {
    private final SamlHelper samlHelper;
    private final SamlConfigurationProperties samlConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = ((HttpServletRequest) request);
        HttpServletResponse res = ((HttpServletResponse) response);
        boolean continueFilterChain = true;

        if (RequestMethod.POST.name().equals(req.getMethod())) {
            String samlRequest = req.getParameter("SAMLRequest");
            String samlResponse = req.getParameter("SAMLResponse");

            if (samlRequest != null) {
                String decodedSamlRequest = new String(Base64.getDecoder().decode(samlRequest));
                XMLObject requestXmlObject;
                try {
                    requestXmlObject = samlHelper.unmarshallSamlMessage(decodedSamlRequest);
                } catch (Exception e) {
                    throw new ServletException(e);
                }
                if (requestXmlObject instanceof LogoutRequest) {
                    continueFilterChain = false;
                    handleSloSamlRequest(((LogoutRequest) requestXmlObject), req, res);
                }
            }

            if (samlResponse != null) {
                String decodedSamlResponse = new String(Base64.getDecoder().decode(samlResponse));
                XMLObject responseXmlObject;
                try {
                    responseXmlObject = samlHelper.unmarshallSamlMessage(decodedSamlResponse);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                if (responseXmlObject instanceof LogoutResponse) {
                    continueFilterChain = false;
                    handleLogoutResponse(((LogoutResponse) responseXmlObject), req, res);
                } else {
                    Response samlResp = (Response) responseXmlObject;

                    if (!samlHelper.validateSignedSamlMessage(samlResp)) {
                        throw new ServletException("Invalid signature");
                    }

                    // Check if login is successful
                    if (!StatusCode.SUCCESS.equals(samlResp.getStatus().getStatusCode().getValue())) {
                        throw new ServletException("Login unsuccessful. IDP returned status " + samlResp.getStatus().getStatusCode().getValue());
                    }

                    Assertion assertion = samlHelper.extractAssertion(samlResp);

                    // @TODO :: Parameterize skew
                    if (!samlHelper.verifyAssertionConditions(assertion, SamlUtils.getIssuer(req), 5000L)) {
                        throw new ServletException("Invalid assertion conditions");
                    }

                    String username = assertion.getSubject().getNameID().getValue();
                    Map<String, List<String>> attributesMap = samlHelper.getAttributesMap(assertion);
                    Set<String> authnContexts = new HashSet<>();
                    authnContexts.add(samlHelper.getAuthnContextClassRef(assertion));

                    // Set user info into session
                    SSOUserInfo userInfo = ImmutableSSOUserInfo.builder()
                            .username(username)
                            .roles(attributesMap.get("Role"))
                            .attributes(attributesMap)
                            .addAllAuthnContexts(authnContexts)
                            .build();

                    log.trace("Created {}", userInfo);
                    MDC.put("username", username);
                    log.debug("SAML login");
                    request.setAttribute(MOFT_IDM_SSO_USERINFO, userInfo);
                }
            }
        }

        if (continueFilterChain) {
            chain.doFilter(request, response);
        }
    }

    private void handleLogoutResponse(LogoutResponse samlLogoutResp, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            if (!samlHelper.validateSignedSamlMessage(samlLogoutResp)) {
                throw new Exception("Invalid signature");
            }

            log.debug("Logging out");
            request.getSession().invalidate();
            postLogoutRedirect(request, response);
        } catch (Exception e) {
            log.error("Error during Slo Saml response:", e);
            throw new ServletException(e);
        }
    }

    private void postLogoutRedirect(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        URI issuer = URI.create(SamlUtils.getIssuer(request));
        String redirectTo = Optional.ofNullable(samlConfig.getSloRedirectPath())
                .map(redirect -> URI.create(redirect).isAbsolute() ? redirect : redirect.startsWith("/") ? redirect : issuer.resolve(redirect).toString())
                .orElse(issuer.toString());
        try {
            log.debug("Redirecting to {}", redirectTo);
            response.sendRedirect(redirectTo);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handleSloSamlRequest(LogoutRequest samlReq, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            if (!samlHelper.validateSignedSamlMessage(samlReq)) {
                throw new Exception("Invalid signature");
            }

            String samlReqId = samlReq.getID();

            String logoutResponse = samlHelper.createSamlLogoutResponse(
                    samlConfig.getIdpUrl(),
                    samlReqId, SamlUtils.getIssuer(request),
                    true,
                    null);

            String base64EncodedResponse = Base64.getEncoder().encodeToString(logoutResponse.getBytes(StandardCharsets.UTF_8));

            // RelayState should be echoed back
            String relayState = request.getParameter(RELAY_STATE);
            log.debug("relayState: {}", relayState);

            String samlResponseFormMarkup = samlHelper.getSamlForm("POST", samlConfig.getIdpUrl(), null, base64EncodedResponse, relayState);
            response.setContentType("text/html");
            response.getWriter().append(samlResponseFormMarkup).flush();

            ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);

        } catch (Exception e) {
            log.error("Error during Slo Saml Request:", e);
            throw new ServletException(e);
        }
    }
}
