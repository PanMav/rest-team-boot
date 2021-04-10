package my.approach.team.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.serialization.Views;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SingleApiResponse<T> {
    @JsonView(Views.DefaultApiResponse.class)
    private T data;

    @JsonView(Views.DefaultApiResponse.class)
    private ApiResponseMetadata metadata;
}
