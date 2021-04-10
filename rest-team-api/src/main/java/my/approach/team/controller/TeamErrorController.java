package my.approach.team.controller;

import my.approach.team.model.dto.ApiError;
import my.approach.team.model.dto.ApiErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static my.approach.team.error.ApiErrorCode.*;

@Controller
public class TeamingErrorController implements ErrorController {
    private static final String ERROR_PATH = "/error";

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    @ApiIgnore
    public ResponseEntity<ApiErrorResponse> handleError(HttpServletRequest request) {
        final Exception exception = ((Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
        final HttpStatus status = HttpStatus.valueOf((int) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));

        switch (status) {
            case NOT_FOUND: {
                final Object errorRequestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
                return ResponseEntity
                        .status(status)
                        .body(ApiErrorResponse.builder()
                                .status(status.value())
                                .errors(Collections.singletonList(ApiError.builder()
                                        .code(SGM_E_UNMAPPED_PATH.name())
                                        .message(String.format("URL: \"%s\" is not mapped", errorRequestUri))
                                        .build()))
                                .build());
            }

            case INTERNAL_SERVER_ERROR: {
                final Object errorRequestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
                return ResponseEntity
                        .status(status)
                        .body(ApiErrorResponse.builder()
                                .status(status.value())
                                .errors(Collections.singletonList(ApiError.builder()
                                        .code(SGM_E_GENERIC_ERROR.name())
                                        .message(String.format(exception.getMessage() + ", while requesting from URL: %s", errorRequestUri))
                                        .build()))
                                .build());
            }

            default: {
                // @TODO :: This may not be appropriate as it will expose the native exception to the client
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiErrorResponse.builder()
                                .status(status.value())
                                .errors(Collections.singletonList(ApiError.builder()
                                        .code(SGM_E_UNKNOWN_ERROR.name())
                                        .message(exception != null ? exception.getLocalizedMessage() : null)
                                        .build()))
                                .build());
            }
        }
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
