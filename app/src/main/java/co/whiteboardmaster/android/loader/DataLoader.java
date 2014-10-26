package co.whiteboardmaster.android.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by matthiaskoch on 25.10.14.
 */
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    public DataLoader(Context conext) {
        super(conext);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(D data)  {
        mData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

}
