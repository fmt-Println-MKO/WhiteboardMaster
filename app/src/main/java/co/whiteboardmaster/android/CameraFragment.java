package co.whiteboardmaster.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jni.bitmap_operations.JniBitmapHolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class CameraFragment extends Fragment {
    private static final String TAG = "WMCameraFragment";

    public static final String EXTRA_PHOTO_FILENAME = "co.whiteboardmaster.android.whiteboardintent.filename";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;

    private int rotation;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJPegCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "WhiteboardMaster");
            File mediaStorageDir = getActivity().getDir("whiteboardimages", Context.MODE_PRIVATE);
//            This location works best if you want the created images to be shared
//            between applications and persist after your app has been uninstalled.

//            Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("WhiteboardMaster", "failed to create directory");
                    return;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";
            File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

            boolean success = true;
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);

                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
//                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
//


//                Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
//                final int width=bitmap.getWidth(),height=bitmap.getHeight();
//                store the bitmap in the JNI "world"
                final JniBitmapHolder bitmapHolder = new JniBitmapHolder(bitmap);
                // no need for the bitmap on the java "world", since the operations are done on the JNI "world"
                bitmap.recycle();
                // crop a center square from the bitmap, from (0.25,0.25) to (0.75,0.75) of the bitmap.
//                bitmapHolder.cropBitmap(width/4,height/4,width*3/4,height*3/4);
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
                bitmap = bitmapHolder.getBitmapAndFree();


//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                //fos.write(data);
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
            }
            if (success) {
                Log.i(TAG, "Whiteboard saved at: " + file.getAbsolutePath());
//                Log.i(TAG, "Whiteboard saved at: " + fileName);

                Intent i = new Intent(getActivity(), EditWhiteboardActivity.class);
                i.putExtra(EXTRA_PHOTO_FILENAME, file.getAbsolutePath());
//                i.putExtra(EXTRA_PHOTO_FILENAME, fileName);
                mProgressContainer.setVisibility(View.INVISIBLE);
                startActivity(i);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, parent, false);

        mProgressContainer = v.findViewById(R.id.wm_camera_progress_container);
        mProgressContainer.setVisibility(View.INVISIBLE);

        ImageButton takePictureButton = (ImageButton) v.findViewById(R.id.wm_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.takePicture(mShutterCallback, null, mJPegCallback);
                }
            }
        });

        mSurfaceView = (SurfaceView) v.findViewById(R.id.wm_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error setting up preview display", e);
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera == null) return;
                Parameters parameters = mCamera.getParameters();
                Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes());
                parameters.setPreviewSize(s.width, s.height);

                parameters.setPictureSize(s.width, s.height);
                rotation = setCameraDisplayOrientation(getActivity(), mCamera);

                mCamera.setParameters(setColorEffect(parameters));
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        });

        return v;
    }


    private Size getBestSupportedSize(List<Size> sizes) {
        Size bestSize = null;
        int largestArea = 0;

        for (Size size : sizes) {
            int area = size.width * size.height;
            if (area > largestArea) {
                bestSize = size;
                largestArea = area;
            }
        }
        return bestSize;
    }

    private Parameters setColorEffect(Parameters parameters) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<String> supportedColorEffects = parameters.getSupportedColorEffects();
            if (supportedColorEffects != null) {
                if (supportedColorEffects.contains(Parameters.EFFECT_NEGATIVE)) {
                    parameters.setColorEffect(Parameters.EFFECT_NEGATIVE);
                }
            }
        }
        return parameters;
    }

    public static int setCameraDisplayOrientation(Activity activity, Camera camera) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        result = (info.orientation - degrees + 360) % 360;
        camera.setDisplayOrientation(result);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


}
