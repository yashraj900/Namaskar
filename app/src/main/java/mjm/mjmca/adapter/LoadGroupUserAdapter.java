package mjm.mjmca.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.StatementEvent;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.R;
import mjm.mjmca.model.Contact;

public class LoadGroupUserAdapter extends RecyclerView.Adapter<LoadGroupUserAdapter.ViewHolder> {

    Context context;
    ArrayList<Contact> contactArrayList;
    private String groupID, groupRole;

    public LoadGroupUserAdapter(Context context, ArrayList<Contact> contactArrayList, String groupID, String groupRole) {
        this.context = context;
        this.contactArrayList = contactArrayList;
        this.groupID = groupID;
        this.groupRole = groupRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Contact contact = contactArrayList.get(position);
        final String uid = contact.getUid();
        holder.name.setText(contact.getName());
        try{
            Picasso.get().load(contact.getImage()).placeholder(R.mipmap.account).into(holder.profile);
        }catch(Exception e){
            holder.profile.setImageResource(R.mipmap.account);
        }
        checkIfAlreadyExists(contact, holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
                databaseReference.child(groupID).child("Participants").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String previousRole = ""+ dataSnapshot.child("role").getValue();
                            String[] options;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Choose");
                            if (groupRole.equals("creator")){
                                if (previousRole.equals("admin")){
                                    options = new String[]{"Remove Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0){
                                                removeAdmin(contact);
                                            }
                                            else{
                                                removeParticipant(contact);
                                            }
                                        }
                                    }).show();
                                }
                                else if (previousRole.equals("participant")){
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0){
                                                makeAdmin(contact);
                                            }
                                            else{
                                                removeParticipant(contact);
                                            }
                                        }
                                    }).show();
                                }
                            }
                            else if (groupRole.equals("admin")){
                                if (previousRole.equals("creator")){
                                    Toast.makeText(context, "Creator of the group", Toast.LENGTH_SHORT).show();
                                }
                                else if (previousRole.equals("admin")){
                                    options = new String[]{"Remove Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0){
                                                removeAdmin(contact);
                                            }
                                            else{
                                                removeParticipant(contact);
                                            }
                                        }
                                    }).show();
                                }
                                else if (previousRole.equals("participant")){
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0){
                                                makeAdmin(contact);
                                            }
                                            else{
                                                removeParticipant(contact);
                                            }
                                        }
                                    }).show();
                                }
                            }
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Add Participant").setMessage("Add this user in this group?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    addParticipant(contact);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void addParticipant(Contact contact) {
        String timeStamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", contact.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timeStamp", timeStamp);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Participants").child(contact.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "User Added to the group", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(Contact contact) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Participants").child(contact.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "The user is now participant", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failure: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void removeParticipant(Contact contact){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Participants").child(contact.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "User removed from the group", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failure: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(Contact contact){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Participants").child(contact.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "The user is now admin", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failure: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExists(Contact contact, final ViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Participants").child(contact.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String hisRole = ""+dataSnapshot.child("role").getValue();
                    holder.status.setText(hisRole);
                }
                else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return contactArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profile;
        private TextView name, status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
        }
    }
}
