package com.danielkim.soundrecorder.database;

import android.provider.BaseColumns;

/**
 * Created by iClaude on 08/05/2017.
 */

public class RecordingsContract {

    // Table "saved_recordings".
    public static class TableSavedRecording implements BaseColumns {
        public static final String TABLE_NAME = "saved_recordings";

        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    // Table "scheduled_recordings".
    public static class TableScheduledRecording implements BaseColumns {
        public static final String TABLE_NAME = "scheduled_recordings";

        public static final String COLUMN_NAME_START = "start"; // start of the recording in ms from epoch
        public static final String COLUMN_NAME_LENGTH = "length"; // length of the recording in ms
    }


    private RecordingsContract() {
    }
}
