package co.whiteboardmaster.android.utils;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.whiteboardmaster.android.model.ThumbImageMessage;

/**
 * Created by matthiaskoch on 20.10.14.
 */
public class ImageLoaderThread<Token> extends HandlerThread {

    private static final String TAG = "ImageLoaderThread";

    Handler mHandler;
    private Map<Token,Integer> requestMap = Collections.synchronizedMap(new HashMap<Token, Integer>());

    private Handler mResponseHandler;
    private Listener<Token> mListener;

    public interface Listener<Token> {
        void onImageLoaded(Token token, BitmapDrawable thumbnail);
    }

    public ImageLoaderThread(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
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
//        Log.i(TAG, "handle image loading: ");
        ThumbImageMessage imageMessage = (ThumbImageMessage) token;
        final String path = imageMessage.path;
        final int imageId = imageMessage.imageId;
        final BitmapDrawable image = PictureUtils.getScaledDrawable(imageMessage.destWidth, imageMessage.destHeight, path, imageMessage.resources);
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
//                System.out.println("--- " + requestMap);
//                System.out.println("--- " + token);
//                System.out.println("--- " + imageId);
//                System.out.println("--- " + requestMap.get(token));
                if (requestMap.get(token) == null || requestMap.get(token).intValue()  != imageId) {
                    image.getBitmap().recycle();
                    return;
                }
                requestMap.remove(token);
                mListener.onImageLoaded(token, image);
            }
        });
    }

    public void queueImage(Token token,int imageId) {
        requestMap.put(token,new Integer(imageId));
        mHandler.obtainMessage(ThumbImageMessage.THUMB_IMAGE, token).sendToTarget();

    }

    public void clearQueue() {
        mHandler.removeMessages(ThumbImageMessage.THUMB_IMAGE);
        requestMap.clear();
    }

}
