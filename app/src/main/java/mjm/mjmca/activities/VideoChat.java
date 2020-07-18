package mjm.mjmca.activities;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import mjm.mjmca.MainActivity;
import mjm.mjmca.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChat extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {
    private static String API_KEY = "46782354";
    private static String SESSION_ID = "1_MX40Njc4MjM1NH5-MTU5NDM4MjQxMTEzOH4rZmFxaG9qaEJURVh3bmZEaFdQNzBFdTJ-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njc4MjM1NCZzaWc9MTdlNzhhYjNiMmVkMDY5MjAwMGFlMjMxNjg4MWU0ZTdhMTIwNmZmNjpzZXNzaW9uX2lkPTFfTVg0ME5qYzRNak0xTkg1LU1UVTVORE00TWpReE1URXpPSDRyWm1GeGFHOXFhRUpVUlZoM2JtWkVhRmRRTnpCRmRUSi1mZyZjcmVhdGVfdGltZT0xNTk0MzgyNDk2Jm5vbmNlPTAuMzAwMzM4NDkzMTgxNzE5JnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE1OTY5NzQzODImaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String LOG_TAG = VideoChat.class.getSimpleName();
    private static final int RECORD_VIDEO_APP_PERMISSION = 124;
    private ImageView endCall;
    private DatabaseReference userRef;
    private String userID = "";
    private FrameLayout publisher_controller, subscriber_controller;
    private Session mSessions;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_chat);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        userRef = FirebaseDatabase.getInstance().getReference().child("user");
        endCall = findViewById(R.id.close_video_chat);
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing")){
                            userRef.child(userID).child("Ringing").removeValue();
                            if (mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChat.this, MainActivity.class));
                            finish();
                        }
                        if (dataSnapshot.child(userID).hasChild("Calling")){
                            userRef.child(userID).child("Calling").removeValue();
                            if (mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChat.this, MainActivity.class));
                            finish();
                        }
                        else{
                            if (mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChat.this, MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChat.this);

    }
    @AfterPermissionGranted(RECORD_VIDEO_APP_PERMISSION)
    private void requestPermission(){
        String[] permissions = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, permissions)){
            publisher_controller = findViewById(R.id.publisher);
            subscriber_controller = findViewById(R.id.subscriber);
            mSessions = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSessions.setSessionListener(VideoChat.this);
            mSessions.connect(TOKEN);
        }
        else{
            EasyPermissions.requestPermissions(this, "Please Grant permission to continue", RECORD_VIDEO_APP_PERMISSION);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Toast.makeText(this, "Session Connected", Toast.LENGTH_SHORT).show();
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChat.this);
        publisher_controller.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSessions.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Toast.makeText(this, "Session Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Toast.makeText(this, "Stream Received", Toast.LENGTH_SHORT).show();
        if (mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSessions.subscribe(mSubscriber);
            subscriber_controller.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Toast.makeText(this, "Stream Dropped", Toast.LENGTH_SHORT).show();
        if (mSubscriber!=null){
            mSubscriber = null;
            subscriber_controller.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Toast.makeText(this, "Stream Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
