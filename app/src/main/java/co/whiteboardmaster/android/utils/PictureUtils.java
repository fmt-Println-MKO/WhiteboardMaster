package co.whiteboardmaster.android.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class PictureUtils {

    public static BitmapDrawable getScaledDrawable(Activity a, String path) {
        Display display = a.getWindowManager().getDefaultDisplay();
        int destWidth = display.getWidth();
        int destHeight = display.getHeight();

        return getScaledDrawable(destWidth, destHeight, path, a.getResources());
    }

    public static BitmapDrawable getScaledDrawable(int destDPWidth, int destDPHeight, String path, Resources r) {
        int destWidth = dpToPx(destDPWidth, r);
        int destHeight = dpToPx(destDPHeight, r);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            Log.e("PictureUtils", "Image not found", e);
        }

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
//        if (destHeight > GL10.GL_MAX_TEXTURE_SIZE) {
//            destHeight = GL10.GL_MAX_TEXTURE_SIZE;
//        }
//        if (destWidth > GL10.GL_MAX_TEXTURE_SIZE) {
//            destWidth = GL10.GL_MAX_TEXTURE_SIZE;
//        }


//        System.out.println("--------src size: " +srcHeight + " - " + srcWidth);
//        System.out.println("--------dest size: " +destHeight + " - " + destWidth);
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;




        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return new BitmapDrawable(r, bitmap);
    }

    private static int dpToPx(int dp, Resources r) {
        DisplayMetrics displayMetrics = r.getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static void cleanImageView(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return;
        }
        BitmapDrawable b = (BitmapDrawable) imageView.getDrawable();
        b.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }

}
