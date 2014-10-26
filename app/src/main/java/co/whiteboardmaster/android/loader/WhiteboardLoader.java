package co.whiteboardmaster.android.loader;

import android.content.Context;

import co.whiteboardmaster.android.model.Whiteboard;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 25.10.14.
 */
public class WhiteboardLoader extends DataLoader<Whiteboard> {

    private String guid;
    private WhiteboardDatabaseHelper mHelper;


    public WhiteboardLoader(Context context, String guid) {
        super(context);
        this.guid = guid;
        mHelper = new WhiteboardDatabaseHelper(context);
    }

    @Override
    public Whiteboard loadInBackground() {
        return mHelper.getWhiteboardByGuid(this.guid);
    }
}
