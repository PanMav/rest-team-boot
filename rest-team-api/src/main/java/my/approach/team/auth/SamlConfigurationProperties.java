package my.approach.team.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "idm.saml")
@Validated
@Getter
@Setter
@NoArgsConstructor
public class SamlConfigurationProperties {
    private boolean enabled;
    private String idpUrlProperty;
    private String idpUrl;
    private String sloRedirectPath;
    private String keystoreLocation;
    private String keystorePass;
    private String signingKeyAlias;
    private String signingKeyPass;
    private String trustedCertificateAlias;
    private String voucherTokenAttr;
    private boolean stateless = true;

    public Map<String, String> toFilterInitParamMap() throws FileNotFoundException {
        Map<String, String> props = new HashMap<>();
        props.put("IDP_URL_PROPERTY", idpUrlProperty);
        props.put("KEYSTORE_LOCATION", keystoreLocation);
        props.put("SIGNING_KEY_ALIAS", signingKeyAlias);
        props.put("TRUSTED_CERTIFICATE_ALIAS", trustedCertificateAlias);
        props.put("VOUCHER_TOKEN_ATTR", voucherTokenAttr);
        props.put("SLO_REDIRECT_PATH", sloRedirectPath);
        props.put("STATELESS", String.valueOf(stateless));
        props.put("KEYSTORE_PASS", keystorePass);
        props.put("SIGNING_KEY_PASS", signingKeyPass);

        return props;
    }
}
