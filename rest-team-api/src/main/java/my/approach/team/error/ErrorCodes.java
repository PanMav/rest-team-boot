package my.approach.team.error;

public class ErrorCodes {

    public static final class Codes {
        //Data completeness
        public static final int CMP_TEAM_MISSING_REQUIRED_FIELDS              = 1001;
        public static final int CMP_TEAM_MISSING_EXTENDED_INFO                = 1003;
        public static final int CMP_TEAM_MISSING_TEAM_TYPE                   = 1004;
        public static final int CMP_TEAM_MEMBER_OPP_ID_NOT_FOUND              = 1005;
        public static final int CMP_TEAM_MEMBER_OPP_ID_NOT_ACTIVE             = 1006;
        public static final int CMP_TEAM_MEMBER_ID_NOT_VALID                  = 1007;
        public static final int CMP_TEAM_EXTENDED_INFO_OPP_ID_NOT_VALID       = 1008;

        //Data correctness
        public static final int COR_TEAM_ID_FORMAT_NOT_VALID_                 = 2001;
        public static final int COR_TEAM_CODE_FORMAT_NOT_VALID_               = 2002;
        public static final int COR_TEAM_CODE_UNIQUE_PER_TEAM_TYPE           = 2003;
        public static final int COR_TEAM_NAME_UNIQUE_PER_TEAM_TYPE           = 2004;
        public static final int COR_TEAM_REFERENCE_ID_FORMAT_NOT_VALID        = 2005;
        public static final int COR_TEAM_EXTENDED_INFO_SNG_REF_GRP_TYPE       = 2006;
        public static final int COR_TEAM_REFERENCE_ID_TEAM_ID_NOT_FOUND      = 2007;
        public static final int COR_TEAM_TEAM_TYPE_NOT_VALID                 = 2008;
        public static final int COR_TEAM_MEMBERS_IDENTIFIER_NOT_VALID         = 2009;
        public static final int COR_TEAM_MEMBER_TYPE_NOT_VALID                = 2010;
        public static final int COR_TEAM_EXTENDED_INFO_PAIRS_ONLY             = 2011;
        public static final int COR_TEAM_EXTENDED_INFO_REF_TYPE_INVALID_FORMAT= 2012;
        public static final int COR_TEAM_EXTENDED_INFO_REF_CODE_INVALID_FORMAT= 2013;
        public static final int COR_TEAM_EXTENDED_INFO_ACTIVE_INVALID_VALUE   = 2014;
        public static final int COR_TEAM_EXTENDED_INFO_CONFIDENTIAL_INVALID_VALUE   = 2015;
    }

    public static final class CodeDescription {
        //Data completeness
        public static final String Error_1001 = "Team missing mandatory attributes (Code|Type|Name)";
        public static final String Error_1002 = "Team missing mandatory attributes (TeamID)";
        public static final String Error_1003 = "Please provide the correct key/value combination in extendedInfo area, with no empty values";
        public static final String Error_1004 = "No Team Type is present";
        public static final String Error_1005 = "Substance id (Provided memberId) %s was not found ";
        public static final String Error_1006 = "Substance id (Provided memberId) %s is not active";
        public static final String Error_1007 = "Member (TeamId) %s is not present";
        public static final String Error_1008 = "OPP provided %s does not exist or is not active";

        //Data correctness
        public static final String Error_2001 = "TeamID format is not valid";
        public static final String Error_2002 = "The teamCode %s does not have a valid format";
        public static final String Error_2003 = "The teamCode %s is already used in another team for the same source.";
        public static final String Error_2004 = "The teamName %s is already used in another team for the same source.";
        public static final String Error_2005 = "TeamReferenceID %s is not valid";
        public static final String Error_2006 = "You can reference only one system in the extendedInfo area.";
        public static final String Error_2007 = "Key %s provided in the teamReferenceIDs is not found in the Team Registry.";
        public static final String Error_2008 = "The teamType provided is not valid";
        public static final String Error_2009 = "Member id %s is not valid";
        public static final String Error_2010 = "Member type %s is invalid or not given";
        public static final String Error_2011 = "Only the following values are valid for the ExtendedInfo area:" +
                "ReferenceTeamCode | ReferenceTeamType | Active | Confidential";
        public static final String Error_2012 = "Team format %s is not valid";
        public static final String Error_2013 = "TeamType defined in extendedInfo is not valid," + " allowed values are: GGT|GMT|REGULATED";
        public static final String Error_2014 = "Extended info value for attribute Active can be true or false";
        public static final String Error_2015 = "Extended info value for attribute Confidential can be true or false";
    }

}
