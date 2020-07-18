package mjm.mjmca.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.R;
import mjm.mjmca.model.Contact;

public class chatUserDetails extends AppCompatActivity {
    ImageView back;
    String userID;
    TextView online;
    DatabaseReference reference;
    CircleImageView profileImage;
    TextView name, username, phoneNumber, location;
    LinearLayout notificationLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_user_deatils);
        final Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        notificationLayout = findViewById(R.id.notification_linearlayout);
        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(chatUserDetails.this, "Setting design batao", Toast.LENGTH_SHORT).show();
            }
        });
        profileImage = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        phoneNumber = findViewById(R.id.phone_number);
        location = findViewById(R.id.locationText);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        reference = FirebaseDatabase.getInstance().getReference("user").child(userID);
        online = findViewById(R.id.online);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Contact contact = dataSnapshot.getValue(Contact.class);
                name.setText(contact.getName());
                username.setText(contact.getUsername());
                phoneNumber.setText(contact.getPhone());
                Picasso.get().load(contact.getImage()).into(profileImage);
                if (dataSnapshot.child("userstate").hasChild("state")&&dataSnapshot.child("userstate").hasChild("time")&&dataSnapshot.child("userstate").hasChild("date")){
                    String state = dataSnapshot.child("userstate").child("state").getValue().toString();
                    String time = dataSnapshot.child("userstate").child("time").getValue().toString();
                    String date = dataSnapshot.child("userstate").child("date").getValue().toString();
                    if (state.equals("online")){
                        online.setText("Online");
                    }
                    else if (state.equals("offline")){
                        online.setText("Last Seen: " + date + " " + time);
                    }
                }
                else{
                    online.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
