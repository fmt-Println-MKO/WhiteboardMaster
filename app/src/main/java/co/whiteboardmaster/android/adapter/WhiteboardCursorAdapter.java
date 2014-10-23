package co.whiteboardmaster.android.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import co.whiteboardmaster.android.R;
import co.whiteboardmaster.android.model.ThumbImageMessage;
import co.whiteboardmaster.android.model.Whiteboard;
import co.whiteboardmaster.android.utils.ImageLoaderThread;
import co.whiteboardmaster.android.utils.PictureUtils;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardCursorAdapter extends CursorAdapter {

    private static final String TAG = "WhiteboardCursorAdapter";

    private WhiteboardDatabaseHelper.WhiteboardCursor mCursor;

    private ImageLoaderThread mImageLoaderThread;


    private LruCache<String, Bitmap> mMemoryCache;

    public WhiteboardCursorAdapter(Context context, WhiteboardDatabaseHelper.WhiteboardCursor cursor, ImageLoaderThread imageLoaderThread) {
        super(context, cursor, 0);
        mCursor = cursor;
        mImageLoaderThread = imageLoaderThread;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        Log.d(TAG," -------------  image count:" + mCursor.getCount());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.whiteboard_griditem, parent, false);
        ImageView mImageView = (ImageView) view.findViewById(R.id.wm_whiteboard_grid_item_image);
        View progressView = view.findViewById(R.id.wm_whiteboard_grid_item_progress_container);
        ThumbImageMessage imageMessage = new ThumbImageMessage();
        imageMessage.destHeight = 100;
        imageMessage.destWidth = 100;
        imageMessage.resources = view.getResources();
        imageMessage.imageView = mImageView;
        imageMessage.mProgressContainer = progressView;

        view.setTag(imageMessage);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Whiteboard wb = mCursor.getWhiteboard();
//        Log.d(TAG," -------------  courser pos:" + mCursor.getPosition());
//        Log.d(TAG," -------------  whiteboard: " + wb);
        ThumbImageMessage imageMessage = (ThumbImageMessage) view.getTag();
        imageMessage.path = wb.getPath();
        imageMessage.imageId= wb.getId();
        imageMessage.mProgressContainer.setVisibility(View.VISIBLE);
        PictureUtils.cleanImageView(imageMessage.imageView);
        mImageLoaderThread.queueImage(imageMessage,wb.getId());
//        System.out.println("--------size: " + wb.getId() +" -  " + wb.getGuid());
    }

    @Override
    public Whiteboard getItem(int position) {
//        Log.d(TAG," -------------  item pos:" + position);
        return mCursor.getWhiteboard(position);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

}
