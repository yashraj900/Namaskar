package mjm.mjmca.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import mjm.mjmca.R;

public class ImageViewActivity extends AppCompatActivity {
    private ImageView photo;
    String imageUrl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageviewss);
        photo = findViewById(R.id.imageViews);
        Intent intent = getIntent();
        imageUrl = intent.getStringExtra("imageUrl");
        Picasso.get().load(imageUrl).into(photo);
    }
}
