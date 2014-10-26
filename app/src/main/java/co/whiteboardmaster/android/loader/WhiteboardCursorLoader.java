package co.whiteboardmaster.android.loader;

import android.content.Context;
import android.database.Cursor;

import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 25.10.14.
 */
public class WhiteboardCursorLoader extends SQLiteCursorLoader {

    private WhiteboardDatabaseHelper mHelper;

    public WhiteboardCursorLoader(Context context) {
        super(context);
        mHelper = new WhiteboardDatabaseHelper(context);
    }

    @Override
    protected Cursor loadCursor() {
        return mHelper.queryWhiteboards();
    }
}
