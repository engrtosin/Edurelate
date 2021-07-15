package com.codepath.edurelate.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codepath.edurelate.databinding.ActivityLoginBinding;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    public static final String KEY_CURRENT_USER = "current_user";
    private ActivityLoginBinding binding;
    public static ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (ParseUser.getCurrentUser() != null) {
            currentUser = ParseUser.getCurrentUser();
            goHomeActivity();
        }

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                loginUser(username,password);
            }
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                goSignUpActivity(username,password);
            }
        });
    }

    private void goSignUpActivity(String username, String password) {
        Intent i = new Intent(LoginActivity.this,SignUpActivity.class);
        i.putExtra("username",username);
        i.putExtra("password",password);
        startActivity(i);
    }

    protected void loginUser(String username, String password) {
        Log.i(TAG, "Trying to login in user: " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    // TODO: Notify the user of the error
                    Log.e(TAG,"Login error occured",e);
                    return;
                }
                currentUser = ParseUser.getCurrentUser();
                goHomeActivity();
                Toast.makeText(LoginActivity.this,"Successful!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void logoutUser(Activity activity) {
        ParseUser.logOut();
        Intent i = new Intent(activity, LoginActivity.class);
        activity.startActivity(i);
        activity.finish();
        Log.i(TAG,"in login activity after logout");
        currentUser = ParseUser.getCurrentUser();
    }

    protected void goHomeActivity() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}