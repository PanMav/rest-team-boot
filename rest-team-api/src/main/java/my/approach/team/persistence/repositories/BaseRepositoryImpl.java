package my.approach.team.persistence.repositories;

import my.approach.team.model.domain.entities.DualPseudoTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

@Slf4j
@NoRepositoryBean
public class BaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {
    private final JpaEntityInformation<T, ID> entityInfo;
    private final EntityManager em;

    public BaseRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInfo = entityInformation;
        this.em = entityManager;
    }

    @Override
    public String retrieveDBUniqueTeamId() {
        log.info("Requesting new TeamID from DB");
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<String> query = builder.createQuery(String.class);
        query.from(DualPseudoTable.class);
        query.select(builder.function("GRP_AUX_UTL.FNC_GENERATE_GRP_REF_ID", String.class));

        return em.createQuery(query).getSingleResult();
    }

    @Override
    public boolean checkDBValidOPPExists(String oppID) {
        log.info("Checking existence of oppId: " + oppID);
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Boolean> query = builder.createQuery(Boolean.class);
        query.from(DualPseudoTable.class);
        query.select(builder.function
                ("GRP_AUX_UTL.FNC_IS_GRP_OPP_ID_VALID", Boolean.class, builder.literal(oppID)));

        return em.createQuery(query).getSingleResult();
    }

    @Override
    public boolean checkDBValidTeam(String teamId, String teamType) {
        log.info("Checking whether teamId " + teamId + " with teamType " + teamType + " is valid.");
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Boolean> query = builder.createQuery(Boolean.class);
        query.from(DualPseudoTable.class);
        query.select(builder.function
                ("GRP_AUX_UTL.FNC_IS_TEAM_CODE_VALID",
                        Boolean.class, builder.literal(teamId), builder.literal(teamType)));

        return em.createQuery(query).getSingleResult();
    }

    @Override
    public boolean checkDBOPPActive(String oppID) {
        log.info("Checking whether oppId " + oppID + " is active.");
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Boolean> query = builder.createQuery(Boolean.class);
        query.from(DualPseudoTable.class);
        query.select(builder.function
                ("GRP_AUX_UTL.FNC_IS_GRP_OPP_ID_ACTIVE", Boolean.class, builder.literal(oppID)));

        return em.createQuery(query).getSingleResult();
    }



}
