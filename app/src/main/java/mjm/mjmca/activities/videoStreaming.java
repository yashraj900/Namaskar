package mjm.mjmca.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import mjm.mjmca.R;

public class videoStreaming extends AppCompatActivity {
    private VideoView videoView;
    String videoUrl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_streaming);
        Intent intent = getIntent();
        videoUrl = intent.getStringExtra("videourl");
        MediaController mediaController = new MediaController(this);
        videoView = findViewById(R.id.videoView);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);
        videoView.start();
    }
}
