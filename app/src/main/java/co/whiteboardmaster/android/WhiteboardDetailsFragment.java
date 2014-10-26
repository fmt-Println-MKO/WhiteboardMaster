package co.whiteboardmaster.android;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
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

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.whiteboardmaster.android.model.Whiteboard;
import co.whiteboardmaster.android.utils.MultipartRequestBuilder;
import co.whiteboardmaster.android.utils.PictureUtils;
import co.whiteboardmaster.android.utils.StreamUtils;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 16.10.14.
 */
public class WhiteboardDetailsFragment extends Fragment {

    private static final String TAG = "WMWhiteboardDetailsFragment";

    public static final String SERVER_BASE_URL = "http://whiteboardmaster.pixelbauhaus.com";
    public static final String SERVER_API_URL = SERVER_BASE_URL + "/api/whiteboard";
    public static final String SERVER_SHARE_URL = SERVER_BASE_URL + "/w/";

    private static final int REQUEST_NEW_WHITEBOARD = 1;

    private Whiteboard wb;
    private View mProgressContainer;
    private ProgressBar mProgressBar;

    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_whiteboard_detail, parent, false);
        final TouchImageView mImageView = (TouchImageView) v.findViewById(R.id.wm_whiteboard_detail_image);

        final TextView description = (TextView) v.findViewById(R.id.wm_whiteboard_detail_description);
        mProgressContainer = v.findViewById(R.id.wm_whiteboard_detail_progress_container);
        mProgressContainer.setVisibility(View.INVISIBLE);


        mProgressBar = (ProgressBar) v.findViewById(R.id.wm_whiteboard_detail_progress_bar);
        mProgressBar.setMax(100);

        wb = (Whiteboard) getArguments().getSerializable(WhiteboardListFragment.WHITEBOARD);

        Log.d(TAG, "---- WhiteboardDetailsFragment onCreateView: " + wb);

        description.setText(wb.getDescription());

        final View progress = v.findViewById(R.id.wm_whiteboard_detail_image_progress_container);

        try {
            FileInputStream is = new FileInputStream(PictureUtils.getPathToFile(getActivity(), wb.getImageFileName()));
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

                Log.i(TAG, "deleting whiteboard ");

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.delete_whiteboard_dialog);
                // Add the buttons
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(getActivity());
                        boolean deleted = mHelper.deleteWhiteboard(wb.getId());
                        if (deleted) {
                            List<String> files = new ArrayList<String>(2);
                            files.add(wb.getImageFileName());
                            files.add(wb.getThumbFileName());
                            PictureUtils.removeImagesFiles(getActivity(), files);
                        }
                        Intent i = new Intent(getActivity(), WhiteboardListActivity.class);
                        i.putExtra(WhiteboardListFragment.WHITEBOARD_DATA_CHANGED, true);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();


                return true;


            case R.id.action_share_whiteboard:
                Log.i(TAG, "sharing Whiteboard ");
                try {
                    if (wb.getGuid() == null || wb.getGuid().isEmpty()) {
                        new ShareWhiteboardTask(getActivity()).execute(wb);
                    } else {
                        sendShareMail();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error during share", e);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class ShareWhiteboardTask extends AsyncTask<Whiteboard, Integer, String> {

        final String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";

        private PowerManager.WakeLock mWakeLock;
        private Context context;
        private String content;

        public ShareWhiteboardTask(Context context) {
            this.context = context;
        }

        protected String doInBackground(Whiteboard... whiteboards) {
            Whiteboard whiteboard = whiteboards[0];

            HttpURLConnection connection = null;
            DataOutputStream outputStream = null;
            InputStream inputStream = null;

            try {
                File file = new File(PictureUtils.getPathToFile(getActivity(), whiteboard.getImageFileName()));
                FileInputStream fileInputStream = new FileInputStream(file);

                MultipartRequestBuilder requestBuilder = new MultipartRequestBuilder(boundary);
                requestBuilder.addFilePart("Image", "Image", file);
                requestBuilder.addTextPart("title", whiteboard.getTitle());
                requestBuilder.addTextPart("description", whiteboard.getDescription());
                requestBuilder.addTextPart("created", String.valueOf(whiteboard.getCreated()));

                byte[] request = requestBuilder.getRequest();
                requestBuilder.reset();

                URL url = new URL(SERVER_API_URL);
                connection = (HttpURLConnection) url.openConnection();

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setFixedLengthStreamingMode(request.length);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                outputStream = new DataOutputStream(connection.getOutputStream());

                int bufferLength = 1024;
                for (int i = 0; i < request.length; i += bufferLength) {
                    int progress = (int) ((i / (float) request.length) * 100);
                    publishProgress(progress);
                    if (request.length - i >= bufferLength) {
                        outputStream.write(request, i, bufferLength);
                    } else {
                        outputStream.write(request, i, request.length - i);
                    }
                }
                publishProgress(100);

                inputStream = connection.getInputStream();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                content = StreamUtils.convertStreamToString(inputStream);

                fileInputStream.close();
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "error during sharing: ", e);
                e.printStackTrace();
                return e.toString();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            mProgressContainer.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressContainer.setVisibility(View.GONE);
            if (result != null) {
                Toast.makeText(getActivity(), R.string.share_error, Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject jObject = new JSONObject(content);
                    String guid = jObject.getString("guid");
                    if (guid != null) {

                        WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(getActivity());
                        wb = new Whiteboard.WhiteBoardBuilder(wb).setGuid(guid).build();
                        boolean updated = mHelper.updateWhiteBoard(wb);
                        Log.d(TAG, "whiteboard updated: " + updated);

                        sendShareMail();
                    } else {
                        Toast.makeText(getActivity(), R.string.share_error, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), R.string.share_error, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendShareMail() {

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
        sb.append(wb.getGuid());

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
    }
}
