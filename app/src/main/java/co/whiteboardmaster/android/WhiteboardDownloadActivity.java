package co.whiteboardmaster.android;

import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by matthiaskoch on 17.10.14.
 */
public class WhiteboardDownloadActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {

        Intent intent = getIntent();
        String data = intent.getDataString();

        return WhiteboardDownloadFragment.newInstance(data);
    }
}
