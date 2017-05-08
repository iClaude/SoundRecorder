package com.danielkim.soundrecorder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;

import java.util.Comparator;

import static com.danielkim.soundrecorder.database.RecordingsContract.TableSavedRecording;
import static com.danielkim.soundrecorder.database.SQLStrings.CREATE_TABLE_SAVED_RECORDINGS;

/**
 * Created by Daniel on 12/29/2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;
    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    private static final String LOG_TAG = "DBHelper";
    public static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION = 1;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SAVED_RECORDINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    public long addRecording(String recordingName, String filePath, long length) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH, length);
        cv.put(TableSavedRecording.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
        long rowId = db.insert(TableSavedRecording.TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public void removeItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(id)};
        db.delete(TableSavedRecording.TABLE_NAME, "_ID=?", whereArgs);
    }

    public RecordingItem getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TableSavedRecording._ID,
                TableSavedRecording.COLUMN_NAME_RECORDING_NAME,
                TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH,
                TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH,
                TableSavedRecording.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(TableSavedRecording.TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItem item = new RecordingItem();
            item.setId(c.getInt(c.getColumnIndex(TableSavedRecording._ID)));
            item.setName(c.getString(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_RECORDING_NAME)));
            item.setFilePath(c.getString(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setLength(c.getInt(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH)));
            item.setTime(c.getLong(c.getColumnIndex(TableSavedRecording.COLUMN_NAME_TIME_ADDED)));
            c.close();
            return item;
        }
        return null;
    }

    public void renameItem(RecordingItem item, String recordingName, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        db.update(TableSavedRecording.TABLE_NAME, cv,
                TableSavedRecording._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {TableSavedRecording._ID};
        Cursor c = db.query(TableSavedRecording.TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public Context getContext() {
        return mContext;
    }

    public class RecordingComparator implements Comparator<RecordingItem> {
        public int compare(RecordingItem item1, RecordingItem item2) {
            Long o1 = item1.getTime();
            Long o2 = item2.getTime();
            return o2.compareTo(o1);
        }
    }

    public long restoreRecording(RecordingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_NAME, item.getName());
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_FILE_PATH, item.getFilePath());
        cv.put(TableSavedRecording.COLUMN_NAME_RECORDING_LENGTH, item.getLength());
        cv.put(TableSavedRecording.COLUMN_NAME_TIME_ADDED, item.getTime());
        cv.put(TableSavedRecording._ID, item.getId());
        long rowId = db.insert(TableSavedRecording.TABLE_NAME, null, cv);
        if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;
    }
}
