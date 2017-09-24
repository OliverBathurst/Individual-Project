package com.oliver.bathurst.individualproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class Login extends AppCompatActivity{
    private String pass,passHint;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        pass = prefs.getString("app_pass", null);
        passHint = prefs.getString("app_pass_hint", null);

        final Button button = (Button) findViewById(R.id.email_sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLogin();
            }
        });

        final Button button2 = (Button) findViewById(R.id.info_login);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                infoDialog();
            }
        });


        try {
            webView = (WebView) findViewById(R.id.web);
            webView.setWebViewClient(new WebViewClient() {
                @SuppressWarnings("deprecation")
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    webView.setVisibility(View.GONE);
                }
            });
            webView.loadUrl("https://oliverbathurst.github.io/");
        }catch(Exception ignored){
            webView.setVisibility(View.GONE);
        }

        PermissionsManager perm = new PermissionsManager(this);
        perm.permissionsCheckup();
    }

    private void attemptLogin(){
        if(pass==null || pass.trim().length()==0){
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }else {
            showDialog();
        }
    }
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter your password:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert= builder.create();
        alert.show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().toString().trim().equals(pass)) {
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                } else {
                    if (passHint != null && passHint.trim().length() != 0) {
                        Toast.makeText(getApplicationContext(), "Password hint: " + passHint, Toast.LENGTH_LONG).show();
                    }
                    input.getText().clear();
                    Toast.makeText(getApplicationContext(), "Failure to login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void infoDialog(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Created by Oliver Bathurst").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }
    protected void onDestroy(){
        super.onDestroy();
        webView = null;
    }
}