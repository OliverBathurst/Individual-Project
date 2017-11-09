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

        perm = new PermissionsManager(this);
        perm.permissionsCheckup();
    }
    private void attemptLogin(){
        if(pass == null || pass.trim().length() == 0){
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }else {
            showDialog();
        }
    }
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.enter_password_prompt));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.submit_login), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().trim().equals(pass)) {
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                } else {
                    input.getText().clear();
                    Toast.makeText(getApplicationContext(), getString(R.string.login_fail_message), Toast.LENGTH_SHORT).show();
                    if (passHint != null && passHint.trim().length() != 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.show_password_hint) + " " + passHint, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void infoDialog(){
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.my_name) + "\n" + getString(R.string.email_address) + "\n" + (perm != null ? perm.getAppInfo() : getString(R.string.no_app_version_found)))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create().show();
    }
}