package my.approach.team.model.domain.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@Table(name = "THH_GRP_SRC_BUSINESS_PROC")
public class TeamType {
    @Id
    @Column(name = "GRP_SRC_BUSINESS_PROC_ID")
    private int id;

    @Column(name = "BUSINESS_SRC_PROC_CODE")
    private String businessProcCode;

    @Column(name = "BUSINESS_SRC_PROC_DESCRIPTION")
    private String businessProcDescription;

    @Column(name = "BUSINESS_SRC_PROC_COMMENTS")
    private String businessProcComments;
}
