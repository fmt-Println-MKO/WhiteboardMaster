package co.whiteboardmaster.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.diegocarloslima.byakugallery.lib.GalleryViewPager;

import co.whiteboardmaster.android.adapter.WhiteboardCursorFragmentPagerAdapter;
import co.whiteboardmaster.android.model.Whiteboard;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardDetailsActivity extends FragmentActivity {


//    private ViewPager mViewPager;
    private GalleryViewPager mViewPager;
    private WhiteboardDatabaseHelper.WhiteboardCursor mCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.viewpager_whiteboard_details);

        final GalleryViewPager mViewPager = (GalleryViewPager) findViewById(R.id.wm_whiteboard_details_viewpager);


//        mViewPager = new ViewPager(this);
//        mViewPager.setId(R.id.wm_whiteboard_detail_viewpager);
//        setContentView(mViewPager);

        int position = getIntent().getIntExtra(WhiteboardListFragment.WHITEBOARD_POSITION,0);

        FragmentManager fm = getSupportFragmentManager();
        WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(this);

        mCursor  = mHelper.queryWhiteboards();

        Whiteboard wb = mCursor.getWhiteboard(position);

        final WhiteboardCursorFragmentPagerAdapter adapter = new WhiteboardCursorFragmentPagerAdapter(this,fm, mCursor);

        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);
//        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(position,false);
    }



}
