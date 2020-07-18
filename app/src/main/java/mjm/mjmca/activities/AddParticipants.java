package mjm.mjmca.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import mjm.mjmca.R;
import mjm.mjmca.adapter.AddParticipantsAdapter;
import mjm.mjmca.model.Contact;

public class AddParticipants extends AppCompatActivity {
    private RecyclerView recyclerView;
    private String groupID, groupRole;
    private FirebaseAuth firebaseAuth;
    private String groupname, groupPhoto, groupDescription, groupid, groupTimeStamp, groupCreatedBy;
    private AddParticipantsAdapter addParticipantsAdapter;
    private ArrayList<Contact> contactArrayList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);
        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        recyclerView = findViewById(R.id.recyclerViewAD);
        loadGroupInfo();
    }
    private void getAllUser() {
        contactArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactArrayList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    Contact contact = ds.getValue(Contact.class);
                    if (!firebaseAuth.getUid().equals(contact.getUid())){
                        contactArrayList.add(contact);
                    }
                }
                addParticipantsAdapter = new AddParticipantsAdapter(AddParticipants.this, contactArrayList, ""+groupID, ""+groupRole);
                recyclerView.setAdapter(addParticipantsAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void loadGroupInfo() {
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Group");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group");
        reference.orderByChild("groupId").equalTo(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    groupname = ""+ds.child("groupName").getValue();
                    groupPhoto = ""+ds.child("groupIcon").getValue();
                    groupDescription = ""+ds.child("groupDescription").getValue();
                    groupid = ""+ds.child("groupId").getValue();
                    groupTimeStamp = ""+ds.child("groupTimeStamp").getValue();
                    groupCreatedBy = ""+ds.child("groupCreatedBy").getValue();
                    ref1.child(groupid).child("Participants").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                groupRole = ""+dataSnapshot.child("role").getValue();
                                getAllUser();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
