package co.whiteboardmaster.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardListActivity extends FragmentActivity {
//public class WhiteboardListActivity extends SingleFragmentActivity {
//
//    @Override
//    protected Fragment createFragment() {
//        return WhiteboardListFragment.findOrCreateRetainFragment(getSupportFragmentManager());
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        WhiteboardListFragment fragment = WhiteboardListFragment.findOrCreateRetainFragment(getSupportFragmentManager());

//        FragmentManager fm = getSupportFragmentManager();
//        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
//        if (fragment == null) {
//            fragment = createFragment();
//            fm.beginTransaction().add(R.id.fragmentContainer,fragment).commit();
//        }
    }


}
