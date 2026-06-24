package com.example.workerscontrol.data;

import android.provider.BaseColumns;

public final class WokerDbContract {

    private WokerDbContract() {
    }

    public static final String DATABASE_NAME = "WorkersDB";
    public static final int DATABASE_VERSION = 3;
    public static final class Worker implements BaseColumns {

        public static final String TABLE_NAME = "worker";
        public static final String COLUMN_WORKER_ID = "worker_id";
        public static final String COLUMN_FIO = "fio";
        public static final String COLUMN_POST = "post";
        public static final String COLUMN_MONDAY = "monday";
        public static final String COLUMN_TUESDAY = "tuesday";
        public static final String COLUMN_WEDNESDAY = "wednesday";
        public static final String COLUMN_THURSDAY = "thursday";
        public static final String COLUMN_FRIDAY = "friday";
        public static final String COLUMN_SATURDAY = "saturday";
        public static final String COLUMN_SUNDAY = "sunday";
        public static final String COLUMN_TIME_FROM = "time_from";
        public static final String COLUMN_TIME_TO= "time_to";
        public static final String COLUMN_AVATAR_PATH = "avatar_path";
        public static final String COLUMN_LOGIN = "login";
        public static final String COLUMN_PASSWORD = "password";
    }

    public static final class Events implements BaseColumns {

        public static final String TABLE_NAME = "event";

        public static final String COLUM_EVENT_ID = "event_id";
        public static final String COLUMN_WORKER_ID = "worker_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_TYPE = "type";

        public static final int EVENT_TO_WORK = 1;
        public static final int EVENT_FROM_WORK = 2;

    }

    public static final class AccessLog implements BaseColumns {
        public static final String TABLE_NAME = "access_log";

        public static final String COLUMN_ACCESS_ID = "access_id";
        public static final String COLUMN_WORKER_ID = "worker_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_PLACE = "place";
    }

    public static final class AccessToken implements BaseColumns {
        public static final String TABLE_NAME = "access_token";

        public static final String COLUMN_TOKEN_ID = "token_id";
        public static final String COLUMN_WORKER_ID = "worker_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ISSUE_DATE = "issue_date";
        public static final String COLUMN_EXPIRY_DATE = "expiry_date";
    }
}
