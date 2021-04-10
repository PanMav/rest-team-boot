package my.approach.team.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.serialization.Views;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponseMetadata {
    @JsonView(Views.DefaultApiResponse.class)
    private Integer fetchedItems;

    @JsonView(Views.DefaultApiResponse.class)
    private Long totalItems;

    @JsonView(Views.DefaultApiResponse.class)
    private Integer nextPage;

    @JsonView(Views.DefaultApiResponse.class)
    private Integer totalPages;

    public Integer getFetchedItems() {
        return fetchedItems;
    }

    public void setFetchedItems(Integer fetchedItems) {
        this.fetchedItems = fetchedItems;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
