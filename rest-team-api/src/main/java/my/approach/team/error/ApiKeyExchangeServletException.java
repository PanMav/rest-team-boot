package my.approach.team.error;

import my.approach.team.idm.nx.token.client.commons.ApiKeyExchange;

import javax.servlet.ServletException;

public class ApiKeyExchangeServletException extends ServletException {
    public ApiKeyExchangeServletException(ApiKeyExchange.ApiKeyExchangeException exc) {
        super(exc);
    }
}
