package my.approach.team.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.serialization.Views;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ListApiResponse<T> {
    @JsonView(Views.DefaultApiResponse.class)
    private List<T> data;

    @JsonView(Views.DefaultApiResponse.class)
    private ApiResponseMetadata metadata;
}
