package my.approach.team.auth;

import lombok.Data;
import lombok.NonNull;

@Data
public class VoucherTokenCredentials {
    @NonNull
    private final String voucherToken;
}
