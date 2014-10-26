package co.whiteboardmaster.android.model;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by matthiaskoch on 20.10.14.
 */
public class ThumbImageMessage {

    public static int THUMB_IMAGE = 1;

    public long imageId;
    public int destHeight;
    public int destWidth;
    public ImageView imageView;
    public View mProgressContainer;
    public Context context;
    public String path;

    @Override
    public String toString() {
        return "ThumbImageMessage{" +
                "imageId=" + imageId +
                ", imageView=" + imageView +
                '}';
    }
}
