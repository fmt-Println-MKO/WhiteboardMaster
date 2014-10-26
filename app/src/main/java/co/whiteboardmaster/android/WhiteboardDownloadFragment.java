package co.whiteboardmaster.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TouchImageView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import co.whiteboardmaster.android.model.Whiteboard;
import co.whiteboardmaster.android.utils.PictureUtils;
import co.whiteboardmaster.android.utils.StreamUtils;
import co.whiteboardmaster.android.utils.WhiteboardDatabaseHelper;

/**
 * Created by matthiaskoch on 17.10.14.
 */
public class WhiteboardDownloadFragment extends Fragment {


    private static final String TAG = "WMWhiteboardDownloadFragment";

    public static final String WHITEBOARD_DOWNLOAD_URL = "whiteboard.download.url";
    public static final String WHITEBOARD_GUID = "co.whiteboardmaster.android.guid";

    private TextView descriptionView;
    private TouchImageView mImageView;

    private ProgressDialog mProgressDialog;

    private ImageButton finishedButton;
    private  View progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_whiteboard_download, parent, false);

        mImageView = (TouchImageView) v.findViewById(R.id.wm_whiteboard_download_image);
        descriptionView = (TextView) v.findViewById(R.id.wm_whiteboard_download_description);
        finishedButton = (ImageButton) v.findViewById(R.id.wm_whiteboard_download_finished);
        finishedButton.setEnabled(false);

        progress = v.findViewById(R.id.wm_whiteboard_download_image_progress_container);

        final String previewUrl = (String) getArguments().getSerializable(WHITEBOARD_DOWNLOAD_URL);
        final String whiteboardUrl = WhiteboardDetailsFragment.SERVER_API_URL + previewUrl.substring(previewUrl.lastIndexOf("/"));

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.wb_downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);


        final DownloadTask downloadTask = new DownloadTask(getActivity());
        downloadTask.execute(whiteboardUrl);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });

        return v;
    }

    public static WhiteboardDownloadFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putSerializable(WHITEBOARD_DOWNLOAD_URL, url);

        WhiteboardDownloadFragment fragment = new WhiteboardDownloadFragment();
        fragment.setArguments(args);
        return fragment;
    }


    // usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        String guid;
        Long created;
        String description;
        String title;
        String imageFileName;
        String thumbFileName;

        Whiteboard wbExists;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            ByteArrayOutputStream output = null;
            HttpURLConnection connection = null;
            String content = "";
            URL url;

            try {
                url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                content = StreamUtils.convertStreamToString(connection.getInputStream());

            } catch (Exception e) {
                return e.toString();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            try {
                JSONObject jObject = new JSONObject(content);
                String imageUrl = jObject.getString("picture");
                guid = jObject.getString("guid");
                created = jObject.getLong("created");
                description = jObject.getString("description");
                title = jObject.getString("title");

                WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(getActivity());
                wbExists = mHelper.getWhiteboardByGuid(guid);

                if (wbExists == null) {

                    url = new URL(imageUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();

                    output = new ByteArrayOutputStream(1024 * 600);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) {
                            publishProgress((int) (total * 100 / fileLength));
                        }
                        output.write(data, 0, count);
                    }
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null) {
                        output.close();
                        if (wbExists == null) {
                            Map<PictureUtils.PictureType, String> images = PictureUtils.storeBitmap(output.toByteArray(), 0, getActivity());
                            imageFileName = images.get(PictureUtils.PictureType.IMAGE);
                            thumbFileName = images.get(PictureUtils.PictureType.THUMBNAIL);
                        }
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
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
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
                Intent i = new Intent(getActivity(), WhiteboardListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            } else {


                final Intent i = new Intent(getActivity(), WhiteboardListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Whiteboard whiteboard;
                if (wbExists == null) {
                    i.putExtra(WhiteboardListFragment.WHITEBOARD_DATA_CHANGED, true);

                    WhiteboardDatabaseHelper mHelper = new WhiteboardDatabaseHelper(context);
                     Whiteboard.WhiteBoardBuilder wb = new Whiteboard.WhiteBoardBuilder()
                            .setDescription(description)
                            .setTitle(title)
                            .setImageFileName(imageFileName)
                            .setThumbFileName(thumbFileName)
                            .setCreated(created*1000)
                            .setUpdated(System.currentTimeMillis())
                            .setGuid(guid);


                    whiteboard = mHelper.insertWhiteboard(wb.build());
//                    Log.i(TAG, "insert new whiteboard: " + whiteboard);
                    Toast.makeText(context, "Whiteboard downloaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Whiteboard already exists", Toast.LENGTH_LONG).show();
                    whiteboard = wbExists;
                }

                finishedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(i);
                    }
                });


                getActivity().getActionBar().setTitle(whiteboard.getTitle());

                Context context = getActivity().getApplicationContext();
                Date dt = new Date(whiteboard.getCreated());
                String date = DateFormat.getDateFormat(context).format(dt);
                String time = DateFormat.getTimeFormat(context).format(dt);
                getActivity().getActionBar().setSubtitle(date + " - " + time);

                descriptionView.setText(whiteboard.getDescription());

                try {
                    FileInputStream is = new FileInputStream(PictureUtils.getPathToFile(getActivity(), whiteboard.getImageFileName()));
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


                finishedButton.setEnabled(true);
            }
        }

    }
}
