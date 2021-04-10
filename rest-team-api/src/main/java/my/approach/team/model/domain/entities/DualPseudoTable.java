package my.approach.team.model.domain.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is dummy pseudo table class representation contained in the Oracle DB
 * used mainly for assisting in calling functions
 */
@Entity
@Table(name = "DUAL")
public class DualPseudoTable {

    @Id
    @Column(name = "DUMMY")
    private String dummy;
}
