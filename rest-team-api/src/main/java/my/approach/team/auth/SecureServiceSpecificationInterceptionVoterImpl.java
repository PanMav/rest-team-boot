package my.approach.team.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SecureServiceSpecificationInterceptionVoterImpl extends AbstractSecureServiceSpecificationInterceptionVoter {
    private static final List<Class<?>> COLLECTION_RETURN_TYPES = Arrays.asList(List.class, Page.class, Slice.class, Stream.class);
    private final EntityManager em;

    @Override
    public int doVote(Authentication authentication, Specification<?> querySpecs, String secureMethodName, Class<?> returnType) {
        if (COLLECTION_RETURN_TYPES.contains(returnType)) {
            // @TODO :: Do additional stuff with specification
            return ACCESS_GRANTED;
        }

        // @TODO :: Check if user can access specific single resource
        return ACCESS_GRANTED;
    }
}
