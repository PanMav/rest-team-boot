package my.approach.team.auth;

import my.approach.team.idm.nx.token.client.commons.JsonWebToken;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdmAccessTokenAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?:Token +)(.+)");

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        JsonWebToken accessToken;
        if ((accessToken = getTokenFromHeader(request)) != null) {
            return accessToken.getUser();
        }

        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        JsonWebToken accessToken;
        if ((accessToken = getTokenFromHeader(request)) != null) {
            return new AccessTokenCredentials(accessToken);
        }

        return null;
    }

    private JsonWebToken getTokenFromHeader(HttpServletRequest request) {
        String authHeader;
        if ((authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)) != null) {
            Matcher tokenMatcher = TOKEN_PATTERN.matcher(authHeader);
            if (tokenMatcher.find()) {
                return JsonWebToken.valueOf(tokenMatcher.team(1));
            }
        }

        return null;
    }
}
