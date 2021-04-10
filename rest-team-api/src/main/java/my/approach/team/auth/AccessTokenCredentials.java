package my.approach.team.auth;

import my.approach.team.idm.nx.token.client.commons.JsonWebToken;
import lombok.Data;
import lombok.NonNull;

@Data
public class AccessTokenCredentials {
    @NonNull
    private final JsonWebToken accessToken;
}
