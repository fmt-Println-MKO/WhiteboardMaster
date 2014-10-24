package co.whiteboardmaster.android.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
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

    public WhiteboardCursorAdapter(Context context, WhiteboardDatabaseHelper.WhiteboardCursor cursor, ImageLoaderThread imageLoaderThread) {
        super(context, cursor, 0);
        mCursor = cursor;
        mImageLoaderThread = imageLoaderThread;
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
        imageMessage.context = context;
        imageMessage.imageView = mImageView;
        imageMessage.mProgressContainer = progressView;

        view.setTag(imageMessage);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Whiteboard wb = mCursor.getWhiteboard();
//        Log.d(TAG," -------------  courser pos:" + mCursor.getPosition());
//        Log.d(TAG, " -------------  whiteboard: " + wb.getId());
        ThumbImageMessage imageMessage = (ThumbImageMessage) view.getTag();
        imageMessage.path = PictureUtils.getPathToFile(context, wb.getThumbFileName());
        imageMessage.imageId = wb.getId();
        imageMessage.mProgressContainer.setVisibility(View.VISIBLE);
        imageMessage.imageView.setImageDrawable(null);

        BitmapDrawable cachedImage = mImageLoaderThread.getBitmapFromMemCache(wb.getId());

//        Log.d(TAG, " -------------  cachedImage: " + cachedImage);
//        if (cachedImage != null) {
//            Log.d(TAG, " ------------- recycled: " + cachedImage.getBitmap().isRecycled() + " -- " + cachedImage.getBitmap());
//        }
//
        if (cachedImage == null || cachedImage.getBitmap().isRecycled()) {
//            Log.d(TAG, " ------------- null or recycled: " );
            mImageLoaderThread.queueImage(imageMessage, wb.getId());
        } else {
//            Log.d(TAG, " ------------- using: " + cachedImage.getBitmap().isRecycled() + " hash: " + cachedImage.getBitmap());
            imageMessage.mProgressContainer.setVisibility(View.INVISIBLE);
            imageMessage.imageView.setImageDrawable(cachedImage);

//            mListener.onImageLoaded(token, image);
        }
//        System.out.println("--------size: " + wb.getId() +" -  " + wb.getGuid());
    }

    @Override
    public Whiteboard getItem(int position) {
//        Log.d(TAG," -------------  item pos:" + position);
        return mCursor.getWhiteboard(position);
    }
}
