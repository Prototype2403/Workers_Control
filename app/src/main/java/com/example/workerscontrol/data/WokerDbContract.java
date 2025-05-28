package com.example.workerscontrol.data;

import android.provider.BaseColumns;

public final class WokerDbContract {

    private WokerDbContract() {
    }

    public static final String DATABASE_NAME = "WorkersDB";
    public static final int DATABASE_VERSION = 1;
    public static final class Worker implements BaseColumns {

        public static final String TABLE_NAME = "worker";
        public static final String COLUMN_WORKER_ID = "worker_id";
        public static final String COLUMN_FIO = "fio";
        public static final String COLUMN_POST = "post";
        public static final String COLUMN_MONDAY = "monday";
        public static final String COLUMN_TUESDAY = "tuesday";
        public static final String COLUMN_WEDNESDAY = "wednesday";
        public static final String COLUMN_THURSDAY = "thursday";
        public static final String COLUMN_FRIDAY = "friday ";
        public static final String COLUMN_SATURDAY = "saturday";
        public static final String COLUMN_SUNDAY = "sunday";
        public static final String COLUMN_TIME_FROM = "time_from";
        public static final String COLUMN_TIME_TO= "time_to";
    }

    public static final class Events implements BaseColumns {

        public static final String TABLE_NAME = "event";

        public static final String COLUM_EVENT_ID = "event_id";
        public static final String COLUMN_WORKER_ID = "worker_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_TYPE = "type";

    }
}
