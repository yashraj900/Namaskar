package mjm.mjmca.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mjm.mjmca.Decoration.GridDecoration;
import mjm.mjmca.R;
import mjm.mjmca.adapter.ChatAdapter;
import mjm.mjmca.model.ChatList;
import mjm.mjmca.model.ChatMessages;
import mjm.mjmca.model.Contact;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    ArrayList<Contact> contacts;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private ArrayList<ChatList> usersList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addItemDecoration(new GridDecoration(1, 15, false));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();

        /*reference = FirebaseDatabase.getInstance().getReference("Chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ChatMessages chatMessages = ds.getValue(ChatMessages.class);
                    if (chatMessages != null && chatMessages.getSender().equals(firebaseUser.getUid())) {
                        usersList.add(chatMessages.getReceiver());
                    }
                    if (chatMessages != null && chatMessages.getReceiver().equals(firebaseUser.getUid())) {
                        usersList.add(chatMessages.getSender());
                    }
                }
                displayChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    usersList.add(chatList);
                }
                DisplayChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayChatList() {
        contacts = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contacts.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Contact contact = snapshot.getValue(Contact.class);
                    for (ChatList chatList:usersList){
                        if(contact!=null&&contact.getUid()!=null){
                            if (contact.getUid().equals(chatList.getId())){
                                contacts.add(contact);
                            }
                        }
                    }
                }
                chatAdapter = new ChatAdapter(getActivity(), contacts, true);
                recyclerView.setAdapter(chatAdapter);
                Collections.reverse(contacts);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*private void displayChats() {
        contacts = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contacts.clear();
                for (DataSnapshot dS:dataSnapshot.getChildren()){
                    Contact contact = dS.getValue(Contact.class);
                    for (String id: usersList){
                        if (contact!=null && contact.getId()!=null){
                            if (contact.getId().equals(id)){
                                if (contacts.size() != 0){
                                    for (Contact contact1: contacts){
                                        if (!contact.getId().equals(contact1.getId())){
                                            contacts.add(contact);
                                        }
                                    }
                                }
                                else{
                                    contacts.add(contact);
                                }
                            }
                        }
                    }
                }
                chatAdapter = new ChatAdapter(getActivity(), contacts);
                recyclerView.setAdapter(chatAdapter);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
