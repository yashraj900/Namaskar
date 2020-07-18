package mjm.mjmca.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.R;
import mjm.mjmca.model.Contact;
import mjm.mjmca.model.UserObject;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    Context context;
    ArrayList<Contact> userObjectArrayList;

    public UserListAdapter(Context context,ArrayList<Contact> userObjectArrayList){
        this.context = context;
        this.userObjectArrayList = userObjectArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts, parent, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.name.setText(userObjectArrayList.get(position).getName());
        /*if (userObjectArrayList.get(position).getImage() == null){
            holder.image.setImageResource(R.drawable.ic_account_circle_black_24dp);
        }
        else{
            holder.image.setImageBitmap(userObjectArrayList.get(position).getImage());
        }*/
        Picasso.get().load(userObjectArrayList.get(position).getImage()).placeholder(R.mipmap.account).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mjm = new Intent(context, mjm.mjmca.activities.chat.class);
                mjm.putExtra("userID", userObjectArrayList.get(position).getId());
                context.startActivity(mjm);
            }
        });


    }


    @Override
    public int getItemCount() {
        return userObjectArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CircleImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
        }
    }
}
