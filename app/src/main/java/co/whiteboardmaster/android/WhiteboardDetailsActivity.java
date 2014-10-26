package co.whiteboardmaster.android;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;

import com.diegocarloslima.byakugallery.lib.GalleryViewPager;

import java.util.Date;

import co.whiteboardmaster.android.adapter.WhiteboardCursorFragmentPagerAdapter;
import co.whiteboardmaster.android.loader.WhiteboardCursorLoader;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

//import co.whiteboardmaster.android.model.Whiteboard;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardDetailsActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "WhiteboardDetailsActivity";


    private GalleryViewPager mViewPager;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewpager_whiteboard_details);

        mViewPager = (GalleryViewPager) findViewById(R.id.wm_whiteboard_details_viewpager);

        position = getIntent().getIntExtra(WhiteboardListFragment.WHITEBOARD_POSITION, 0);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new WhiteboardCursorLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        Log.d(TAG, "onLoadFinished");
        FragmentManager fm = getSupportFragmentManager();
        WhiteboardDatabaseHelper.WhiteboardCursor mCursor = (WhiteboardDatabaseHelper.WhiteboardCursor) cursor;
        final WhiteboardCursorFragmentPagerAdapter adapter = new WhiteboardCursorFragmentPagerAdapter(this, fm, mCursor);

        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setCurrentItem(position, false);

        String title = adapter.getPageTitle(position);
        long created = adapter.getItemcreated(position);
        updateTitle(title,created);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                String title = adapter.getPageTitle(position);
                long created = adapter.getItemcreated(position);
                updateTitle(title,created);
            }

            @Override
            public void onPageScrolled(int position, float offset, int offsetPixel) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateTitle(String title, long created ) {
        getActionBar().setTitle(title);
        Context context = getApplicationContext();
        Date dt = new Date(created);
        String date = DateFormat.getDateFormat(context).format(dt);
        String time = DateFormat.getTimeFormat(context).format(dt);
        getActionBar().setSubtitle(date + " - " + time);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//        mViewPager.setAdapter(null);
    }
}
