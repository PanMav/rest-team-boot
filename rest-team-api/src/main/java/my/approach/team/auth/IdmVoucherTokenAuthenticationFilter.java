package my.approach.team.auth;

import my.approach.team.idm.nx.sso.sp.opensaml.domain.SSOUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
public class IdmVoucherTokenAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private final String voucherTokenAttr;

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        SSOUserInfo userInfo;
        if ((userInfo = ((SSOUserInfo) request.getAttribute(SamlUtils.MOFT_IDM_SSO_USERINFO))) != null) {
            return userInfo.getUsername();
        }

        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        SSOUserInfo userInfo;
        if ((userInfo = ((SSOUserInfo) request.getAttribute(SamlUtils.MOFT_IDM_SSO_USERINFO))) != null) {
            return new VoucherTokenCredentials(userInfo.getAttributes().get(voucherTokenAttr).iterator().next());
        }

        return null;
    }
}
