package com.example.soundrecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mStartButton;
    private Button mStopButton;
    private Button mPauseButton;
    private String mOutputFile;
    private MediaRecorder mRecorder;
    private boolean mIsRecording;
    private boolean mIsRecordingStopped;
    final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartButton = findViewById(R.id.start_button);
        mPauseButton = findViewById(R.id.pause_button);
        mStopButton = findViewById(R.id.stop_button);
        mStartButton.setOnClickListener(new ButtonClickListener());
        mPauseButton.setOnClickListener(new ButtonClickListener());
        mStopButton.setOnClickListener(new ButtonClickListener());

        mPauseButton.setEnabled(false);
        mStopButton.setEnabled(false);

        mOutputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.mp3";
    }

    private void startRecording() {
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile(mOutputFile);

            // first need to call the prepare function before we can start recording
            mRecorder.prepare();
            mRecorder.start();
            mIsRecording = true;
            mPauseButton.setEnabled(true);
            mStopButton.setEnabled(true);
            Toast.makeText(this, "Recording started", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void pauseRecording() {
        if (mRecorder != null && mIsRecording) {
            if (!mIsRecordingStopped) {
                mRecorder.pause();
                mIsRecordingStopped = true;
                mPauseButton.setText("Resume");
                Toast.makeText(this, "Recording is paused.", Toast.LENGTH_SHORT).show();
            }
        } else{
            resumeRecording();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording() {
        if (mRecorder != null && mIsRecording) {
            mRecorder.resume();
            mPauseButton.setText("Pause");
            mIsRecordingStopped = false;
            Toast.makeText(this, "Recording is resumed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        // Note: check if recording is currently running before calling mStopButton to avoid app crash.
        if (mRecorder != null && mIsRecording) {
            mRecorder.stop();
            mRecorder.release();
            mIsRecording = false;
            mPauseButton.setEnabled(false);
            mStopButton.setEnabled(false);
            Toast.makeText(this, "Recording is stopped.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No Recording is on right now.", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermissions() {
        for  (String permission : permissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 100);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(permissions[i])) {
                        showAlertDialog(permissions[i]);
                    }
                    return;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showAlertDialog(String permission) {
        new AlertDialog.Builder(this)
                .setMessage("Permission required for the app to work smoothly")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestMultiplePermissions();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.start_button:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (hasPermissions()) {
                            startRecording();
                        } else {
                            requestMultiplePermissions();
                        }
                    }
                    break;
                case R.id.pause_button:
                    pauseRecording();
                    break;
                case R.id.stop_button:
                    stopRecording();
                    break;
                    default:
                        break;
            }
        }
    }
}
