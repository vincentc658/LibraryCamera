package com.library.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.library.libcamera.CameraActivity;
import com.library.libcamera.WebPageActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvCamera= findViewById(R.id.tvCamera);
        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new  Intent(MainActivity.this, WebPageActivity.class);
                startActivity(i);
            }
        });
    }
}