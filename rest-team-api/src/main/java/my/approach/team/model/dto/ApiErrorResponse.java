package my.approach.team.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ApiErrorResponse {
    private int status;

    @Builder.Default
    private List<ApiError> errors = new ArrayList<>();
}
