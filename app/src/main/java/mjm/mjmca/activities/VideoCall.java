package mjm.mjmca.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import mjm.mjmca.MainActivity;
import mjm.mjmca.R;

public class VideoCall extends AppCompatActivity {
    private ImageView userProfile;
    private TextView username;
    private LinearLayout answer_call, end_call;
    private String receiverUserId="", receiverUsername="", receiverUserProfile="";
    private DatabaseReference userRef;
    private String senderUserId="", senderUsername="", senderUserProfile="";
    private String checker="", callingID="", ringingID="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (getIntent().getExtras()!=null){
            receiverUserId = getIntent().getExtras().get("user_id").toString();
        }
        userRef = FirebaseDatabase.getInstance().getReference().child("user");

        setContentView(R.layout.video_call);
        userProfile = findViewById(R.id.user_profile_picture);
        username = findViewById(R.id.username);
        answer_call = findViewById(R.id.answer_call);
        end_call = findViewById(R.id.end_call);
        end_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "clicked";
                endCallingUser();
            }
        });
        answer_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final HashMap<String, Object> callingPickup = new HashMap<>();
                callingPickup.put("picked", "picked");
                userRef.child(senderUserId).child("Ringing").updateChildren(callingPickup).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(VideoCall.this, VideoChat.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
        getAndSetUserProfileInfo();
    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverUserId).exists()){
                    receiverUserProfile = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUsername = dataSnapshot.child(receiverUserId).child("username").getValue().toString();
                    username.setText(receiverUsername);
                    Picasso.get().load(receiverUserProfile).placeholder(R.mipmap.ic_launcher).into(userProfile);
                }
                if (dataSnapshot.child(senderUserId).exists()){
                    senderUserProfile = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUsername = dataSnapshot.child(senderUserId).child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") &&!dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")){
                    final HashMap<String, Object> callingInfo = new HashMap<>();
                    /*callingInfo.put("uid", senderUserId);
                    callingInfo.put("name", senderUsername);
                    callingInfo.put("image", senderUserProfile);*/
                    callingInfo.put("calling", receiverUserId);
                    userRef.child(senderUserId).child("Calling").updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                final HashMap<String, Object> ringingInfo = new HashMap<>();
                                /*ringingInfo.put("uid", receiverUserId);
                                ringingInfo.put("name", receiverUsername);
                                ringingInfo.put("image", receiverUserProfile);*/
                                ringingInfo.put("ringing", senderUserId);
                                userRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") || !dataSnapshot.child(senderUserId).hasChild("Calling")){
                    answer_call.setVisibility(View.VISIBLE);
                }
                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")){
                    Intent intent = new Intent(VideoCall.this, VideoChat.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void endCallingUser(){
        userRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")){
                    callingID = dataSnapshot.child("calling").getValue().toString();
                    userRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Intent intent = new Intent(VideoCall.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    Intent intent = new Intent(VideoCall.this, chat.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")){
                    ringingID = dataSnapshot.child("ringing").getValue().toString();
                    userRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Intent intent = new Intent(VideoCall.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    Intent intent = new Intent(VideoCall.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
