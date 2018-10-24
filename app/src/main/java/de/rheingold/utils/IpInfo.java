package de.rheingold.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.format.Time;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.chromium.chrome.browser.ChromeApplication;
import org.chromium.chrome.browser.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class IpInfo
{
    public static void sendAlive(final Context context, final String message)
    {
        String filename = "ipinfo.php";
        String url = "http" + "://alsotoday.com/rheingold/" + filename;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {

            }
        })
        {
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                Time now = new Time();
                now.setToNow();
                long currenttimestamp = (System.currentTimeMillis()) / 1000;
                params.put("message", message);
                params.put("apptime", String.valueOf(currenttimestamp + now.gmtoff));
                params.put("deviceinfo", "Android " + android.os.Build.VERSION.SDK + "-" + Build.MANUFACTURER + "-" + android.os.Build.MODEL + "-" + android.os.Build.DEVICE);
                if (BuildConfig.DEBUG)
                    params.put("debug", "false");
//                if (m_PackageInfo != null)
                try
                {
                    params.put("appversion", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
                Log.d(ChromeApplication.TAG_RHG_LOGIN, "Sending alive");
                return params;
            }
        };
        queue.add(stringRequest);

    }
}
