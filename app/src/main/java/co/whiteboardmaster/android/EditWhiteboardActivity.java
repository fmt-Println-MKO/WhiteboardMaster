package co.whiteboardmaster.android;

import android.support.v4.app.Fragment;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class EditWhiteboardActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        String path = (String)getIntent().getSerializableExtra(CameraFragment.EXTRA_PHOTO_FILENAME);
        return EditWhiteboardFragment.newInstance(path);
    }
}