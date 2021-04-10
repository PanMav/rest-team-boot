package my.approach.team.auth;

import my.approach.team.idm.nx.s2s.client.S2STokenClient;
import my.approach.team.idm.nx.token.client.commons.JsonWebToken;
import my.approach.team.idm.nx.token.client.commons.TokenClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationProvider extends PreAuthenticatedAuthenticationProvider {
    private final S2STokenClient s2STokenClient;
    private final UserDetailsService userDetailsService;

    /**
     * This will be called by the {@link IdmVoucherTokenAuthenticationFilter} for authentication.
     * At this point the user is already authenticated against the IDP server so we just call {@link Authentication#setAuthenticated(boolean)}
     * to make sure the rest of the security chain handles the user as authenticated.
     *
     * @param authentication The current authentication status.
     * @return The populated {@link Authentication} object.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken result = ((PreAuthenticatedAuthenticationToken) authentication);
        if (authentication.getCredentials() instanceof VoucherTokenCredentials) {
            result.setAuthenticated(true);
        } else if (authentication.getCredentials() instanceof AccessTokenCredentials) {
            JsonWebToken accessToken = ((AccessTokenCredentials) authentication.getCredentials()).getAccessToken();
            try {
                boolean validToken = s2STokenClient.validate(accessToken);
                if (!validToken) {
                    log.warn("Invalid access token: {}", accessToken);
                    throw new BadCredentialsException(String.format("Invalid access token: %s", accessToken.getRepresentation()));
                } else {
                    Collection<? extends GrantedAuthority> grantedAuthorities = userDetailsService.loadUserByUsername(authentication.getName()).getAuthorities();
                    result = new PreAuthenticatedAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), grantedAuthorities);
                    result.setAuthenticated(true);
                }
            } catch (TokenClientException tce) {
                log.warn("Error validating token: {} - Got response status: {}", accessToken, tce.getResponseStatus());
                throw new BadCredentialsException(String.format("Could not validate token: %s", accessToken));
            }
        }

        return result;
    }


}
