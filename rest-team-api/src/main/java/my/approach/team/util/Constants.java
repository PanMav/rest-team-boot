package my.approach.team.util;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static class Cache {
        public static final String USERS = "users";

    }

    public static class Graphs {

        public static class User {
            public static final String DEFAULT_SINGLE = "DefaultSingleUserGraph";

            public static class Subgraph {
                public static final String ROLES = "UserRolesSubgraph";
            }
        }
    }

    public static final List<String> allowedExtendedInfoKeys = Arrays.asList("ReferenceTeamCode", "ReferenceTeamType", "Active", "Confidential");

    public enum MemberTypes {
        Substance,
        Team
    }

    public enum ResourceActionTypes {
        UPDATE,
        CREATE,
        SEARCH
    }

    public static final String GMT_TEAM_TYPE = "GMT";
    public static final String GGT_TEAM_TYPE = "GGT";
    public static final String REGULATED_TEAM_TYPE = "REGULATED";


}
