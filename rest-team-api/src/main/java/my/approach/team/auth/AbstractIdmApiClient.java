package my.approach.team.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.approach.team.idm.nx.token.client.commons.JsonWebToken;
import my.approach.team.idm.nx.token.client.commons.TokenClientException;
import my.approach.team.idm.nx.token.client.commons.TokenGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractIdmApiClient implements TokenGenerator {
    protected JsonWebToken systemToken;
    protected RestTemplate client;
    protected final IdmIntegrationProperties properties;
    private final ObjectMapper objectMapper;

    @PostConstruct
    private void init() {
        initClient();
        issueSystemToken();
    }

    @Override
    public JsonWebToken generate() {
        if (systemToken == null || systemToken.isExpired(properties.getSkewSeconds())) {
            issueSystemToken();
        }
        return systemToken;
    }

    protected void initClient() {
        client = new RestTemplate();
        client.setUriTemplateHandler(new DefaultUriBuilderFactory(properties.getBaseUrl()));
        client.setInterceptors(Collections.singletonList((request, body, execution) -> {
            // Abstain when issuing/refreshing system token
            if (!request.getURI().getPath().endsWith("/issue")) {
                List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
                if (systemToken == null || systemToken.isExpired(properties.getSkewSeconds())) {
                    systemToken = generate();
                }

                if (systemToken != null && !systemToken.isExpired(properties.getSkewSeconds()) && (authHeaders == null || authHeaders.isEmpty())) {
                    request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Token " + systemToken.getRepresentation());
                }
            }

            return execution.execute(request, body);
        }));
    }

    protected void issueSystemToken() throws TokenClientException {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", properties.getUsername());
        map.add("password", properties.getPassword());
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, createHeadersForFormRequest());
        try {
            String representation = Objects.requireNonNull(client.postForObject("/token/issue", requestEntity, String.class));
            systemToken = JsonWebToken.valueOf(representation);
        } catch (RestClientResponseException rcre) {
            log.error("Failed to issue a system token for user [{}] using token server [{}]. Received HTTP response {}.",
                    properties.getUsername(), properties.getBaseUrl(), rcre.getRawStatusCode());
            throw new TokenClientException(rcre.getRawStatusCode(), getErrorBodyFromResponse(rcre), rcre.getCause());
        } catch (NullPointerException npe) {
            log.error("Token response returned null");
            throw new TokenClientException(200, Collections.emptyMap(), npe);
        }
    }

    protected Map<String, Object> getErrorBodyFromResponse(RestClientResponseException rcre) {
        Map<String, Object> errorBody;
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
            };
            errorBody = objectMapper.readValue(rcre.getResponseBodyAsByteArray(), typeRef);
        } catch (IOException jioe) {
            log.warn("Could not deserialize error response body to Map", rcre);
            errorBody = Collections.emptyMap();
        }

        return errorBody;
    }

    protected static HttpHeaders createHeadersForFormRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }
}
