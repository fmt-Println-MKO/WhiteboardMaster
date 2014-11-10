package co.whiteboardmaster.android;

import android.app.Activity;
import android.content.Intent;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import co.whiteboardmaster.android.utils.PictureUtils;
import go.mygodroid.Mygodroid;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class CameraFragment extends Fragment {
    private static final String TAG = "WMCameraFragment";

    public static final String EXTRA_PHOTO_FILENAME = "co.whiteboardmaster.android.whiteboardintent.filename";
    public static final String EXTRA_THUMB_PHOTO_FILENAME = "co.whiteboardmaster.android.whiteboardintent.thumbfilename";

    private Camera mCamera;
    private View mProgressContainer;

    private int rotation;
    private ImageButton takePictureButton;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJPegCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Map<PictureUtils.PictureType, String> pictures = PictureUtils.storeBitmap(data, rotation, getActivity());


           // try {
           //     Mygodroid.SayHelloGo(pictures.get(PictureUtils.PictureType.IMAGE));
           //     Mygodroid.StoreImage(pictures.get(PictureUtils.PictureType.IMAGE), "/sdcard/data/gotest/" + System.currentTimeMillis() + ".jpg");
           // } catch (Exception e) {
           //     Log.e(TAG, "some strange GO error happens: ", e);
           // }

            if (pictures != null) {

                Intent i = new Intent(getActivity(), EditWhiteboardActivity.class);
                i.putExtra(EXTRA_PHOTO_FILENAME, pictures.get(PictureUtils.PictureType.IMAGE));
                i.putExtra(EXTRA_THUMB_PHOTO_FILENAME, pictures.get(PictureUtils.PictureType.THUMBNAIL));
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

        takePictureButton = (ImageButton) v.findViewById(R.id.wm_camera_takePictureButton);
//        takePictureButton.setEnabled(false);
        takePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {

                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            mCamera.takePicture(mShutterCallback, null, mJPegCallback);
                        }
                    });

                }
            }
        });

        final SurfaceView mSurfaceView = (SurfaceView) v.findViewById(R.id.wm_camera_surfaceView);
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
                Size ps = getBestSupportedSize(parameters.getSupportedPreviewSizes());
                parameters.setPreviewSize(ps.width, ps.height);


                Size s = getBestSupportedSize(parameters.getSupportedPictureSizes());
                parameters.setPictureSize(s.width, s.height);

                Log.d(TAG, "-----taking image: " + s.width + " * " + s.height);
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
        List<String> supportedColorEffects = parameters.getSupportedColorEffects();
        if (supportedColorEffects != null) {
            if (supportedColorEffects.contains(Parameters.EFFECT_NEGATIVE)) {
                parameters.setColorEffect(Parameters.EFFECT_NEGATIVE);
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
