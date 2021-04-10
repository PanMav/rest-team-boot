package my.approach.team.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface SecureEntityService<SE> {
    Page<SE> fetchEntitiesPage(Pageable pageable, Specification<SE> specification);
}
