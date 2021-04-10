package my.approach.team.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.approach.team.model.dto.ApiError;
import my.approach.team.model.dto.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.approach.team.error.ApiErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.debug("Access token auth exception", authException);
        int status = HttpStatus.UNAUTHORIZED.value();
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(status)
                .errors(Collections.singletonList(ApiError.builder()
                        .code(ApiErrorCode.SGM_E_INVALID_TOKEN.name())
                        .message("Invalid token")
                        .build()))
                .build();
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
