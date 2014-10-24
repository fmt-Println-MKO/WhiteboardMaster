package co.whiteboardmaster.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import co.whiteboardmaster.android.model.Whiteboard;
import co.whiteboardmaster.android.utils.PictureUtils;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class EditWhiteboardFragment extends Fragment {

    private static final String TAG = "WMEditWhiteboardFragment";

    private static final int REQUEST_NEW_WHITEBOARD = 1;

    private EditText title;
    private EditText description;
    private TouchImageView mImageView;
    private String imageFilename;
    private String thumbFileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_whiteboard, parent, false);

        mImageView = (TouchImageView) v.findViewById(R.id.wm_edit_whiteboard_image);
        title = (EditText) v.findViewById(R.id.wm_edit_whiteboard_title);
        description = (EditText) v.findViewById(R.id.wm_edit_whiteboard_description);

        imageFilename = (String) getArguments().getSerializable(CameraFragment.EXTRA_PHOTO_FILENAME);
        thumbFileName = (String) getArguments().getSerializable(CameraFragment.EXTRA_THUMB_PHOTO_FILENAME);


        final ProgressBar progress = (ProgressBar) v.findViewById(R.id.wm_edit_whiteboard_image_progress);

        try {
            FileInputStream is = new FileInputStream(PictureUtils.getPathToFile(getActivity(),imageFilename));
            TileBitmapDrawable.attachTileBitmapDrawable(mImageView, is, null, new TileBitmapDrawable.OnInitializeListener() {

                @Override
                public void onStartInitialization() {
                    progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEndInitialization() {
                    progress.setVisibility(View.GONE);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Button safeWhiteboard = (Button) v.findViewById(R.id.wm_edit_whiteboard_save_button);

        safeWhiteboard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(getActivity());

                Whiteboard.WhiteBoardBuilder wb = new Whiteboard.WhiteBoardBuilder()
                        .setDescription(description.getText().toString())
                        .setTitle(title.getText().toString())
                        .setImageFileName(imageFilename)
                        .setThumbFileName(thumbFileName)
                        .setCreated(System.currentTimeMillis())
                        .setUpdated(System.currentTimeMillis());
                Whiteboard whiteboard = wb.build();
                long count = mHelper.insertWhiteboard(whiteboard);
                Log.i(TAG, "insert new whiteboard: " + count);
                Intent i = new Intent(getActivity(), WhiteboardListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
//        BitmapDrawable image = PictureUtils.getScaledDrawable(getActivity(), imageFilename);
//        mImageView.setImageDrawable(image);
    }

    public static EditWhiteboardFragment newInstance(String imageFilename, String thumbFileName) {
        Bundle args = new Bundle();
        args.putSerializable(CameraFragment.EXTRA_PHOTO_FILENAME, imageFilename);
        args.putSerializable(CameraFragment.EXTRA_THUMB_PHOTO_FILENAME, thumbFileName);

        EditWhiteboardFragment fragment = new EditWhiteboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        PictureUtils.cleanImageView(mImageView);
    }

}
