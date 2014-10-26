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

    private LruCache<Long, BitmapDrawable> mMemoryCache;


    Handler mHandler;
    private Map<Token, Long> requestMap = Collections.synchronizedMap(new HashMap<Token, Long>());

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

        mMemoryCache = new LruCache<Long, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(Long key, BitmapDrawable bitmap) {
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
        final long imageId = imageMessage.imageId;
        final BitmapDrawable image = PictureUtils.getScaledDrawable(imageMessage.destWidth, imageMessage.destHeight, path, imageMessage.context);
        if (image.getBitmap() != null) {
            addBitmapToMemoryCache(imageId, image);
        }

        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (requestMap.get(token) == null || requestMap.get(token) != imageId) {
                    return;
                }
                requestMap.remove(token);
                mListener.onImageLoaded(token, image);
            }
        });
    }

    public void queueImage(Token token, long imageId) {
        requestMap.put(token, imageId);
        mHandler.obtainMessage(ThumbImageMessage.THUMB_IMAGE, token).sendToTarget();

    }

    public void clearQueue() {
        mHandler.removeMessages(ThumbImageMessage.THUMB_IMAGE);
        requestMap.clear();
    }

    @Override
    public boolean quit() {
        mMemoryCache.evictAll();
        mMemoryCache = null;
        return super.quit();
    }

    public void addBitmapToMemoryCache(Long key, BitmapDrawable bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public BitmapDrawable getBitmapFromMemCache(Long key) {
        return mMemoryCache.get(key);
    }


}
