package co.whiteboardmaster.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import go.Go;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardListActivity extends FragmentActivity {

    private static final String TAG = "WhiteboardListActivity";

    private WhiteboardListFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        fragment = WhiteboardListFragment.findOrCreateRetainFragment(getSupportFragmentManager());

        //Magic
        //Go.init(getApplicationContext());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean dataChanged = intent.getBooleanExtra(WhiteboardListFragment.WHITEBOARD_DATA_CHANGED, false);

        if (dataChanged) {
            Bundle args = fragment.getArguments();
            args.putBoolean(WhiteboardListFragment.WHITEBOARD_DATA_CHANGED, true);
        }

    }
}
