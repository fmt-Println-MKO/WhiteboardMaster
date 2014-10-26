package co.whiteboardmaster.android;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import co.whiteboardmaster.android.loader.WhiteboardCursorLoader;
import co.whiteboardmaster.android.model.ThumbImageMessage;
import co.whiteboardmaster.android.utils.ImageLoaderThread;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "WMWhiteboardListFragment";

    public static final String WHITEBOARD = "co.whiteboardmaster.android.whiteboard";
    public static final String WHITEBOARD_POSITION = "co.whiteboardmaster.android.whiteboard_position";
    public static final String WHITEBOARD_DATA_CHANGED = "co.whiteboardmaster.android.whiteboard_data_changed";

    private ImageLoaderThread<ThumbImageMessage> mImageLoaderThread;

    private GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_whiteboard_list, parent, false);

        gridView = (GridView) v.findViewById(R.id.wm_whiteboard_grid_list);
//        Log.i(TAG, "---- Whiteboardlist Fragment gridView created ");

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Intent i = new Intent(getActivity(), WhiteboardDetailsActivity.class);
                i.putExtra(WHITEBOARD_POSITION, position);
                startActivity(i);
            }
        });

//        Log.i(TAG, "---- Whiteboardlist Fragment onCreateView ");

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
            Log.i(TAG, "ImageLoaderThread started");

        }

        getLoaderManager().initLoader(0, null, this);
        return v;
    }

    public static WhiteboardListFragment findOrCreateRetainFragment(FragmentManager fm) {
        WhiteboardListFragment fragment = (WhiteboardListFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new WhiteboardListFragment();
            fragment.setArguments(new Bundle());
            fm.beginTransaction().add(R.id.fragmentContainer, fragment, TAG).commit();
//            Log.i(TAG, "---- findOrCreateRetainFragment created new fragment");
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
//        Log.i(TAG, "----- Whiteboard List Fragment created ");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        if (getArguments() != null) {
            boolean dataChanged = getArguments().getBoolean(WHITEBOARD_DATA_CHANGED);

            if (dataChanged) {
                getLoaderManager().restartLoader(0, null, this);
//                Log.d(TAG, "--- data changed");
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
                    startActivity(new Intent(getActivity(), CameraActivity.class));
                } else {
                    Toast.makeText(getActivity(), R.string.no_cam, Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new WhiteboardCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        WhiteboardCursorAdapter adapter = new WhiteboardCursorAdapter(getActivity(), (WhiteboardDatabaseHelper.WhiteboardCursor) cursor, mImageLoaderThread);
        gridView.setAdapter(adapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        gridView.setAdapter(null);
    }
}
