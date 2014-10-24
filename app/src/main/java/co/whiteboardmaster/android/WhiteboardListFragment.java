package co.whiteboardmaster.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import co.whiteboardmaster.android.adapter.WhiteboardCursorAdapter;
import co.whiteboardmaster.android.model.ThumbImageMessage;
import co.whiteboardmaster.android.utils.ImageLoaderThread;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardListFragment extends Fragment {

    private static final String TAG = "WMWhiteboardListFragment";

    public static final String WHITEBOARD = "whiteboard";
    public static final String WHITEBOARD_POSITION = "whiteboard_position";

    private static final int REQUEST_NEW_WHITEBOARD = 1;

    private WhiteboardDatabaseHelper.WhiteboardCursor mCursor;
    private WhiteboardDatabaseHelper mHelper;

    private ImageLoaderThread<ThumbImageMessage> mImageLoaderThread;

    private WhiteboardCursorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_whiteboard_list, parent, false);

        GridView gridView = (GridView) v.findViewById(R.id.wm_whiteboard_grid_list);
        Log.i(TAG, "---- Whiteboardlist Fragment gridView created ");

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Intent i = new Intent(getActivity(), WhiteboardDetailsActivity.class);
                i.putExtra(WHITEBOARD_POSITION, position);
                startActivity(i);
            }
        });

        Log.i(TAG, "---- Whiteboardlist Fragment onCreateView ");

        if (mImageLoaderThread == null) {
            mImageLoaderThread = new ImageLoaderThread<ThumbImageMessage>(new Handler());
            mImageLoaderThread.setListener(new ImageLoaderThread.Listener<ThumbImageMessage>() {
                @Override
                public void onImageLoaded(ThumbImageMessage imageMessage, BitmapDrawable image) {
                    imageMessage.mProgressContainer.setVisibility(View.INVISIBLE);
                    imageMessage.imageView.setImageDrawable(image);
                }
            });
            mImageLoaderThread.start();
            mImageLoaderThread.getLooper();
            Log.i(TAG, "---- image loader thread startet");

        }

        if (mHelper == null) {
            mHelper = new WhiteboardDatabaseHelper(getActivity());
        }

        if (mCursor == null) {
            mCursor = mHelper.queryWhiteboards();
        }

        if (adapter == null) {
            adapter = new WhiteboardCursorAdapter(getActivity(), mCursor, mImageLoaderThread);

        }


        gridView.setAdapter(adapter);

        return v;
    }

    public static WhiteboardListFragment findOrCreateRetainFragment(FragmentManager fm) {
        WhiteboardListFragment fragment = (WhiteboardListFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new WhiteboardListFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment, TAG).commit();
            Log.i(TAG, "---- findOrCreateRetainFragment created new fragment");
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        Log.i(TAG, "----- Whiteboard List Fragment created ");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCursor.close();
        mImageLoaderThread.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mImageLoaderThread.clearQueue();
    }

    @Override
    public void onResume() {
        super.onResume();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_NEW_WHITEBOARD) {
            String filename = data.getStringExtra(CameraFragment.EXTRA_PHOTO_FILENAME);
            if (filename != null) {
                Log.i(TAG, "received filename: " + filename);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_whiteboard_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_whiteboard:

                PackageManager pm = getActivity().getPackageManager();
                boolean hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Camera.getNumberOfCameras() > 0);
                if (hasCamera) {
                    Intent i = new Intent(getActivity(), CameraActivity.class);
                    startActivityForResult(i, 1);
                } else {
                    Toast.makeText(getActivity(), R.string.no_cam, Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
