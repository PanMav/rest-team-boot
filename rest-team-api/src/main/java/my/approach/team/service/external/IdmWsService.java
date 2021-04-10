package my.approach.team.service.external;

import my.approach.team.error.IdmApiClientException;
import my.approach.team.model.dto.s2s.IdmUser;

import java.util.List;

public interface IdmWsService {
    IdmUser fetchUser(String username) throws IdmApiClientException;
    List<String> fetchUserRoles(String username) throws IdmApiClientException;
}
