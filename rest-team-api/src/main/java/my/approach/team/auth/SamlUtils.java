package my.approach.team.auth;

import my.approach.team.idm.nx.sso.sp.opensaml.domain.SSOUserInfo;
import my.approach.team.idm.nx.sso.sp.opensaml.util.URLHelper;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.saml.saml2.core.AuthnContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Slf4j
public class SamlUtils {
    private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
    private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    public static final String AUTHN_CONTEXT_CLASS_REF_OVERRIDE = "AuthnContextClassRefOverride";
    public static final String MOFT_IDM_SSO_USERINFO = "team.idm.sso.userinfo";
    public static final String RELAY_STATE = "RelayState";

    public static String getIssuer(HttpServletRequest req) {
        String scheme = getIssuerScheme(req);
        String serverName = getIssuerServerName(req);
        int serverPort = getIssuerServerPort(req);
        String contextPath = req.getContextPath();

        if (serverPort == 80 || serverPort == 443) {
            return String.format("%s://%s%s/", scheme, serverName, contextPath);
        }

        return String.format("%s://%s:%d%s/", scheme, serverName, serverPort, contextPath);
    }

    public static String getIssuerScheme(HttpServletRequest req) {
        String scheme = ofNullable(req.getHeader(X_FORWARDED_PROTO)).orElse(req.getScheme());
        log.debug("Issuer scheme {}", scheme);
        return scheme;
    }

    public static String getIssuerServerName(HttpServletRequest req) {
        String serverName = ofNullable(req.getHeader(X_FORWARDED_HOST)).orElse(req.getServerName());
        log.debug("Issuer serverName {}", serverName);
        return serverName;
    }

    public static int getIssuerServerPort(HttpServletRequest req) {
        int serverPort = ofNullable(req.getHeader(X_FORWARDED_PORT)).map(Integer::valueOf).orElse(req.getServerPort());
        log.debug("Issuer serverPort {}", serverPort);
        return serverPort;
    }

    public static Optional<SSOUserInfo> getUserInfo(HttpServletRequest req) {
        return ofNullable((SSOUserInfo) req.getAttribute(MOFT_IDM_SSO_USERINFO));
    }

    public static String getAuthnContextClass(HttpServletRequest request) {
        return Optional.ofNullable((String) request.getAttribute(AUTHN_CONTEXT_CLASS_REF_OVERRIDE))
                .orElse(AuthnContext.PPT_AUTHN_CTX);
    }

    public static String getRelayStateUrlAfterLogin(HttpServletRequest httpReq) {
        return URLHelper.getRequestFullUrl(httpReq);
    }
}
