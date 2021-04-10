package my.approach.team.model.dto.s2s;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class IdmWsApiPageResponse<T> {
    private List<T> elements = new ArrayList<>();
    private IdmWsPageInfo pageInfo;
    private Long totalSize;
}
