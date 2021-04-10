package my.approach.team.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.approach.team.auth.AbstractIdmApiClient;
import my.approach.team.auth.IdmIntegrationProperties;
import my.approach.team.error.IdmApiClientException;
import my.approach.team.model.dto.s2s.IdmUser;
import my.approach.team.model.dto.s2s.IdmWsApiPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@ConditionalOnBean(IdmIntegrationProperties.class)
public class IdmWsServiceImpl extends AbstractIdmApiClient implements IdmWsService {
    public IdmWsServiceImpl(IdmIntegrationProperties properties, ObjectMapper objectMapper) {
        super(properties, objectMapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> fetchUserRoles(String username) throws IdmApiClientException {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(properties.getBaseUrl() + "/role")
                    .queryParam("usernames", username)
                    .build().encode().toUri();
            IdmWsApiPageResponse<String> response = client.getForObject(uri, IdmWsApiPageResponse.class);
            return Objects.requireNonNull(response).getElements();
        } catch (HttpClientErrorException exc) {
            log.warn(exc.getLocalizedMessage(), exc);
            throw new IdmApiClientException(exc.getLocalizedMessage(), exc);
        }
    }

    @Override
    public IdmUser fetchUser(String username) throws IdmApiClientException {
        try {
            URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + "/user/" + username).build().encode().toUri();
            return client.getForObject(uri, IdmUser.class);
        } catch (HttpClientErrorException exc) {
            log.warn(exc.getLocalizedMessage(), exc);
            throw new IdmApiClientException(exc.getLocalizedMessage(), exc);
        }
    }
}
