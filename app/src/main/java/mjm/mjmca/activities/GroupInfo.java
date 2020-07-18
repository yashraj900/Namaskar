package mjm.mjmca.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.net.Inet4Address;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.MainActivity;
import mjm.mjmca.R;
import mjm.mjmca.adapter.LoadGroupUserAdapter;
import mjm.mjmca.model.Contact;

public class GroupInfo extends AppCompatActivity {
    private String groupID;
    private String groupRole = "";
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private String groupname, groupPhoto, groupDescription, groupid, groupTimeStamp, groupCreatedBy;
    private ArrayList<Contact> contactArrayList;
    private LoadGroupUserAdapter loadGroupUserAdapter;
    private ImageView edit, more, exit;
    private String groupUNS;
    private CircleImageView profile;
    private TextView groupUN, groupNameT, totalUserBGN, exitGroup, groupDescriptionText, totalMembersINV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_info_layout);
        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        firebaseAuth = FirebaseAuth.getInstance();
        groupUN = findViewById(R.id.groupUN);
        groupNameT = findViewById(R.id.groupName);
        totalUserBGN = findViewById(R.id.totalMembers);
        groupDescriptionText = findViewById(R.id.groupDescription);
        totalMembersINV = findViewById(R.id.tmiv);
        profile = findViewById(R.id.profile);
        edit = findViewById(R.id.editGroup);
        more = findViewById(R.id.more);
        exit = findViewById(R.id.exit);
        exitGroup = findViewById(R.id.exitGroup);
        recyclerView = findViewById(R.id.addedUserRecyclerView);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(GroupInfo.this, EditGroup.class);
                intent1.putExtra("groupID", groupID);
                startActivity(intent1);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButton = "";
                if (groupRole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure you want to delete " + groupname + " group";
                    positiveButton = "DELETE";
                }
                else{
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure you want to leave " + groupname + " group";
                    positiveButton = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);
                builder.setTitle(dialogTitle).setMessage(dialogDescription).setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (groupRole.equals("creator")){
                            deleteGroup();
                        }
                        else{
                            LeaveGroup();
                        }
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
        exitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButton = "";
                if (groupRole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure you want to delete " + groupname + " group";
                    positiveButton = "DELETE";
                }
                else{
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure you want to leave " + groupname + " group";
                    positiveButton = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);
                builder.setTitle(dialogTitle).setMessage(dialogDescription).setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (groupRole.equals("creator")){
                            deleteGroup();
                        }
                        else{
                            LeaveGroup();
                        }
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
        /*addPArticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(GroupInfo.this, AddParticipants.class);
                intent1.putExtra("groupID", groupID);
                startActivity(intent1);
            }
        });*/
        loadGroupInfo();
        loadGroupRole();
    }

    private void deleteGroup() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupInfo.this, groupname + " group deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GroupInfo.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfo.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void LeaveGroup(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group");
        reference.child(groupID).child("Participants").child(firebaseAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupInfo.this, "You left the group", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GroupInfo.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfo.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadGroupRole() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    groupRole = ""+ds.child("role").getValue();
                    if (groupRole.equals("participant")){
                        edit.setVisibility(View.GONE);
                        exitGroup.setText("Leave Group");
                    }
                    else if (groupRole.equals("admin")){
                        exitGroup.setText("Delete Group");
                        edit.setVisibility(View.VISIBLE);

                    }
                    else if (groupRole.equals("creator")){
                        edit.setVisibility(View.VISIBLE);
                        exitGroup.setText("Delete Group");
                    }
                }
                loadParticipants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadParticipants() {
        contactArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group");
        reference.child(groupID).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactArrayList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
                    databaseReference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                Contact contact = ds.getValue(Contact.class);
                                contactArrayList.add(contact);
                            }
                            loadGroupUserAdapter = new LoadGroupUserAdapter(GroupInfo.this, contactArrayList, groupID, groupRole);
                            recyclerView.setAdapter(loadGroupUserAdapter);
                            totalMembersINV.setText(contactArrayList.size()+" Members");
                            totalUserBGN.setText(contactArrayList.size()+" Members");
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

    private void loadGroupInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.orderByChild("groupId").equalTo(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    groupname = ""+ds.child("groupName").getValue();
                    groupPhoto = ""+ds.child("groupIcon").getValue();
                    groupUNS = ""+ds.child("groupUN").getValue();
                    groupDescription = ""+ds.child("groupDescription").getValue();
                    groupid = ""+ds.child("groupId").getValue();
                    groupTimeStamp = ""+ds.child("groupTimeStamp").getValue();
                    groupCreatedBy = ""+ds.child("groupCreatedBy").getValue();
                    groupNameT.setText(groupname);
                    groupUN.setText(groupUNS);
                    groupDescriptionText.setText(groupDescription);
                    try {
                        Picasso.get().load(groupPhoto).placeholder(R.mipmap.account).into(profile);
                    }
                    catch (Exception e){
                        profile.setImageResource(R.mipmap.account);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCreatorInformation(final String groupTimeStamp, String groupCreatedBy) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        databaseReference.orderByChild("uid").equalTo(groupCreatedBy).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    /*groupCreatedOn.setText("Created By, " + name +" on" + groupTimeStamp);*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
