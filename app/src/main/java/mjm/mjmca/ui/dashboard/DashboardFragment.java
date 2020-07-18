package mjm.mjmca.ui.dashboard;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import mjm.mjmca.R;
import mjm.mjmca.adapter.GroupChatAdapterList;
import mjm.mjmca.model.GroupModel;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private ArrayList<GroupModel> groupModelArrayList;
    private GroupChatAdapterList groupChatAdapterList;

    private FirebaseAuth firebaseAuth;

    public DashboardFragment(){

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.groupsRecyclerView);
        loadGroupList();
    }

    private void loadGroupList() {
        groupModelArrayList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupModelArrayList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        GroupModel groupModel = ds.getValue(GroupModel.class);
                        groupModelArrayList.add(groupModel);
                    }
                }
                groupChatAdapterList = new GroupChatAdapterList(getActivity(), groupModelArrayList);
                recyclerView.setAdapter(groupChatAdapterList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
