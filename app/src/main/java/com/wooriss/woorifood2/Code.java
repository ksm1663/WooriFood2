package com.wooriss.woorifood2;

public class Code {

    public static final String LOG_TAG = "plz";
    public static final int SPLASH_DELAY = 2300;

    // about KFTC FILE DOWNLOAD
    public static final int KFTC_DOWNLOAD_TOTAL_RETRIES = 3;
    public static final String KFTC_DOWNLOAD_URL = "https://www.kftc.or.kr/common/download1.jsp?filename=codefilex.text&sysfilename=codefilex.text&mode=direct/";
    public static final String FKTC_DOWNLOAD_BASE_URL = "https://www.kftc.or.kr";
    public static final String KFTC_DOWNLOAD_FILENAME = "codefilex.text";
    public static final String KFTC_DOWNLOAD_MODE = "direct";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CODE.DB";
    public static final String TABLE_NAME = "TB_CODE";


    //
    public static final int PROGRESS_LOADING_ON = 0;
    public static final int PROGRESS_LOADING_OFF = 1;

    // about api
    public static final String KAKAO_BASE_URL = "https://dapi.kakao.com";
    public static final String KAKAO_API_KEY = "KakaoAK 7aea75b45c61b6eeb0df2f3762c857a1";
    public static final int MAX_LIST_SIZE = 15;


    // about search
    public static final int NEAR_KILOMETER = 4000;


    // about FoodLocaion
    public static final int NEED_SET_MAIN_LIST = 1;


    public static final String SQL_CREATE_ENTRIES =
                    "create table " + TABLE_NAME + " " +
                    "(code text primary key, " + // 금융기관 고유 코드
                    "name text," + // 금융기관명
                    "branch_name text," + // 금융기관지점명
                    "tel text," + // 전화번호
                    "fax text," + // 팩스
                    "zip text," + // 우편번호
                    "addr text)"; // 주소

    public static final String SQL_DELETE_ENTRIES =
                    "drop table if exists " +TABLE_NAME;

    public static final String SQL_CHECK_TABLE =
                    "select name from sqlite_master where type='table'" +
                            " and name = '" + Code.TABLE_NAME+"'";

    public static final String SQL_CHECK_TABLE_OK =
            "select * from " + Code.TABLE_NAME + " where code=\"LAST\"";

    public static final String SQL_CHECK_SIZE_TABLE =
                    "select count(*) from " + Code.TABLE_NAME;

    public static final String SQL_INSERT_LAST_FLAG =
                    "insert into " + Code.TABLE_NAME +
                            " values ('LAST', '','','','','','');";

    //public static final String SQL_GET_WOORI_BRANCH =
    //        "Select * from " + Code.TABLE_NAME + " where name like \"우리%\"" + " and (code like \"020%\" or code like \"20%\")";

    public static final String SQL_GET_WOORI_BRANCH =
            "Select * from " + Code.TABLE_NAME + " where name like \"우리%\"" + " " +
                    "and (code like \"020%\" or code like \"022%\" or code like \"024%\" or code like \"083%\" or code like \"084%\" or code like \"20%\")";


    public static final String SQL_DELETE_DATA =
            "Delete from " + Code.TABLE_NAME;

    public static final int REQUEST_PERMISSION_CODE = 1001;
    public static final int CAMERA_CODE = 1111;

    public class ViewType {
        public static final int DEFAULT_SIKDANG = 0;
        public static final int REVIEWED_SIKDANG = 1;
        public static final int MY_SIKDANG = 2;
    }

    public class MapType {
        public static final int SEARCH_MAP = 0;
        public static final int REVIEWED_MAP = 1;
    }

    public class PriceType {
        public static final int CHEAP = 1;
        public static final int NORMAL = 2;
        public static final int EXPENSIVE = 3;
    }

    public class VisitType {
        public static final int FIRST = 1;
        public static final int SECOND = 2;
        public static final int THIRD = 3;
    }

    public class ComplexType {
        public static final int COZY = 1;
        public static final int NORMAL = 2;
        public static final int BUZY = 3;
    }

    public class LuxuryType {
        public static final int BAD = 1;
        public static final int NORMAL = 2;
        public static final int GOOD = 3;
    }


}
