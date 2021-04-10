package my.approach.team.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Properties bean for use by the {@link my.approach.team.idm.nx.s2s.client.S2STokenClient}
 * implementation for Spring.
 */
@Configuration
@ConfigurationProperties(prefix = "idm.integration")
@NoArgsConstructor
@Validated
@Getter
@Setter
@ConditionalOnProperty(prefix = "idm.saml", name = "enabled", havingValue = "true")
public class IdmIntegrationProperties {
    @NotNull(message = "You must provide a baseUrl for IDM integration")
    private String baseUrl;

    @NotNull(message = "You must provide a system username for IDM integration")
    private String username;

    @NotNull(message = "You must provide a system password for IDM integration")
    private String password;

    @Min(15)
    @Max(300)
    private int skewSeconds = 60;

    private boolean allowNonSSL = false;
}
