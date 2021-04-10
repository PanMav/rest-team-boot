package my.approach.team.auth;

import my.approach.team.service.SecureEntityService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AbstractAclVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.util.Collection;
import java.util.stream.Stream;

public abstract class AbstractSecureServiceSpecificationInterceptionVoter extends AbstractAclVoter {
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation invocation, Collection<ConfigAttribute> attributes) {
        final Class<?> serviceClass = invocation.getMethod().getDeclaringClass();

        if (!SecureEntityService.class.isAssignableFrom(serviceClass)) {
            return ACCESS_ABSTAIN;
        }

        final Specification<?> spec = (Specification<?>) Stream.of(invocation.getArguments())
                .filter(arg -> Specification.class.isAssignableFrom(arg.getClass()))
                .findFirst().orElse(null);

        if (spec == null) {
            return ACCESS_ABSTAIN;
        }

        return doVote(authentication, spec, invocation.getMethod().getName(), invocation.getMethod().getReturnType());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz) || MethodInvocation.class.isAssignableFrom(clazz);
    }

    public abstract int doVote(Authentication authentication, Specification<?> querySpecs, String secureMethodName, Class<?> returnType);
}
