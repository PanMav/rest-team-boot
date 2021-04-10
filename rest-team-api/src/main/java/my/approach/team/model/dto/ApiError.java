package my.approach.team.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import my.approach.team.util.Util;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ApiError {
    private String code;
    private String message;

    @Builder.Default
    private String extraInfo = null;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = Util.getTimestamp();
}
