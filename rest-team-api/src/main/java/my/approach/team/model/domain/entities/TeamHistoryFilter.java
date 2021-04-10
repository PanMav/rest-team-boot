package my.approach.team.model.domain.entities;

import my.approach.team.util.Util;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//This class is intended to be used for filtering team history objects (history-teams RestAPI)
public class TeamHistoryFilter {

    @ApiModelProperty(example = "GRID-00045")
    private String teamId;

    @ApiModelProperty(example = "Can be CREATE, UPDATE, SEARCH or left blank")
    private String action;

    @ApiModelProperty(example = "GMT, GGT, REGULATED or left blank")
    private String origin;

    private String user;

    @ApiModelProperty(example = "e.g. 2020-01-20")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date date;

    @ApiModelProperty(hidden = true)
    private LocalDateTime startDate;

    @ApiModelProperty(hidden = true)
    private LocalDateTime endDate;

    @ApiModelProperty(hidden = true)
    public boolean isFilterEmpty() {
        return Util.isAllNull
                (teamId, action, origin, user, date);
    }

    @ApiModelProperty(hidden = true)
    public LocalDateTime calculateDateFrom() {
        if (date != null) {
            return startDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0);
        }
        return null;
    }

    @ApiModelProperty(hidden = true)
    public LocalDateTime calculateDateTo() {
        if (date != null) {
            return endDate = date.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59);
        }
        return null;
    }


}
