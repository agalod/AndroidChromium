package de.rheingold.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.chromium.chrome.browser.BuildConfig;
import org.chromium.chrome.browser.ChromeApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Upload browse data to the study server
 * Alexander Roehnisch
 * August-October 2018
 * Copyright Rheingold 2018
 */
public class UploadJobService extends JobService
{
    private static final String TAG = ChromeApplication.TAG_RHG_UPLOADSERVICE;

    public class RHGMessage
    {
        public RHGMessage(String file, Bitmap bitmap, String screenshotBytes, String url, String reason, String tabId)
        {
            this.file = file;
            this.bitmap = bitmap;
            this.screenshotBytes = screenshotBytes;
            this.url = url;
            this.reason = reason;
            this.tabId = tabId;
        }

        String file = null;
        Bitmap bitmap = null;
        String screenshotBytes = null;
        String url = null;
        String reason = null;
        String tabId = null;
    }

    ArrayList<RHGMessage> messages = new ArrayList<RHGMessage>();

    String file = null;
    Bitmap bitmap = null;
    String screenshotBytes = null;
    String url = null;
    String reason = null;
    String tabId = null;
    JobParameters params;

    private Messenger mActivityMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mActivityMessenger = intent.getParcelableExtra(ChromeApplication.MESSENGER_INTENT_KEY);
        if(mActivityMessenger == null)
            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Error: could not initialize messenger for upload service.");
        else
            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Initialized messenger for upload service.");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params)
    {
        // The work that this service "does" is simply wait for a certain duration and finish
        // the job (on another thread).

//        sendMessage(ChromeApplication.MSG_COLOR_START, params.getJobId());
        this.params = params;

        long duration = params.getExtras().getLong(ChromeApplication.WORK_DURATION_KEY);

        PersistableBundle extras = params.getExtras();
        tabId = (String) extras.get("tabId");
        reason = (String) extras.get("reason");
        url = (String) extras.get("url");
        file = (String) extras.get("bitmapFile");
        Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "... " + extras);
//            screenshotBytesString = (String) extras.get("bitmapBytes");
//            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Uploading " + screenshotBytesString.substring(0,10));
        uploadMessage(file);

        // Uses a handler to delay the execution of jobFinished().
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                sendMessage(ChromeApplication.MSG_COLOR_STOP, params.getJobId());
                jobFinished(params, false);
            }
        }, duration);
        Log.i(TAG, "on start job: " + params.getJobId());

        // Return true as there's more work to be done with this job.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params)
    {
        // Stop tracking these job parameters, as we've 'finished' executing.
        sendMessage(ChromeApplication.MSG_COLOR_STOP, params.getJobId());
        Log.i(TAG, "on stop job: " + params.getJobId());

        // Return false to drop the job.
        return false;
    }

    private void sendMessage(int messageID, @Nullable Object params)
    {
//        // If this service is launched by the JobScheduler, there's no callback Messenger. It
//        // only exists when the MainActivity calls startService() with the callback in the Intent.
//        if (mActivityMessenger == null)
//        {
//            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Service is bound, not started. There's no callback to send a message to.");
//            return;
//        }
//        Message m = Message.obtain();
//        m.what = messageID;
//        m.obj = params;
//        try
//        {
//            mActivityMessenger.send(m);
//        } catch (RemoteException e)
//        {
//            Log.e(TAG, "Error passing service object back to activity.");
//        }
    }

    public UploadJobService()
    {
//        super("UploadService");
    }

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent)
//    {
//        Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "MessageUploadService");
//        if (intent != null)
//        {
//            Bundle extras = intent.getExtras();
//            tabId = (String) extras.get("tabId");
//            reason = (String) extras.get("reason");
//            url = (String) extras.get("url");
//            file = (File) extras.get("bitmapFile");
//            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "... " + extras);
////            screenshotBytesString = (String) extras.get("bitmapBytes");
////            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Uploading " + screenshotBytesString.substring(0,10));
//            uploadMessage(file);
//        }
//    }

    private void uploadMessage(final String file)
    {

        if (ChromeApplication.authorization == null || ChromeApplication.authorization.length() < 1)
        {
            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Cannot upload. No authorization code");
            return;
        }

        if (file != null && file.length() > 0)
        {
            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Bitmap received at " + file);
            try
            {
                bitmap = BitmapFactory.decodeFile(file);
                if (bitmap != null)
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    screenshotBytes = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
                    stream.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        String filename = "ping";
        String requestURL = "https" + "://screen.rheingold-salon.de/api/0/" + filename;
        RequestQueue queue = Volley.newRequestQueue(this, null);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestURL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                if (response.contains("true"))
                {
                    sendMessage(ChromeApplication.UPLOADMSG_SUCCESS, params.getJobId());
                    Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Successfully uploaded message: " + response);
                    if (file != null)
                    {
                        File f = new File(file);
                        Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Deleting screenshot: " + file + " result: " + f.delete());
                    }
                } else
                    Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Error in uploading message: " + response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Error in uploading message: " + error.getLocalizedMessage());
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();

                @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                String visited_at = df.format(Calendar.getInstance().getTime());

                params.put("messages[][id]", UUID.randomUUID().toString());
                params.put("messages[][url]", url);
                params.put("messages[][reason]", reason);
                params.put("messages[][tab_id]", tabId);
                params.put("messages[][window_id]", "1");
                params.put("messages[][visited_at]", visited_at);
                params.put("messages[][session_id]", ChromeApplication.sessionId);
                params.put("messages[][browser_id]", ChromeApplication.browserId);

                Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, params.toString());

                if (screenshotBytes != null)
                    params.put("messages[][screenshot]", "data:image/jpeg;base64," + String.valueOf(screenshotBytes));
                params.put("messages[][deviceinfo]", "Android " + Build.VERSION.SDK + "-" + Build.MANUFACTURER + "-" + Build.MODEL + "-" + Build.DEVICE);
                if (BuildConfig.DEBUG)
                    params.put("debug", "true");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Basic " + ChromeApplication.authorization);
                return headers;
            }
        };
        queue.add(stringRequest);

    }
}
