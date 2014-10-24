package co.whiteboardmaster.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.jni.bitmap_operations.JniBitmapHolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import co.whiteboardmaster.android.EditWhiteboardActivity;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class PictureUtils {

    private static final String TAG = "PictureUtils";

    public enum PictureType {
        THUMBNAIL,
        IMAGE;
    }

    public static BitmapDrawable getScaledDrawable(int destDPWidth, int destDPHeight, String path, Context context) {
        int destWidth = getPixelFromDP(destDPWidth,context);
        int destHeight = getPixelFromDP(destDPHeight,context);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            Log.e("PictureUtils", "Image not found", e);
        }

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

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
        return new BitmapDrawable(context.getResources(), bitmap);
    }

//    private static int dpToPx(int dp, Resources r) {
//        DisplayMetrics displayMetrics = r.getDisplayMetrics();
//        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        return px;
//    }

    public static void cleanImageView(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return;
        }
        BitmapDrawable b = (BitmapDrawable) imageView.getDrawable();
        b.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }


    public static File getStorageDir(Context context) {
        return  context.getDir("whiteboardimages", Context.MODE_PRIVATE);
    }

    public static String getPathToFile(Context context, String fileName) {
        return getStorageDir(context).getPath() + File.separator + fileName;
    }

    public static Map<PictureType,String> storeBitmap(byte[] data, int rotation, Context context) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        String thumbFileName = "THUMB_" + timeStamp + ".jpg";
        File file = new File(getPathToFile(context,fileName));
        File thmubFile = new File(getPathToFile(context,thumbFileName));

        boolean success = true;
        FileOutputStream fos = null;
        FileOutputStream tfos = null;
        try {
            fos = new FileOutputStream(file);
            tfos = new FileOutputStream(thmubFile);

            Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data));

            final JniBitmapHolder bitmapHolder = new JniBitmapHolder(bitmap);
            bitmap.recycle();
            bitmap = null;
            //rotate the bitmap:
            switch (rotation) {
                case (90):
                    bitmapHolder.rotateBitmapCw90();
                    break;
                case (180):
                    bitmapHolder.rotateBitmap180();
                    break;
                case (270):
                    bitmapHolder.rotateBitmapCcw90();

                    bitmapHolder.rotateBitmapCcw90();
                    break;
            }

            //get the output java bitmap , and free the one on the JNI "world"
            bitmap = bitmapHolder.getBitmap();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            float height = bitmap.getHeight();
            float width = bitmap.getWidth();

            Log.d(TAG,"----stroing image: " + height +" * " + width);


            bitmap.recycle();

            boolean isPortrait = height > width;

            int scaleHeight = 100;
            int scaleWidth = 100;
            if (isPortrait) {
                float format = height / width;
                Log.d(TAG,"----stroing scale h format: " +format);
//                Log.d(TAG,"----stroing scale h format scaled: " +(scaleHeight *format));
                scaleHeight = Math.round(scaleHeight * format);

            } else {
                float format = width / height;
                Log.d(TAG,"----stroing scale w format: " +format);
//                Log.d(TAG,"----stroing scale w format scaled: " +(scaleWidth *format));
                scaleWidth = Math.round(scaleWidth * format);
            }

            Log.d(TAG,"----stroing scale thumb image: " + scaleHeight +" * " + scaleWidth);

            int thumbHeight = getPixelFromDP(scaleHeight,context);
            int thumbWidth = getPixelFromDP(scaleWidth,context);

            int height10Perctent = thumbHeight * 10 / 100;
            int width10Perctent = thumbWidth * 10 / 100;

            bitmapHolder.scaleBitmap(thumbWidth + (width10Perctent *2 ), thumbHeight + (height10Perctent * 2), JniBitmapHolder.ScaleMethod.NearestNeighbour);
            // crop a center square from the bitmap, from (0.25,0.25) to (0.75,0.75) of the bitmap.
            if ( isPortrait) {
               int left = width10Perctent;
                int top = (thumbHeight - thumbWidth ) / 2 + height10Perctent;
                int right = thumbWidth + width10Perctent;
                int bottom = (thumbHeight - thumbWidth ) / 2 + height10Perctent + thumbWidth;

                bitmapHolder.cropBitmap(left,top,right,bottom);
            } else {
                int left = (thumbWidth - thumbHeight) / 2 + width10Perctent;
                int top =  height10Perctent;
                int right = (thumbWidth - thumbHeight) / 2 + width10Perctent + thumbHeight;
                int bottom = thumbHeight + height10Perctent;

                bitmapHolder.cropBitmap(left,top,right,bottom);
            }


            Bitmap thumbBitmap = bitmapHolder.getBitmapAndFree();
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG,85,tfos);

            Log.d(TAG,"----stroing thumb image: " + thumbBitmap.getHeight() +" * " + thumbBitmap.getWidth());
            thumbBitmap.recycle();
            thumbBitmap = null;

        } catch (Exception e) {
            Log.e(TAG, "error writing to file.: " + fileName, e);
            success = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "error closing file: " + fileName);
                success = false;
            }
            try {
                if (tfos != null) {
                    tfos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "error closing file: " + thumbFileName);
                success = false;
            }
        }
        if (success) {
            Log.i(TAG, "Whiteboard saved at: " + file.getAbsolutePath());
            Map<PictureType,String> pictures = new HashMap<PictureType, String>();
            pictures.put(PictureType.THUMBNAIL,thumbFileName);
            pictures.put(PictureType.IMAGE,fileName);

            return pictures;

        } else {
            return null;
        }
    }

    private static int getPixelFromDP(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}
