package co.whiteboardmaster.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fakeawt.Rectangle;
import magick.CompressionType;
import magick.ImageInfo;
import magick.MagickImage;

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
        int destWidth = getPixelFromDP(destDPWidth, context);
        int destHeight = getPixelFromDP(destDPHeight, context);

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

    public static void cleanImageView(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return;
        }
        BitmapDrawable b = (BitmapDrawable) imageView.getDrawable();
        b.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }


    public static File getStorageDir(Context context) {
        return context.getDir("whiteboardimages", Context.MODE_PRIVATE);
    }

    public static String getPathToFile(Context context, String fileName) {
        return getStorageDir(context).getPath() + File.separator + fileName;
    }

    public static Map<PictureType, String> storeBitmap(byte[] data, int rotation, Context context) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        String thumbFileName = "THUMB_" + timeStamp + ".jpg";
        File thumbFile = new File(getPathToFile(context, thumbFileName));

        boolean success = true;
        BufferedOutputStream tbos = null;
        try {
            tbos = new BufferedOutputStream(new FileOutputStream(thumbFile));

            Log.d(TAG, "--store image: bytes: " + data.length);

            ImageInfo info = new ImageInfo(getPathToFile(context, fileName));
            info.setMagick("jpeg");
            MagickImage img = new MagickImage(info, data);
            img.setCompression(CompressionType.JPEGCompression);
            img.setCompression(100);

            //rotate the bitmap:
            switch (rotation) {
                case (90):
                    img = img.rotateImage(90);
                    break;
                case (180):
                    img = img.rotateImage(180);
                    break;
                case (270):
                    img = img.rotateImage(270);
                    break;
            }
            boolean written = img.writeImage(info);
            Log.d(TAG, "----image written: : -- " + written);
            if (!written) {
                throw new Exception("image could not be written");
            }

            float height = img.getHeight();
            float width = img.getWidth();

            Log.d(TAG, "----stroing image: " + height + " * " + width);

            boolean isPortrait = height > width;

            int scaleHeight = 100;
            int scaleWidth = 100;
            if (isPortrait) {
                float format = height / width;
                Log.d(TAG, "----stroing scale h format: " + format);
                scaleHeight = Math.round(scaleHeight * format);

            } else {
                float format = width / height;
                Log.d(TAG, "----stroing scale w format: " + format);
                scaleWidth = Math.round(scaleWidth * format);
            }

            Log.d(TAG, "----stroing scale thumb image: " + scaleHeight + " * " + scaleWidth);

            int thumbHeight = getPixelFromDP(scaleHeight, context);
            int thumbWidth = getPixelFromDP(scaleWidth, context);

            int height10Perctent = thumbHeight * 10 / 100;
            int width10Perctent = thumbWidth * 10 / 100;

            img = img.scaleImage(thumbWidth + (width10Perctent * 2), thumbHeight + (height10Perctent * 2));

            // crop a center square from the bitmap, from (0.25,0.25) to (0.75,0.75) of the bitmap.
            if (isPortrait) {
                int top = (thumbHeight - thumbWidth) / 2 + height10Perctent;
                img = img.cropImage(new Rectangle(width10Perctent, top, thumbWidth, thumbWidth));
            } else {
                int left = (thumbWidth - thumbHeight) / 2 + width10Perctent;
                img = img.cropImage(new Rectangle(left, height10Perctent, thumbHeight, thumbHeight));
            }
            Log.d(TAG, "---written thumb image ");
            ImageInfo thumbInfo = new ImageInfo(thumbFile.getAbsolutePath());
            thumbInfo.setMagick("jpeg");

            byte[] thumbImage = img.imageToBlob(thumbInfo);
            tbos.write(thumbImage);

        } catch (Exception e) {
            Log.e(TAG, "error writing to file.: " + fileName, e);
            success = false;
        } finally {
            try {
                if (tbos != null) {
                    tbos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "error closing file: " + thumbFileName);
                success = false;
            }
        }
        if (success) {
            Log.i(TAG, "Whiteboard saved at: " + thumbFile.getAbsolutePath());
            Map<PictureType, String> pictures = new HashMap<PictureType, String>();
            pictures.put(PictureType.THUMBNAIL, thumbFileName);
            pictures.put(PictureType.IMAGE, fileName);

            return pictures;

        } else {
            return null;
        }
    }

    public static void removeImagesFiles(Context context, List<String> files){
        for (String fileName : files) {
            File file = new File(getPathToFile(context,fileName));
            file.delete();
        }
    }

    private static int getPixelFromDP(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
