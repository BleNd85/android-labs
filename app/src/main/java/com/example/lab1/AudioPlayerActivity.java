package com.example.lab1;

import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class AudioPlayerActivity extends AppCompatActivity {

    private ImageView audioImage, backgroundImage;
    private SeekBar seekBar;
    private TextView titleText, timeText, durationText;
    private ImageButton previousButton, playButton, nextButton;
    private ArrayList<String> uriList;
    private ArrayList<String> nameList;
    private int currentIndex;
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private NotificationManager notificationManager;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_audio_player);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            backgroundImage = findViewById(R.id.background_image);
            RenderEffect blur = RenderEffect.createBlurEffect(25f, 20f, Shader.TileMode.CLAMP);
            backgroundImage.setRenderEffect(blur);
        }

        audioImage = findViewById(R.id.audio_image);
        seekBar = findViewById(R.id.audio_seek_bar);
        timeText = findViewById(R.id.time_text);
        titleText = findViewById(R.id.audio_title_text);
        durationText = findViewById(R.id.duration_text);
        previousButton = findViewById(R.id.previous_btn);
        playButton = findViewById(R.id.play_btn);
        nextButton = findViewById(R.id.next_btn);
        backgroundImage = findViewById(R.id.background_image);

        mediaPlayer = new MediaPlayer();

        uriList = getIntent().getStringArrayListExtra("media_uri_list");
        nameList = getIntent().getStringArrayListExtra("media_name_list");
        currentIndex = getIntent().getIntExtra("media_index", 0);

        if (currentIndex >= uriList.size()) {
            currentIndex = 0;
        }

        loadAudioAt(currentIndex);


        mediaPlayer.setOnCompletionListener(mp -> {
            if (currentIndex < uriList.size() - 1) {
                currentIndex++;
                loadAudioAt(currentIndex);
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play);
            }
        });

        playButton.setOnClickListener(v -> {
            boolean wasPlaying = mediaPlayer.isPlaying();

            if (wasPlaying) {
                mediaPlayer.pause();
                playButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                updateTime();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                loadAudioAt(currentIndex);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < uriList.size() - 1) {
                currentIndex++;
                loadAudioAt(currentIndex);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean wasPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                wasPlaying = mediaPlayer.isPlaying();
                if (wasPlaying) mediaPlayer.pause();
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    timeText.setText(format(progress));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                updateTime();
                if (wasPlaying) {
                    mediaPlayer.start();
                    updateTime();
                }
            }
        });
    }

    private void loadAudioAt(int index) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        try {
            String uriString = uriList.get(index);
            String name = nameList.get(index);

            if (titleText != null) {
                titleText.setText(name);
            }

            mediaPlayer.setDataSource(this, Uri.parse(uriString));
            mediaPlayer.prepare();

            seekBar.setMax(mediaPlayer.getDuration());
            durationText.setText(format(mediaPlayer.getDuration()));

            mediaPlayer.start();
            playButton.setImageResource(android.R.drawable.ic_media_pause);
            updateTime();

            try {
                Bitmap albumArt = getAlbumArt(Uri.parse(uriString));
                if (albumArt != null) {
                    audioImage.setImageBitmap(albumArt);
                    backgroundImage.setImageBitmap(albumArt);
                } else {
                    audioImage.setImageResource(R.drawable.ic_launcher_background);
                    backgroundImage.setImageResource(R.drawable.ic_launcher_background);
                }
            } catch (Exception e) {
                e.printStackTrace();
                audioImage.setImageResource(R.drawable.ic_launcher_background);
                backgroundImage.setImageResource(R.drawable.ic_launcher_background);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTime() {
        if (mediaPlayer == null) return;

        int current = mediaPlayer.getCurrentPosition();
        timeText.setText(format(current));
        seekBar.setProgress(current);

        if (mediaPlayer.isPlaying()) {
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

    private Bitmap getAlbumArt(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            handler.removeCallbacksAndMessages(null);
        }
    }
}
