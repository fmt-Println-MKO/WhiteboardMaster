package co.whiteboardmaster.android;

import android.support.v4.app.Fragment;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new WhiteboardListFragment();
    }


}
