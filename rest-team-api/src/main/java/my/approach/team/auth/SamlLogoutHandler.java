package my.approach.team.auth;

import my.approach.team.idm.nx.sso.sp.opensaml.saml.SamlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class SamlLogoutHandler implements LogoutHandler {
    private final SamlConfigurationProperties samlConfig;
    private final SamlHelper samlHelper;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String logoutRequest = samlHelper.createSamlLogoutRequest(
                    samlConfig.getIdpUrl(),
                    SamlUtils.getIssuer(request),
                    UUID.randomUUID().toString(),
                    authentication.getName()
            );

            log.debug("samlLogoutRequest: {}", logoutRequest);
            String base64EncodedRequest = Base64.getEncoder().encodeToString(logoutRequest.getBytes(StandardCharsets.UTF_8));

            // RelayState should be echoed back
            String relayState = request.getRequestURL().toString();
            log.debug("relayState: {}", relayState);

            String samlRequestFormMarkup = samlHelper.getSamlForm("POST", samlConfig.getIdpUrl(), base64EncodedRequest, null, relayState);
            response.setContentType("text/html");
            response.getWriter().append(samlRequestFormMarkup).flush();
        } catch (Exception e) {
            log.error("Error during Sso Global Request:", e);
        }
    }
}
