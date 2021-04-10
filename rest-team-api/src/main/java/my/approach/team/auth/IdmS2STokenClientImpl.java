package my.approach.team.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.approach.team.idm.nx.s2s.client.S2STokenClient;
import my.approach.team.idm.nx.s2s.client.cache.TokenCache;
import my.approach.team.idm.nx.token.client.commons.JsonWebToken;
import my.approach.team.idm.nx.token.client.commons.TokenClientException;
import my.approach.team.idm.nx.token.client.commons.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("DuplicatedCode")
@Service
@ConditionalOnProperty(prefix = "idm.saml", name = "enabled", havingValue = "true")
public class IdmS2STokenClientImpl extends AbstractIdmApiClient implements S2STokenClient {
    private static final String MEDIA_TYPE_APPLICATION_JWT = "application/jwt";
    private static final Logger LOG = LoggerFactory.getLogger(IdmS2STokenClientImpl.class);

    private TokenCache cache = TokenCache.DEFAULT();

    public IdmS2STokenClientImpl(IdmIntegrationProperties properties, ObjectMapper objectMapper) {
        super(properties, objectMapper);
    }

    @Override
    public JsonWebToken issueOnBehalfOf(String user) throws TokenClientException {
        final String key = "acc_" + user;
        JsonWebToken result = cache.get(key);
        if (result == null || result.isExpired(properties.getSkewSeconds())) {
            try {
                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("username", user);
                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, createHeadersForFormRequest());
                result = client.postForObject("/token/issue/user", requestEntity, JsonWebToken.class);
                cache.put(key, result);
            } catch (RestClientResponseException rcre) {
                throw new TokenClientException(rcre.getRawStatusCode(), getErrorBodyFromResponse(rcre), rcre);
            }
        }
        return result;
    }

    @Override
    public TokenGenerator tokenGenerator(String onBehalfOfUser) {
        return () -> issueOnBehalfOf(onBehalfOfUser);
    }

    @Override
    public JsonWebToken exchange(String apiKey) throws ApiKeyExchangeException {
        final String key = "api_" + apiKey;
        JsonWebToken result = cache.get(key);
        if (result == null || result.isExpired(properties.getSkewSeconds())) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_APPLICATION_JWT);
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            HttpEntity<String> requestEntity = new HttpEntity<>(apiKey, headers);
            try {
                JsonWebToken token = JsonWebToken.valueOf(Objects.requireNonNull(client.postForObject("/token/exchange", requestEntity, String.class)));
                cache.put(key, token);
            } catch (RestClientResponseException rcre) {
                throw new ApiKeyExchangeException(rcre.getRawStatusCode(), getErrorBodyFromResponse(rcre), rcre);
            }
        }
        return result;
    }

    @Override
    public boolean validate(JsonWebToken token) throws TokenClientException {
        final String key = token.getRepresentation();
        JsonWebToken cachedToken = cache.get(key);
        if (cachedToken != null) {
            LOG.trace("Found token {} in cache", token.getRepresentation());
        }
        if (cachedToken == null || cachedToken.isExpired(properties.getSkewSeconds())) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_APPLICATION_JWT);
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            HttpEntity<String> requestEntity = new HttpEntity<>(token.getRepresentation(), headers);
            try {
                LOG.trace("Storing valid token {} in cache", token.getRepresentation());
                client.exchange("/token/validate", HttpMethod.POST, requestEntity, Void.class);
                cache.put(key, token);
                return true;
            } catch (RestClientResponseException rcre) {
                throw new TokenClientException(rcre.getRawStatusCode(), getErrorBodyFromResponse(rcre), rcre);
            }
        }
        return true;
    }
}
