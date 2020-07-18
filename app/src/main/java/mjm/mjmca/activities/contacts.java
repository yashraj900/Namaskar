package mjm.mjmca.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mjm.mjmca.Decoration.GridDecoration;
import mjm.mjmca.R;
import mjm.mjmca.adapter.ContactListAdapter;
import mjm.mjmca.adapter.UserListAdapter;
import mjm.mjmca.model.Contact;
import mjm.mjmca.model.UserObject;
import mjm.mjmca.utils.CountryToPhonePrefix;

public class contacts extends AppCompatActivity {

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<Contact> userList, contactList;

    List<UserObject> phoneContactList;
    private ContactListAdapter contactListAdapter;
    private RecyclerView phoneContactRecyclerview;
    FirebaseUser firebaseUser;
    String userid;
    String image;
    String username;
    String about;
    String state;
    String ringing;
    String userstate;
    String calling;
    boolean isBlocked;
    private RelativeLayout goToGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        ImageView back =findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        contactList= new ArrayList<>();
        userList= new ArrayList<>();
        phoneContactList = new ArrayList<>();


        initializeRecyclerView();
        getContactList();
        getContacts();

        goToGroup = findViewById(R.id.group_layout);
        goToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contacts.this, CreateGroup.class);
                startActivity(intent);
            }
        });
    }


    private void initializeRecyclerView() {
        mUserList= findViewById(R.id.userRecycleview);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserList.addItemDecoration(new GridDecoration(1, 20, false));
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(mjm.mjmca.activities.contacts.this, userList);
        mUserList.setAdapter(mUserListAdapter);

        phoneContactRecyclerview = findViewById(R.id.contact_list);
        phoneContactRecyclerview.setNestedScrollingEnabled(false);
        phoneContactRecyclerview.setHasFixedSize(true);
        phoneContactRecyclerview.addItemDecoration(new GridDecoration(1, 20, false));
        phoneContactRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        contactListAdapter = new ContactListAdapter(mjm.mjmca.activities.contacts.this, phoneContactList);
        phoneContactRecyclerview.setAdapter(contactListAdapter);
    }


    private void getContactList(){

        String ISOPrefix = getCountryISO();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (phones != null) {
            while(phones.moveToNext()){
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                if(!String.valueOf(phone.charAt(0)).equals("+"))
                    phone = ISOPrefix + phone;

                Contact mContact = new Contact(userid, name, phone, image, userid, username, about, state, isBlocked);
                contactList.add(mContact);
                getUserDetails(mContact);
            }
        }
    }

    private void getUserDetails(final Contact mContact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String  phone = "",
                            name = "";
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        if(childSnapshot.child("phone").getValue()!=null)
                            phone = childSnapshot.child("phone").getValue().toString();
                        if(childSnapshot.child("name").getValue()!=null)
                            name = childSnapshot.child("name").getValue().toString();
                        if(childSnapshot.child("image").getValue()!=null)
                            image = childSnapshot.child("image").getValue().toString();
                        if (childSnapshot.child("uid").getValue()!=null){
                            userid = childSnapshot.child("uid").getValue().toString();
                        }
                        if (childSnapshot.child("username").getValue()!=null){
                            username = childSnapshot.child("username").getValue().toString();
                        }
                        if (childSnapshot.child("about").getValue()!=null){
                            about = childSnapshot.child("about").getValue().toString();
                        }
                        if (childSnapshot.child("state").getValue()!=null){
                            state = childSnapshot.child("state").getValue().toString();
                        }
                        if (childSnapshot.child("isBlocked").getValue()!=null){
                            isBlocked = (boolean) childSnapshot.child("isBlocked").getValue();
                        }


                        Contact mUser = new Contact(userid, name, phone, image, userid, username, about, state, isBlocked);
                        if (name.equals(phone)){
                            for (Contact mContactIterator : contactList){
                                if (mContactIterator.getPhone().equals(mUser.getPhone())){
                                    mUser.setName(mContactIterator.getName());
                                }
                            }
                        }
                        int flag = 0;
                        if (userList.size() == 0){
                            userList.add(mUser);
                        }
                        for (int i=0;i<userList.size();i++){
                            if (!userList.get(i).getPhone().trim().equals(phone)){
                                flag = 1;
                            }
                            else{
                                flag = 0;
                                break;
                            }
                        }
                        if (flag == 1){
                            userList.add(mUser);
                        }
                        mUserListAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private String getCountryISO(){
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso()!=null)
            if (!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso = telephonyManager.getNetworkCountryIso().toString();

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void getContacts(){
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                final String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorinfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id)));

                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                    Uri p_uri = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                    Bitmap bitmap = null;
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    }

                    assert cursorinfo != null;
                    while (cursorinfo.moveToNext()) {
                        final UserObject userObject = new UserObject();
                        userObject.id = id;
                        userObject.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        userObject.phone = cursorinfo.getString(cursorinfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        userObject.image = bitmap;
                        userObject.photoUri = p_uri;
                        int flag = 0;
                        if (phoneContactList.size() == 0){
                            phoneContactList.add(userObject);
                        }
                        for (int i=0;i<phoneContactList.size();i++){
                            if (!phoneContactList.get(i).getId().trim().equals(id)){
                                flag = 1;
                            }
                            else{
                                flag = 0;
                                break;
                            }
                        }
                        if (flag == 1){
                            phoneContactList.add(userObject);
                        }

                    }
                    cursorinfo.close();
                }
            }
            cursor.close();
        }

        contactListAdapter.notifyDataSetChanged();
    }



}



/*

    */