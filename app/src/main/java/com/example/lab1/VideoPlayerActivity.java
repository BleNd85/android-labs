package com.example.lab1;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.ArrayList;


public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private LinearLayout topBar;
    private LinearLayout bottomBar;
    private SeekBar seekBar;
    private TextView titleText, timeText, durationText;
    private ImageButton previousButton, pauseButton, nextButton, rotateButton;
    private final Handler handler = new Handler();
    private int lastPosition = 0;
    private ArrayList<String> uriList;
    private ArrayList<String> nameList;
    private int currentIndex;
    private WindowInsetsControllerCompat windowInsetsController;
    private final String PREFS = "video_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.video_view);
        topBar = findViewById(R.id.top_bar);
        bottomBar = findViewById(R.id.bottom_controls);
        ViewCompat.setOnApplyWindowInsetsListener(bottomBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom + (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
            return WindowInsetsCompat.CONSUMED;
        });
        titleText = findViewById(R.id.video_title);
        timeText = findViewById(R.id.time_text);
        durationText = findViewById(R.id.duration_time_text);
        seekBar = findViewById(R.id.video_seekbar);
        previousButton = findViewById(R.id.btn_previous);
        nextButton = findViewById(R.id.btn_forward);
        pauseButton = findViewById(R.id.btn_play_pause);
        rotateButton = findViewById(R.id.btn_rotate);


        windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        String name = getIntent().getStringExtra("media_name");

        uriList = getIntent().getStringArrayListExtra("media_uri_list");
        nameList = getIntent().getStringArrayListExtra("media_name_list");
        currentIndex = getIntent().getIntExtra("media_index", 0);

        loadVideoAt(currentIndex);

        titleText.setText(name);

        String uniqueKey = "video_position_" + name;

        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        lastPosition = preferences.getInt(uniqueKey, 0);

        videoView.setOnCompletionListener(mp -> {
            if (currentIndex < uriList.size() - 1) {
                currentIndex++;
                loadVideoAt(currentIndex);
            } else {
                pauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
        });

        FrameLayout container = findViewById(R.id.video_container);
        container.setOnClickListener(v -> {
            if (topBar.getVisibility() == View.VISIBLE && bottomBar.getVisibility() == View.VISIBLE) {
                topBar.setVisibility(View.GONE);
                bottomBar.setVisibility(View.GONE);
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
                handler.removeCallbacksAndMessages(null);
            } else {
                showOverlay();
            }
        });

        pauseButton.setOnClickListener(v -> {
            boolean wasPlaying = videoView.isPlaying();

            if (wasPlaying) {
                videoView.pause();
                pauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
            if (!wasPlaying) {
                videoView.start();
                pauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
            updateTime();
        });

        previousButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                loadVideoAt(currentIndex);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < uriList.size() - 1) {
                currentIndex++;
                loadVideoAt(currentIndex);
            }
        });

        rotateButton.setOnClickListener(v -> {
            int currentOrientation = getResources().getConfiguration().orientation;

            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean wasPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                wasPlaying = videoView.isPlaying();
                if (wasPlaying) videoView.pause();
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                    timeText.setText(format(progress));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress());
                updateTime();
                if (wasPlaying) {
                    videoView.start();
                    updateTime();
                }
            }
        });

    }

    private void updatePlayPauseIcon() {
        if (videoView.isPlaying()) {
            pauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            pauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void loadVideoAt(int index) {
        String uriString = uriList.get(index);
        String name = nameList.get(index);
        titleText.setText(name);

        String uniqueKey = "video_position_" + name;
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        lastPosition = preferences.getInt(uniqueKey, 0);

        videoView.setVideoURI(Uri.parse(uriString));
        videoView.setOnPreparedListener(mp -> {
            videoView.seekTo(lastPosition);
            videoView.start();
            seekBar.setMax(videoView.getDuration());
            showOverlay();
            durationText.setText(format(videoView.getDuration()));
            updateTime();
            updatePlayPauseIcon();
        });

        updateNavButtons();
    }

    private void updateNavButtons() {
        previousButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < uriList.size() - 1);
    }

    private void showOverlay() {
        topBar.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        handler.postDelayed(() -> {
                    topBar.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.GONE);
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
                }
                , 7000);
    }

    private void updateTime() {
        if (videoView == null) return;

        int current = videoView.getCurrentPosition();
        timeText.setText(format(current));
        seekBar.setProgress(current);

        if (videoView.isPlaying()) {
            handler.postDelayed(this::updateTime, 100);
        }
    }

    private String format(int ms) {
        int s = ms / 1000;
        int m = s / 60;
        int h = m / 60;
        s %= 60;
        m %= 60;
        return h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        editor.putInt("video_position_" + getIntent().getStringExtra("media_name"), videoView.getCurrentPosition());
        editor.apply();
    }
}
