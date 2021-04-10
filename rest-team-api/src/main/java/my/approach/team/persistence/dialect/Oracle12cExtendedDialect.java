package my.approach.team.persistence.dialect;


import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class Oracle12cExtendedDialect extends Oracle12cDialect {
    public Oracle12cExtendedDialect() {
        super();
        registerFunction(
                "GRP_AUX_UTL.FNC_GENERATE_GRP_REF_ID",
                new SQLFunctionTemplate(
                        StandardBasicTypes.STRING,
                        "GRP_AUX_UTL.FNC_GENERATE_GRP_REF_ID"
                )
        );
        registerFunction(
                "GRP_AUX_UTL.FNC_IS_GRP_OPP_ID_VALID",
                new SQLFunctionTemplate(
                        StandardBasicTypes.NUMERIC_BOOLEAN,
                        "GRP_AUX_UTL.FNC_IS_GRP_OPP_ID_VALID" + "(?1)"
                )
        );
        registerFunction(
                "GRP_AUX_UTL.FNC_IS_GRP_OPP_ID_ACTIVE",
                new SQLFunctionTemplate(
                        StandardBasicTypes.NUMERIC_BOOLEAN,
                        "GRP_AUX_UTL.FNC_IS_GRP_OPP_ID_ACTIVE" + "(?1)"
                )
        );
        registerFunction(
                "GRP_AUX_UTL.FNC_IS_TEAM_CODE_VALID",
                new SQLFunctionTemplate(
                        StandardBasicTypes.NUMERIC_BOOLEAN,
                        "GRP_AUX_UTL.FNC_IS_TEAM_CODE_VALID" + "(?1, ?2)"
                )
        );
    }
}
