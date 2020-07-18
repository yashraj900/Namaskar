package mjm.mjmca.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.R;
import mjm.mjmca.model.ChatMessages;
import mjm.mjmca.model.Contact;
import mjm.mjmca.model.UserObject;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    Context context;
    ArrayList<Contact> userObjectArrayList;
    private boolean isChat;

    String lastMessage;

    DatabaseReference databaseReference;

    public ChatAdapter(Context context,ArrayList<Contact> userObjectArrayList, boolean isChat){
        this.context = context;
        this.userObjectArrayList = userObjectArrayList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list, parent, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.name.setText(userObjectArrayList.get(position).getName());
        /*if (userObjectArrayList.get(position).getImage() == null){
            holder.image.setImageResource(R.drawable.ic_account_circle_black_24dp);
        }
        else{
            holder.image.setImageBitmap(userObjectArrayList.get(position).getImage());
        }*/
        if (isChat){
            lastMessage(userObjectArrayList.get(position).getUid(), holder.last_message);
        }else{
            holder.last_message.setVisibility(View.GONE);
        }
        Picasso.get().load(userObjectArrayList.get(position).getImage()).placeholder(R.mipmap.account).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mjm = new Intent(context, mjm.mjmca.activities.chat.class);
                mjm.putExtra("userID", userObjectArrayList.get(position).getUid());
                context.startActivity(mjm);
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Chat");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ChatMessages chatMessages = ds.getValue(ChatMessages.class);
                    if (chatMessages !=null && chatMessages.getReceiver()!=null && chatMessages.getSender()!=null && FirebaseAuth.getInstance().getCurrentUser()!=null){
                        if (chatMessages.getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && chatMessages.getSender().equals(userObjectArrayList.get(position).getUid())||
                                chatMessages.getReceiver().equals(userObjectArrayList.get(position).getUid()) && chatMessages.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        && !chatMessages.isIsseen()){
                            unread++;
                        }
                        else{
                            holder.unreadMessagesLayout.setVisibility(View.GONE);
                            holder.unreadMessages.setVisibility(View.GONE);
                        }
                    }
                }
                if (unread == 0){
                    holder.unreadMessagesLayout.setVisibility(View.GONE);
                    holder.unreadMessages.setVisibility(View.GONE);
                }
                else{
                    holder.unreadMessages.setVisibility(View.VISIBLE);
                    holder.unreadMessagesLayout.setVisibility(View.VISIBLE);
                    holder.unreadMessages.setText(String.valueOf(unread));
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return userObjectArrayList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, last_message, unreadMessages;
        public CircleImageView image, online;
        public RelativeLayout unreadMessagesLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            online = itemView.findViewById(R.id.online);
            last_message = itemView.findViewById(R.id.last_message);
            unreadMessages = itemView.findViewById(R.id.unreadMessages);
            unreadMessagesLayout = itemView.findViewById(R.id.unreadMessageLayout);
        }
    }
    private void lastMessage(final String userid, final TextView lst_msg){
        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chat");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ChatMessages chatMessages = snapshot.getValue(ChatMessages.class);
                    if (chatMessages !=null && chatMessages.getReceiver()!=null && chatMessages.getSender()!=null && firebaseUser!=null){
                        if (chatMessages.getReceiver().equals(firebaseUser.getUid()) && chatMessages.getSender().equals(userid)||
                        chatMessages.getReceiver().equals(userid) && chatMessages.getSender().equals(firebaseUser.getUid())){
                            lastMessage = chatMessages.getMessage();
                        }
                    }
                }
                switch (lastMessage){
                    case "default":
                        lst_msg.setText("");
                        break;
                    default:
                        lst_msg.setText(lastMessage);
                        break;
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
