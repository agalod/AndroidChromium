package de.rheingold.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.chromium.chrome.browser.ChromeApplication;
import org.chromium.chrome.browser.R;
import org.chromium.chrome.browser.document.ChromeLauncherActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import de.rheingold.database.TLookup;
import de.rheingold.utils.IpInfo;
import de.rheingold.utils.SSLUtils;

/*
 * LoginActivity
 * Purpose: Login for Rheingold Browser
 * August 2018
 * Author Alexander Roehnisch
 * Copyright Rheingold GmbH
 * */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{

    SharedPreferences sharedPref;
    private EditText etEmail;
    private EditText etPass;
    private Button login;
    public PackageInfo packageInfo;
    public String studyName;
    public String studyId;

    public boolean isInternetAvailable()
    {
        try
        {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e)
        {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            android.support.v13.app.ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        login = (Button) findViewById(R.id.btnLogin);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPass);
        login.setOnClickListener(this);
        sharedPref = this.getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        etEmail.setText(sharedPref.getString("Email Adress", ""));
        etPass.setText(sharedPref.getString("Paswwort", ""));
        try
        {
            packageInfo = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        String functionName = getClass().getName() + " - " + new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_LOGIN, functionName);

        SSLUtils.setCertificate(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 0:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onClick(View view)
    {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Email Adress", etEmail.getText().toString());
        editor.putString("Paswwort", etPass.getText().toString());
        editor.apply();

        login();
    }


    public void login()
    {
        String filename = "auth";
        String url = "https" + "://screen.rheingold-salon.de/api/0/" + filename;
        RequestQueue queue = Volley.newRequestQueue(this, null);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
//                loadManifest();
                JSONObject json = null;
                try
                {
                    json = new JSONObject(response);
                    studyId = json.getString("study_id");
                } catch (JSONException e)
                {
                    String message = "Fehler beim Einloggen: " + e.getMessage();
                    Log.d(ChromeApplication.TAG_RHG_LOGIN, message);
                    e.printStackTrace();
                }
                if (json != null && studyId != null)
                {
                    String message = "Erfolgreich eingeloggt: " + response;
                    if (ChromeApplication.rhgDebugMode)
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    Log.d(ChromeApplication.TAG_RHG_LOGIN, message);
                    loadManifest();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(LoginActivity.this, "Fehler beim Login. Überprüfen sie Ihre Internetverbindung: " + error, Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                byte[] strBytes = null;
                String credentials = etEmail.getText() + ":" + etPass.getText();
                try
                {
                    strBytes = credentials.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
                ChromeApplication.authorization = Base64.encodeToString(strBytes, Base64.DEFAULT);
                headers.put("Authorization", ChromeApplication.authorization);
                if (getResources().getBoolean(R.bool.sendalive))
                    IpInfo.sendAlive(getApplicationContext(), etEmail.getText().toString());
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);

    }

    public Response.Listener<String> manifestCallback = new Response.Listener < String > ()
    {
        @Override
        public void onResponse (String response)
        {
            JSONObject json = null;
            try
            {
                json = new JSONObject(response);
                studyName = json.getString("name");
            } catch (JSONException e)
            {
                Toast.makeText(LoginActivity.this, "Fehler beim Laden des Manifests. Überprüfen sie Ihre Internetverbindung.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            if (json != null && studyName != null)
            {
                Log.d(ChromeApplication.TAG_RHG_LOGIN, "Manifest loaded: " + json);
                if (!TLookup.setContent(json))
                {
                    String messsage = "Error: could not write manifest to database.";
                    Log.d(ChromeApplication.TAG_RHG_DATABASE, messsage);
                    Toast.makeText(getApplicationContext(), messsage, Toast.LENGTH_LONG).show();
                }
                Intent browserIntent = new Intent(LoginActivity.this, ChromeLauncherActivity.class);
                LoginActivity.this.startActivity(browserIntent);
            }
        }
    };

        public void loadManifest ()
        {
            Log.d(ChromeApplication.TAG_RHG_LOGIN, "Loading manifest...");
            String filename = "manifest";
            String url = "https" + "://screen.rheingold-salon.de/api/0/" + filename;
            RequestQueue queue = Volley.newRequestQueue(this, null);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET, url,
                    manifestCallback,
                    new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Toast.makeText(LoginActivity.this, "Fehler beim Laden des Manifests. Überprüfen sie Ihre Internetverbindung: " + error, Toast.LENGTH_LONG).show();
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", "Basic " + ChromeApplication.authorization);
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        }
    }
