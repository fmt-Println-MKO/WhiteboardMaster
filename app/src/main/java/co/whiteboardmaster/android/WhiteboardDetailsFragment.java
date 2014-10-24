package co.whiteboardmaster.android;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;

import co.whiteboardmaster.android.model.Whiteboard;
import co.whiteboardmaster.android.utils.PictureUtils;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;
//import co.whiteboardmaster.android.view.TouchImageView;
//import it.sephiroth.android.library.imagezoom.ImageViewTouch;
//import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

//import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardDetailsFragment extends Fragment {

    private static final String TAG = "WMWhiteboardDetailsFragment";

    public static final String SERVER_BASE_URL = "http://whiteboardmaster.pixelbauhaus.com";
    public static final String SERVER_API_URL = SERVER_BASE_URL + "/api/whiteboard";
    public static final String SERVER_SHARE_URL = SERVER_BASE_URL + "/w/";

    private static final int REQUEST_NEW_WHITEBOARD = 1;

//    private TextView title;
//    private TextView description;

    private Whiteboard wb;

//    private ImageViewTouch mImageView;
//    private TouchImageView mImageView;

//    private WhiteboardDatabaseHelper.WhiteboardCursor mCursor;

    private View mProgressContainer;
    private ProgressBar mProgressBar;

    private Handler mHandler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_whiteboard_detail, parent, false);

//        mImageView = (ImageViewTouch) v.findViewById(R.id.wm_whiteboard_detail_image);
        final TouchImageView mImageView = (TouchImageView) v.findViewById(R.id.wm_whiteboard_detail_image);

