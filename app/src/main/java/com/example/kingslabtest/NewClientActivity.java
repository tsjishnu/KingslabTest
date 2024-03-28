package com.example.kingslabtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

public class NewClientActivity extends AppCompatActivity {

    private EditText dateEditText, timeEditText, addressEditText;
    private TextView usernameTextView;
    private ImageButton playButton,speaker;
    private ProgressBar progressBar;
    private Button addVoiceButton;
    private Calendar calendar;
    private String audioFilePath;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private static final int REQUEST_CODE_RECORD_AUDIO = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_client);

        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        addressEditText = findViewById(R.id.addressEditText);
        usernameTextView = findViewById(R.id.usernameTextView);
        progressBar = findViewById(R.id.playback_progress_bar);
        addVoiceButton = findViewById(R.id.btn_add_voice);
        playButton = findViewById(R.id.playButton);
        speaker = findViewById(R.id.speaker);

        String username = getIntent().getStringExtra("USERNAME");
        usernameTextView.setText(username);
        calendar = Calendar.getInstance();
        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        playButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        speaker.setVisibility(View.GONE);


        addVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewClientActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NewClientActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO);
                } else {
                    startRecording();
                    addVoiceButton.setVisibility(View.GONE);
                }
            }
        });
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewClientActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Update the dateEditText with the selected date
                                String date = (month + 1) + "-" + dayOfMonth + "-" + year;
                                dateEditText.setText(date);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(NewClientActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                                timeEditText.setText(time);
                            }
                        }, hour, minute, true); // true indicates 24-hour format

                // Show the time picker dialog
                timePickerDialog.show();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play the recorded audio with progress update using a Handler
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            progressBar.setMax(mediaPlayer.getDuration());
                            final Handler handler = new Handler();
                            final Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                        int currentPosition = mediaPlayer.getCurrentPosition();
                                        progressBar.setProgress(currentPosition);
                                        handler.postDelayed(this, 100); // Update progress every 100 milliseconds
                                    } else {
                                        handler.removeCallbacks(this); // Stop updates when playback finishes
                                    }
                                }
                            };
                            handler.post(runnable);
                        }
                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                            progressBar.setProgress(progressBar.getMax());
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private void startRecording() {
        try {
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio.3gp";
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecording();
                }
            }, 10000);
            mediaRecorder.start();
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }
    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            playButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            speaker.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
