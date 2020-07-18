package mjm.mjmca.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import mjm.mjmca.R;

public class audio_streaming extends AppCompatActivity {

    private ImageView playPause;
    private TextView currentTime, totalTime, audioName;
    private SeekBar seekBar;
    private String audioUrl, audioNameString;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_streaming);
        playPause = findViewById(R.id.play_pause);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        audioName = findViewById(R.id.song_name);
        mediaPlayer = new MediaPlayer();
        seekBar = findViewById(R.id.seekbar);
        Intent ite = getIntent();
        audioNameString = ite.getStringExtra("audioName");
        audioName.setText(audioNameString);
        Intent intent = getIntent();
        audioUrl = intent.getStringExtra("audioUrl");
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    handler.removeCallbacks(runnable);
                    mediaPlayer.pause();
                    playPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                }else{
                    mediaPlayer.start();
                    playPause.setImageResource(R.drawable.ic_pause_black_24dp);
                    updateSeekBar();
                }
            }
        });
        prepareMediaPlayer();
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SeekBar seekBar = (SeekBar)view;
                int playPosition = (mediaPlayer.getDuration() / 100) + seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                currentTime.setText(millSecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                seekBar.setSecondaryProgress(i);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekBar.setProgress(0);
                playPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                currentTime.setText("0:00");
                totalTime.setText("0:00");
                mediaPlayer.reset();
                prepareMediaPlayer();
            }
        });

    }

    private void prepareMediaPlayer(){
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            totalTime.setText(millSecondsToTimer(mediaPlayer.getDuration()));
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            long currentDuration  = mediaPlayer.getCurrentPosition();
            currentTime.setText(millSecondsToTimer(currentDuration));
        }
    };

    private void updateSeekBar(){
        if (mediaPlayer.isPlaying()){
            seekBar.setProgress((int)(((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) + 100));
            handler.postDelayed(runnable, 1000);
        }
    }

    private String millSecondsToTimer(long milliseconds){
        String timeString = "";
        String secondsString;

        int hours = (int)(milliseconds / (1000 * 60 * 60));
        int minutes = (int)(milliseconds % (1000 * 60 * 60))/(1000 * 60);
        int seconds = (int)((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0){
            timeString = hours + ":";
        }
        if(seconds < 10){
            secondsString = "0" + seconds;
        }
        else{
            secondsString = "" + seconds;
        }
        timeString = timeString + minutes + ":" + secondsString;
        return timeString;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
