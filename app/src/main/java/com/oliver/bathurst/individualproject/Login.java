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
 * Unauthorized copying of this file via any medium is strictly prohibited
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

        findViewById(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLogin();
            }
        });

        findViewById(R.id.info_login).setOnClickListener(new View.OnClickListener() {
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
        new PermissionsManager(this).permissionsCheckup();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Please enter your password:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().trim().equals(pass)) {
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                } else {
                    input.getText().clear();
                    Toast.makeText(getApplicationContext(), "Failure to login", Toast.LENGTH_SHORT).show();
                    if (passHint != null && passHint.trim().length() != 0) {
                        Toast.makeText(getApplicationContext(), "Password hint: " + passHint, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void infoDialog(){
        new android.app.AlertDialog.Builder(this)
                .setMessage("Created by Oliver Bathurst")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create().show();
    }
    protected void onDestroy(){
        super.onDestroy();
        webView = null;
    }
}