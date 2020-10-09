package com.library.libcamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private ProcessCameraProvider cameraProvider;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private PreviewView mPreviewView;
    private RelativeLayout captureImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mPreviewView = findViewById(R.id.previewView);
        captureImage = findViewById(R.id.rlButtonCapture);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {

                cameraProvider = cameraProviderFuture.get();
                int cameraFacing = getIntent().getIntExtra("isFrontCamera", 0);
                String instruction = getIntent().getStringExtra("instruction");
                TextView tvInstruction = findViewById(R.id.tvInstruction);
                tvInstruction.setText(instruction);
                ImageView bgOverlay = findViewById(R.id.bgOverlay);
                if (cameraFacing == 0 && hasBackCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_BACK;
                    bgOverlay.setBackgroundResource(R.drawable.overlay_identity_card);
                } else if (cameraFacing == 1 && hasFrontCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_FRONT;
                    bgOverlay.setBackgroundResource(R.drawable.overlay_selfie_with_id);
                } else {
                    throw new IllegalStateException("Back and front camera are unavailable");
                }
                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();


        ImageCapture.Builder builder = new ImageCapture.Builder();

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        cameraProvider.unbindAll();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        String encodedString = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        Log.d("encode string", "" + encodedString);
                        CacheImage.setCacheImage(CameraActivity.this, encodedString);
                        Intent i = new Intent();
                        setResult(Activity.RESULT_OK, i);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                    }
                });
            }
        });
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private boolean hasBackCamera() {
        try {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns true if the device has an available front camera. False otherwise
     */
    private boolean hasFrontCamera() {
        try {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
            return false;
        }
    }
}
