package co.whiteboardmaster.android.utils;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.LruCache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.whiteboardmaster.android.model.ThumbImageMessage;

/**
 * Created by matthiaskoch on 20.10.14.
 */
public class ImageLoaderThread<Token> extends HandlerThread {

    private static final String TAG = "ImageLoaderThread";

    private LruCache<Integer, BitmapDrawable> mMemoryCache;


    Handler mHandler;
    private Map<Token, Integer> requestMap = Collections.synchronizedMap(new HashMap<Token, Integer>());

    private Handler mResponseHandler;
    private Listener<Token> mListener;

    public interface Listener<Token> {
        void onImageLoaded(Token token, BitmapDrawable thumbnail);
    }

    public ImageLoaderThread(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<Integer, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, BitmapDrawable bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getBitmap().getByteCount() / 1024;
            }
        };

    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Token token = (Token) msg.obj;
                handleImage(token);
            }
        };

    }

    private void handleImage(final Token token) {
        ThumbImageMessage imageMessage = (ThumbImageMessage) token;
        final String path = imageMessage.path;
        final int imageId = imageMessage.imageId;
//        Log.i(TAG, "------handle image loading: " + imageId);
//        BitmapDrawable processedImage = null;
//        Bitmap cachedImage = getBitmapFromMemCache(imageId);
//        if (cachedImage == null) {
//            processedImage = PictureUtils.getScaledDrawable(imageMessage.destWidth, imageMessage.destHeight, path, imageMessage.resources);
//            addBitmapToMemoryCache(imageId, processedImage.getBitmap());
//        } else {
//            processedImage = new BitmapDrawable(imageMessage.resources, cachedImage);
//        }
//        final BitmapDrawable image = processedImage;

        final BitmapDrawable image = PictureUtils.getScaledDrawable(imageMessage.destWidth, imageMessage.destHeight, path, imageMessage.resources);
        addBitmapToMemoryCache(imageId, image);

        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
//                System.out.println("--- " + requestMap);
//                System.out.println("--- " + token);
//                System.out.println("--- " + imageId);
//                System.out.println("--- " + requestMap.get(token));
                if (requestMap.get(token) == null || requestMap.get(token).intValue() != imageId) {
                    //image.getBitmap().recycle();
                    return;
                }
                requestMap.remove(token);
                mListener.onImageLoaded(token, image);
//                Log.i(TAG, "-----handle image loading: " +  image.getBitmap());
//                addBitmapToMemoryCache(imageId, image);

            }
        });
    }

    public void queueImage(Token token, int imageId) {
        requestMap.put(token, new Integer(imageId));
        mHandler.obtainMessage(ThumbImageMessage.THUMB_IMAGE, token).sendToTarget();

    }

    public void clearQueue() {
        mHandler.removeMessages(ThumbImageMessage.THUMB_IMAGE);
        requestMap.clear();
    }

    public void addBitmapToMemoryCache(Integer key, BitmapDrawable bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public BitmapDrawable getBitmapFromMemCache(Integer key) {
        return mMemoryCache.get(key);
    }


}
