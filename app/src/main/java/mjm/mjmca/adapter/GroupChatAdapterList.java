package mjm.mjmca.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.R;
import mjm.mjmca.activities.GroupChat;
import mjm.mjmca.model.GroupModel;

public class GroupChatAdapterList extends RecyclerView.Adapter<GroupChatAdapterList.ViewHolder> {
    Context context;
    ArrayList<GroupModel> groupModels;

    public GroupChatAdapterList(Context context, ArrayList<GroupModel> groupModels) {
        this.context = context;
        this.groupModels = groupModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupModel groupModel = groupModels.get(position);
        String groupIcon = groupModel.getGroupIcon();
        final String groupId = groupModel.getGroupId();
        String groupName = groupModel.getGroupName();
        holder.senderName.setText("");
        holder.lastMessage.setText("");
        holder.groupName.setText(groupName);
        try {
            Picasso.get().load(groupIcon).placeholder(R.mipmap.account).into(holder.profile);
        }catch (Exception e){
            holder.profile.setImageResource(R.mipmap.account);
        }
        loadLastMessage(groupModel, holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //groupId
                Intent intent = new Intent(context, GroupChat.class);
                intent.putExtra("groupID", groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(GroupModel groupModel, final ViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupModel.getGroupId()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String message = ""+ds.child("message").getValue();
                    String sender = ""+ds.child("sender").getValue();
                    holder.lastMessage.setText(message);
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("user");
                    databaseReference1.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                String name = ""+ds.child("name").getValue();
                                holder.senderName.setText(name + ":");
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

    @Override
    public int getItemCount() {
        return groupModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profile;
        private TextView groupName, senderName, lastMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profile);
            groupName = itemView.findViewById(R.id.group_name);
            senderName = itemView.findViewById(R.id.sender_name);
            lastMessage = itemView.findViewById(R.id.groupLastMessage);
        }
    }
}
