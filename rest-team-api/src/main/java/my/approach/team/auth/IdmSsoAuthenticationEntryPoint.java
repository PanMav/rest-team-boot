package my.approach.team.auth;

import my.approach.team.idm.nx.sso.sp.opensaml.saml.SamlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Used to commence IDM-IDP SSO authentication flow.
 * Redirects user to SSO login page by injecting an appropriate html form in the response.
 * This is essentially a spring-compliant implementation for
 * {@link my.approach.team.idm.nx.sso.sp.opensaml.filter.SamlAuthFilter#handleSsoLoginRequest}
 *
 * <em>Must make sure to exclude all static resources from spring security flow e.g:</em>
 * <pre>
 * {@code
 * @Override
 * public void configure(WebSecurity web) throws Exception {
 *     web.ignoring().antMatchers("/*.js", "/*.css", "/*.ico");
 * }
 * }
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class IdmSsoAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final SamlConfigurationProperties samlConfig;
    private final SamlHelper samlHelper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws ServletException {
        String issuer = SamlUtils.getIssuer(request);
//        String responseLocation = getAssertionConsumerServiceURL(request);
        String responseLocation = request.getRequestURL().toString();
        String authnContextClass = SamlUtils.getAuthnContextClass(request);
        try {
            String samlRequest = samlHelper.createSamlLoginRequest(
                    samlConfig.getIdpUrl(),
                    responseLocation,
                    issuer,
                    authnContextClass);
            log.debug("samlRequest: {}", samlRequest);
            String base64EncodedRequest = Base64.getEncoder().encodeToString(samlRequest.getBytes(StandardCharsets.UTF_8));
            log.debug("base64EncodedRequest: {}", base64EncodedRequest);

            // Construct the relay state url, so that the user is redirected to the  

            String relayStateUrl = SamlUtils.getRelayStateUrlAfterLogin(request);

            String samlRequestFormMarkup = samlHelper.getSamlForm(HttpMethod.POST.name(), samlConfig.getIdpUrl(), base64EncodedRequest, null, relayStateUrl);
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.getWriter().append(samlRequestFormMarkup).flush();
        } catch (Exception e) {
            log.error("Error during Sso Login Request:", e);
            throw new ServletException(e);
        }
    }
}
