package my.approach.team.util;

import my.approach.team.model.dto.ApiResponseMetadata;
import my.approach.team.model.dto.ListApiResponse;
import my.approach.team.model.dto.SingleApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

public class Util {
    public static <T> ListApiResponse<T> toListApiResponse(Page<T> page) {
        final ListApiResponse<T> response = new ListApiResponse<T>();
        final ApiResponseMetadata metadata = new ApiResponseMetadata();
        metadata.setTotalItems(page.getTotalElements());
        metadata.setFetchedItems(page.getContent().size());
        metadata.setTotalPages(page.getTotalPages());
        metadata.setNextPage(page.hasNext() ? page.nextPageable().getPageNumber() + 1 : null);
        response.setData(page.getContent());
        response.setMetadata(metadata);

        return response;
    }

    public static <T> ListApiResponse<T> toListApiResponse(List<T> data) {
        final ListApiResponse<T> response = new ListApiResponse<T>();
        final ApiResponseMetadata metadata = new ApiResponseMetadata();
        metadata.setTotalPages(1);
        metadata.setTotalItems((long) data.size());
        metadata.setNextPage(null);
        metadata.setFetchedItems(data.size());
        response.setData(data);
        response.setMetadata(metadata);

        return response;
    }

    public static <T> SingleApiResponse<T> toSingleApiResponse(T data) {
        final SingleApiResponse<T> response = new SingleApiResponse<T>();
        final ApiResponseMetadata metadata = new ApiResponseMetadata();
        metadata.setFetchedItems(1);
        metadata.setTotalItems(1L);
        response.setData(data);
        response.setMetadata(metadata);

        return response;
    }

    public static LocalDateTime getTimestamp() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * Concatenates and hashes the strings given
     */
    public static String hashValues(String... providedValue) {
        final String stringToHash = String.join(";", providedValue);
        return DigestUtils.md5DigestAsHex(stringToHash.getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

    /**
     * Checks if any provided string is empty or null
     */
    public static boolean isAnyNullOrEmpty(String... providedStrings) {

        for (String providedString: providedStrings) {
            if (providedString == null
                    || providedString.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all provided strings are empty or null
     */
    public static boolean isAllNull(Object... providedStrings) {

        for (Object providedObject: providedStrings) {
            if (providedObject != null) {
                return false;
            }
        }
        return true;
    }

    public static String getApiErrorCode(String info) {
        return info.substring(info.lastIndexOf(".") +1 );

    }

}
