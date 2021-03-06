package com.oliver.bathurst.individualproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class Login extends AppCompatActivity{
    private String pass,passHint;
    private PermissionsManager perm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.newlogin);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(prefs.getBoolean("first_use_app", true)){
            Toast.makeText(getApplicationContext(), R.string.first_use_detected, Toast.LENGTH_LONG).show();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_use_app", false).apply();
        }

        pass = prefs.getString("app_pass", null);
        passHint = prefs.getString("app_pass_hint", null);

        findViewById(R.id.email_sign_in_button).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.info_login).setOnClickListener(v -> infoDialog());

        perm = new PermissionsManager(this);
        perm.permissionsCheckup();
    }
    private void attemptLogin(){
        if(pass == null || pass.trim().length() == 0){
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }else{
            showDialog();
        }
    }
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.enter_password_prompt));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.submit_login), (dialog, which) -> {
            if (input.getText().toString().trim().equals(pass)) {
                startActivity(new Intent(Login.this, MainActivity.class));
                finish();
            } else {
                input.getText().clear();
                Toast.makeText(getApplicationContext(), getString(R.string.login_fail_message), Toast.LENGTH_SHORT).show();
                if (passHint != null && passHint.trim().length() != 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.show_password_hint) + passHint, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_dialog), (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    private void infoDialog(){
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.my_name) + "\n" + getString(R.string.email_address) + "\n" + (perm != null ? perm.getAppInfo() : getString(R.string.no_app_version_found))
                + "\n" + getString(R.string.please_use_gcm_sms_instead))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), (dialog, id) -> dialog.dismiss()).create().show();
    }
}