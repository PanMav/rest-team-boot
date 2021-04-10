package my.approach.team.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
public class Pagination {
    @NonNull
    @Positive(message = "Parameter `page` must be a positive integer")
    private Integer page = 1;

    @NonNull
    @Positive(message = "Parameter `size` must be a positive integer")
    @Max(value = 50, message = "Parameter `size` cannot be greater than 50")
    private Integer size = 10;

    private String sort;
}
