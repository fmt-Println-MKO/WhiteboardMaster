package co.whiteboardmaster.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.whiteboardmaster.android.model.Whiteboard;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "WhiteboardDatabaseHelper";

    private static final String DB_NAME = "whiteboardmaster.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_WM = "whiteboardmaster";
    private static final String COLUMN_WHITEBOARD_ID = "_id";
    private static final String COLUMN_WHITEBOARD_CREATED = "created";
    private static final String COLUMN_WHITEBOARD_UPDATED = "updated";
    private static final String COLUMN_WHITEBOARD_TITLE = "title";
    private static final String COLUMN_WHITEBOARD_IMAGE_FILENAME = "imagefilename";
    private static final String COLUMN_WHITEBOARD_THUMB_FILENAME = "thumbfilename";
    private static final String COLUMN_WHITEBOARD_DESCRIPTION = "description";
    private static final String COLUMN_WHITEBOARD_GUID = "guid";

    public WhiteboardDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_WM + " ( " +
                COLUMN_WHITEBOARD_ID + " integer primary key autoincrement, " +
                COLUMN_WHITEBOARD_TITLE + " varchar(250), " +
                COLUMN_WHITEBOARD_IMAGE_FILENAME + " varchar(64), " +
                COLUMN_WHITEBOARD_THUMB_FILENAME + " varchar(64), " +
                COLUMN_WHITEBOARD_DESCRIPTION + " text," +
                COLUMN_WHITEBOARD_CREATED + " integer, " +
                COLUMN_WHITEBOARD_UPDATED + " integer, " +
                COLUMN_WHITEBOARD_GUID + " varchar(128) )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Whiteboard insertWhiteboard(Whiteboard whiteboard) {
        ContentValues cv = makeBaseContentValues(whiteboard);
        cv.put(COLUMN_WHITEBOARD_CREATED, whiteboard.getCreated());

        long id = getWritableDatabase().insert(TABLE_WM, null, cv);
        if (id > 0) {
            return  new Whiteboard.WhiteBoardBuilder(whiteboard).setId(id).build();
        } else {
            return null;
        }
    }

    public WhiteboardCursor queryWhiteboards() {
        Cursor wrapped = getReadableDatabase().query(TABLE_WM, null, null, null, null, null, COLUMN_WHITEBOARD_CREATED + " desc");
        return new WhiteboardCursor(wrapped);
    }

    public boolean deleteWhiteboard(long id) {
        int rows = getWritableDatabase().delete(TABLE_WM, COLUMN_WHITEBOARD_ID + " = ?", new String[]{String.valueOf(id)});
        return rows == 1;
    }

    public boolean updateWhiteBoard(Whiteboard wb) {
        int rows = getWritableDatabase().update(TABLE_WM, makeBaseContentValues(wb), COLUMN_WHITEBOARD_ID + " =? ", new String[]{String.valueOf(wb.getId())});
        return rows == 1;
    }

    public Whiteboard getWhiteboardByGuid(String guid) {
        Cursor wrapped = getReadableDatabase().query(TABLE_WM, null, COLUMN_WHITEBOARD_GUID + " = ?", new String[]{guid}, null, null, COLUMN_WHITEBOARD_CREATED + " desc");

        if (wrapped.getCount() == 1) {
            return new WhiteboardCursor(wrapped).getWhiteboard(0);
        }
        return null;
    }

    private ContentValues makeBaseContentValues(Whiteboard whiteboard) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WHITEBOARD_UPDATED, System.currentTimeMillis());
        cv.put(COLUMN_WHITEBOARD_TITLE, whiteboard.getTitle());
        cv.put(COLUMN_WHITEBOARD_DESCRIPTION, whiteboard.getDescription());
        cv.put(COLUMN_WHITEBOARD_IMAGE_FILENAME, whiteboard.getImageFileName());
        cv.put(COLUMN_WHITEBOARD_THUMB_FILENAME, whiteboard.getThumbFileName());
        cv.put(COLUMN_WHITEBOARD_GUID, whiteboard.getGuid());
        return cv;
    }

    public static class WhiteboardCursor extends CursorWrapper {

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        public WhiteboardCursor(Cursor cursor) {
            super(cursor);
        }

        public Whiteboard getWhiteboard() {
            return getWhiteboardAtCursor();
        }

        public Whiteboard getWhiteboard(int position) {
            if (moveToPosition(position)) {
                return getWhiteboardAtCursor();
            } else {
                return null;
            }
        }

        private Whiteboard getWhiteboardAtCursor() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            Whiteboard.WhiteBoardBuilder wb = new Whiteboard.WhiteBoardBuilder();
            wb.setId(getLong(getColumnIndex(COLUMN_WHITEBOARD_ID)));
            wb.setTitle(getString(getColumnIndex(COLUMN_WHITEBOARD_TITLE)));
            wb.setDescription(getString(getColumnIndex(COLUMN_WHITEBOARD_DESCRIPTION)));
            wb.setImageFileName(getString(getColumnIndex(COLUMN_WHITEBOARD_IMAGE_FILENAME)));
            wb.setThumbFileName(getString(getColumnIndex(COLUMN_WHITEBOARD_THUMB_FILENAME)));
            wb.setCreated(getLong(getColumnIndex(COLUMN_WHITEBOARD_CREATED)));
            wb.setUpdated(getLong(getColumnIndex(COLUMN_WHITEBOARD_UPDATED)));
            wb.setGuid(getString(getColumnIndex(COLUMN_WHITEBOARD_GUID)));

            return wb.build();
        }
    }
}