//        final TouchImageView image = (TouchImageView) findViewById(R.id.touch_image_view_sample_image);

        final TextView title = (TextView) v.findViewById(R.id.wm_whiteboard_detail_title);
        final TextView description = (TextView) v.findViewById(R.id.wm_whiteboard_detail_description);
        mProgressContainer = v.findViewById(R.id.wm_whiteboard_detail_progress_container);
        mProgressContainer.setVisibility(View.INVISIBLE);


        mProgressBar = (ProgressBar) v.findViewById(R.id.wm_whiteboard_detail_progress_bar);
        mProgressBar.setMax(100);

        wb = (Whiteboard) getArguments().getSerializable(WhiteboardListFragment.WHITEBOARD);

        Log.d(TAG,"---- WhiteboardDetailsFragment onCreateView: " + wb);

        title.setText(wb.getTitle());
        description.setText(wb.getDescription());


        final ProgressBar progress = (ProgressBar) v.findViewById(R.id.wm_whiteboard_detail_image_progress);

        try {
            FileInputStream is = new FileInputStream(PictureUtils.getPathToFile(getActivity(),wb.getImageFileName()));
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

        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static WhiteboardDetailsFragment newInstance(Whiteboard wb) {
        Bundle args = new Bundle();
        args.putSerializable(WhiteboardListFragment.WHITEBOARD, wb);
        WhiteboardDetailsFragment fragment = new WhiteboardDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        mHandler = new Handler(getActivity().getMainLooper());
        Log.d(TAG,"---- WhiteboardDetailsFragment onCreate: " + wb);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"---- WhiteboardDetailsFragment onResume: " + wb);

//        mImageView.setImageDrawable(null);

//        wb = (Whiteboard) getArguments().getSerializable(WhiteboardListFragment.WHITEBOARD);
//
//        title.setText(wb.getTitle());
//        description.setText(wb.getDescription());
//
////        BitmapDrawable image = PictureUtils.getScaledDrawable(getActivity(), wb.getImageFileName());
//////        mImageView.setImageDrawable(image, null, -1, 8f);
////        mImageView.setImageDrawable(image);
//
//        final InputStream is;
//        try {
//            is = new FileInputStream(wb.getImageFileName());
//            final Drawable placeHolder = getResources().getDrawable(R.drawable.android_placeholder);
//            TileBitmapDrawable.attachTileBitmapDrawable(mImageView, is, placeHolder, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"---- WhiteboardDetailsFragment onPause: " + wb);
//        System.out.println("-------- pause");
        //PictureUtils.cleanImageView(mImageView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"---- WhiteboardDetailsFragment onDestroy: " + wb);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_whiteboard_details_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_whiteboard:

                Log.i(TAG, "nothing ");
                WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(getActivity());
                boolean deleted = mHelper.deleteWhiteboard(wb.getId());
                Intent i = new Intent(getActivity(), WhiteboardListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;


            case R.id.action_share_whiteboard:
                Log.i(TAG, "sharing Whiteboard ");
                try {
                    new ShareWhiteboardTask().execute();
                } catch (Exception e) {
                    Log.e(TAG, "error during share", e);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class ShareWhiteboardTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... args) {
            try {
                mHandler.post(new Runnable() {
                    public void run() {
                        mProgressContainer.setVisibility(View.VISIBLE);
                    }
                });
                postFile(PictureUtils.getPathToFile(getActivity(),wb.getImageFileName()), wb.getTitle(), wb.getDescription(), wb.getCreated());
            } catch (Exception e) {
                Log.e(TAG, "error during share: " + e.getMessage(), e);

            }
            return null;
        }

        public void postFile(String path, String title, String description, long created) throws Exception {

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(SERVER_API_URL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            final File file = new File(path);
            FileBody fb = new FileBody(file, ContentType.create("image/jpeg"), "Image");


            builder.addPart("Image", fb);
            builder.addTextBody("title", title);
            builder.addTextBody("description", description);
            builder.addTextBody("created", String.valueOf(created));
            final HttpEntity wbEntity = builder.build();

            class ProgressiveEntity implements HttpEntity {
                @Override
                public void consumeContent() throws IOException {
                    wbEntity.consumeContent();
                }

                @Override
                public InputStream getContent() throws IOException,
                        IllegalStateException {
                    return wbEntity.getContent();
                }

                @Override
                public Header getContentEncoding() {
                    return wbEntity.getContentEncoding();
                }

                @Override
                public long getContentLength() {
                    return wbEntity.getContentLength();
                }

                @Override
                public Header getContentType() {
                    return wbEntity.getContentType();
                }

                @Override
                public boolean isChunked() {
                    return wbEntity.isChunked();
                }

                @Override
                public boolean isRepeatable() {
                    return wbEntity.isRepeatable();
                }

                @Override
                public boolean isStreaming() {
                    return wbEntity.isStreaming();
                }

                @Override
                public void writeTo(OutputStream outstream) throws IOException {

                    class ProgressiveOutputStream extends FilterOutputStream {

                        private long size;
                        private long written;

                        int progress;

                        public ProgressiveOutputStream(OutputStream proxy) {
                            super(proxy);
                            size = wbEntity.getContentLength();
                        }

                        public void write(byte[] bts, int st, int end) throws IOException {
                            written += end;
                            out.write(bts, st, end);

                            progress = Math.round((float) written / (float) size * 100);
                            mHandler.post(new Runnable() {
                                public void run() {
                                    mProgressBar.setProgress(progress);
                                }
                            });

                        }
                    }

                    wbEntity.writeTo(new ProgressiveOutputStream(outstream));
                }

            }

            ProgressiveEntity myEntity = new ProgressiveEntity();

            post.setEntity(myEntity);
            HttpResponse response = client.execute(post);

            getContent(response);

        }

        public void getContent(HttpResponse response) throws IOException, JSONException {

            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String body = "";
                String content = "";

                while ((body = rd.readLine()) != null) {
                    content += body + "\n";
                }

                JSONObject jObject = new JSONObject(content);
                String guid = jObject.getString("guid");
                if (guid != null) {

                    WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(getActivity());
                    Whiteboard whiteboard = new Whiteboard.WhiteBoardBuilder(wb).setGuid(guid).build();
                    boolean updated = mHelper.updateWhiteBoard(whiteboard);
                    Log.d(TAG, "whiteboard updated: " + updated);

                    StringBuilder sb = new StringBuilder("Whiteboard Master");
                    sb.append("\n");
                    sb.append(wb.getTitle());
                    sb.append("\n");
                    sb.append(wb.getDescription());
                    sb.append("\n");
                    sb.append(new Date(wb.getCreated()));
                    sb.append("\n");
                    sb.append("\n");
                    sb.append(SERVER_SHARE_URL);
                    sb.append(guid);

                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("message/rfc822");
                    share.putExtra(Intent.EXTRA_SUBJECT, wb.getTitle() + " : Whiteboard Master");
                    share.putExtra(Intent.EXTRA_TEXT, sb.toString());
                    startActivity(Intent.createChooser(share, "Choose how to share this Whiteboard"));
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressContainer.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {

                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressContainer.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), R.string.share_error, Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                mHandler.post(new Runnable() {
                    public void run() {
                        mProgressContainer.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), R.string.share_error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
