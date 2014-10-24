package co.whiteboardmaster.android.adapter;


import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseIntArray;

import java.util.HashMap;

import co.whiteboardmaster.android.WhiteboardDetailsFragment;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardCursorFragmentPagerAdapter extends FragmentStatePagerAdapter {

    protected boolean mDataValid;
    protected WhiteboardDatabaseHelper.WhiteboardCursor mCursor;
    protected Context mContext;
    protected SparseIntArray mItemPositions;
    protected HashMap<Object, Integer> mObjectMap;
    protected int mRowIDColumn;

    public WhiteboardCursorFragmentPagerAdapter(Context context, FragmentManager fm, WhiteboardDatabaseHelper.WhiteboardCursor cursor) {
        super(fm);

        init(context, cursor);
    }

    void init(Context context, WhiteboardDatabaseHelper.WhiteboardCursor c) {
        mObjectMap = new HashMap<Object, Integer>();
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
    }

    @Override
    public int getItemPosition(Object object) {
        Integer rowId = mObjectMap.get(object);
        if (rowId != null && mItemPositions != null) {
            return mItemPositions.get(rowId, POSITION_NONE);
        }
        return POSITION_NONE;
    }

    @Override
    public WhiteboardDetailsFragment getItem(int position) {
        if (mDataValid) {
            return WhiteboardDetailsFragment.newInstance(mCursor.getWhiteboard(position));
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        if (mDataValid) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }
}