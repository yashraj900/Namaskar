package mjm.mjmca.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.R;

public class NotificationsFragment extends Fragment {

    CircleImageView profileImage;
    TextView name, username, phoneNumber, location;
    String currentUserID, locale;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileImage = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.name);
        username = view.findViewById(R.id.username);
        phoneNumber = view.findViewById(R.id.phone_number);
        location = view.findViewById(R.id.locationText);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("user").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&&
                    dataSnapshot.hasChild("username")&&
                    dataSnapshot.hasChild("name") &&
                    dataSnapshot.hasChild("phone") &&
                    dataSnapshot.hasChild("image")){
                    String n = ""+dataSnapshot.child("name").getValue();
                    name.setText(n);
                    String un = ""+dataSnapshot.child("username").getValue();
                    username.setText(un);
                    String p = ""+dataSnapshot.child("phone").getValue();
                    phoneNumber.setText(p);
                    String pi = ""+dataSnapshot.child("image").getValue();
                    try {
                        Picasso.get().load(pi).placeholder(R.mipmap.account).into(profileImage);
                    }catch(Exception e){
                        profileImage.setImageResource(R.mipmap.account);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();

            }
        });
        locale = getContext().getResources().getConfiguration().locale.getCountry();
        location.setText(locale);
    }
}
