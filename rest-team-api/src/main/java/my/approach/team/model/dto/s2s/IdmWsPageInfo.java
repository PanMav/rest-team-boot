package my.approach.team.model.dto.s2s;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IdmWsPageInfo {
    private Long offset;
    private Integer limit;
}
