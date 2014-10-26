package co.whiteboardmaster.android;

import android.support.v4.app.Fragment;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class EditWhiteboardActivity extends SingleFragmentActivity {

    private EditWhiteboardFragment fragment;

    @Override
    protected Fragment createFragment() {
        String imageFileName = (String) getIntent().getSerializableExtra(CameraFragment.EXTRA_PHOTO_FILENAME);
        String thumbFileName = (String) getIntent().getSerializableExtra(CameraFragment.EXTRA_THUMB_PHOTO_FILENAME);
        fragment = EditWhiteboardFragment.newInstance(imageFileName, thumbFileName);
        return fragment;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fragment.onBackPressed();
    }
}
