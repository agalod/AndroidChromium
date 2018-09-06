package de.rheingold.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
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
public class UploadService extends IntentService
{
    File file = null;
    Bitmap bitmap = null;
    String screenshotBytes = null;
    String url = null;
    String reason = null;
    String tabId = null;

    public UploadService()
    {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "MessageUploadService");
        if (intent != null)
        {
            Bundle extras = intent.getExtras();
            tabId = (String) extras.get("tabId");
            reason = (String) extras.get("reason");
            url = (String) extras.get("url");
            file = (File) extras.get("bitmapFile");
            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "... " + extras);
//            screenshotBytesString = (String) extras.get("bitmapBytes");
//            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Uploading " + screenshotBytesString.substring(0,10));
            uploadMessage(file);
        }
    }

    private void uploadMessage(final File file)
    {

        if (ChromeApplication.authorization == null || ChromeApplication.authorization.length() < 1)
        {
            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Cannot upload. No authorization code");
            return;
        }

        if (file != null && file.getAbsolutePath().length() > 0 )
        {
            Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Bitmap received at " + file.getAbsolutePath());
            try
            {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if(bitmap != null)
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
                    Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Successfully uploaded message: " + response);
                    if (file != null)
                        Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICE, "Deleting screenshot: " + file + " result: " + file.delete());
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
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError
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
                params.put("messages[][deviceinfo]", "Android " + android.os.Build.VERSION.SDK + "-" + Build.MANUFACTURER + "-" + android.os.Build.MODEL + "-" + android.os.Build.DEVICE);
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
