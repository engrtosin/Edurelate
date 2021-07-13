package com.codepath.edurelate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.codepath.edurelate.R;
import com.codepath.edurelate.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "HomeActivity";

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
}