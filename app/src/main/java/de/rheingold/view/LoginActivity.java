package de.rheingold.view;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.chromium.chrome.browser.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    SharedPreferences sharedPref;
    private EditText etEmail;
    private EditText etPass;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (Button) findViewById(R.id.btnLogin);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPass);
        login.setOnClickListener(this);
        sharedPref = this.getSharedPreferences("SharedPreference_userInfo", MODE_PRIVATE);
        etEmail.setText(sharedPref.getString("Email Adress", ""));
        etPass.setText(sharedPref.getString("Paswwort", ""));
    }

    @Override
    public void onClick(View view) {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Email Adress", etEmail.getText().toString());
        editor.putString("Paswwort", etPass.getText().toString());
        editor.apply();
    }
}
