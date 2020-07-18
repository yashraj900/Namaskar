package mjm.mjmca.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import mjm.mjmca.R;
import mjm.mjmca.adapter.GroupChatMessagesAdapter;
import mjm.mjmca.model.GroupMessages;

public class GroupChat extends AppCompatActivity {
    String groupID, myRoleInGroup="";
    private CircleImageView profile;
    private TextView groupName;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    ImageView sendMessage;
    private MediaPlayer sentSound;
    private String currentUserID;
    Uri fileImageUri, fileVideoUri, fileAudioUri, fileDocumentUri;
    ImageView file;
    ImageView voice_message;
    private String username;
    private ProgressBar progressBar, progressBarImage, progressBarVideo, progressBarAudio, progressBarMic;
    private ImageView menu;
    private PopupMenu popupMenu;

    private RecyclerView recyclerView;

    EmojiconEditText messages;
    String checker = null;
    private static final int STORAGE_REQUEST_PERMISSION = 124;
    private static final int CAMERA_REQUEST_CODE = 224;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 324;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 424;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri imageUri = null;

    private static final int IMAGE_PICK = 1;
    private static final int VIDEO_PICK = 2;
    private static final int AUDIO_PICK = 3;
    private static final int DOCUMENT_PICK = 4;
    private String time, date;
    private String groupname, groupPhoto, groupDescription, groupid, groupTimeStamp, groupCreatedBy;
    private ArrayList<GroupMessages> groupMessagesArrayList;
    private GroupChatMessagesAdapter groupChatMessagesAdapter;
    private String messagePushId;
    private MediaRecorder mRecorder;
    String filename = "null";
    private DatabaseReference reference2;
    ImageView imgEmojiBtn;
    ConstraintLayout rlRootView;
    EmojIconActions emojAction;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_chat);
        sentSound = MediaPlayer.create(this, R.raw.doneforyou);
        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        reference2 = FirebaseDatabase.getInstance().getReference();
        mRecorder = new MediaRecorder();
        filename = Environment.getExternalStorageDirectory().getAbsolutePath();
        filename +="/recorded_audio.3gp";
        profile = findViewById(R.id.user_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(GroupChat.this, GroupInfo.class);
                intent1.putExtra("groupID", groupID);
                startActivity(intent1);
            }
        });
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        progressBar = findViewById(R.id.progressBar);
        progressBarImage = findViewById(R.id.progressBarImage);
        progressBarVideo = findViewById(R.id.progressBarVideo);
        progressBarAudio = findViewById(R.id.progressBarAudio);
        progressBarMic = findViewById(R.id.progressBarMic);


        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        groupName = findViewById(R.id.name);
        recyclerView = findViewById(R.id.message_recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        date = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        time = currentTime.format(calendar.getTime());
        sendMessage = findViewById(R.id.sendMessage);
        file = findViewById(R.id.sendFiles);
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupChat.this, R.style.BottomSheetDialogTheme);
                View uploadView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.app_information_layout, (LinearLayout)findViewById(R.id.app_info_bottom_sheet_container));
                uploadView.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checker = "camera";
                        /*Intent mjm= new Intent(Intent.ACTION_GET_CONTENT);
                        mjm.setType("image/*");
                        startActivityForResult(mjm, IMAGE_PICK);*/
                        if (!checkCameraPermission()){
                            requestCameraPermission();
                        }
                        else{
                            captureFromCamera();
                        }
                    }
                });
                uploadView.findViewById(R.id.images).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checker = "video";
                        Intent mjms = new Intent(Intent.ACTION_GET_CONTENT);
                        mjms.setType("video/*");
                        startActivityForResult(mjms, VIDEO_PICK);
                    }
                });
                uploadView.findViewById(R.id.audio).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checker = "audio";
                        Intent mjs = new Intent(Intent.ACTION_GET_CONTENT);
                        mjs.setType("audio/*");
                        startActivityForResult(mjs, AUDIO_PICK);
                    }
                });
                uploadView.findViewById(R.id.documents).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checker = "document";
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/*");
                        startActivityForResult(intent, DOCUMENT_PICK);
                    }
                });
                bottomSheetDialog.setContentView(uploadView);
                bottomSheetDialog.show();
            }
        });
        voice_message = findViewById(R.id.audio_voice_message);
        voice_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(GroupChat.this, "", Toast.LENGTH_SHORT).show();
            }
        });
        /*voice_message.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    startRecording();
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    stopRecording();
                }
                return true;
            }
        });*/
        rlRootView = findViewById(R.id.contrainsLayout);
        imgEmojiBtn = findViewById(R.id.emoticons);
        messages = findViewById(R.id.messagesEmotions);
        emojAction = new EmojIconActions(this, rlRootView, messages, imgEmojiBtn);
        emojAction.ShowEmojIcon();
        emojAction.setIconsIds(R.drawable.ic_keyboard_black_24dp,R.drawable.ic_insert_emoticon_black_24dp);
        emojAction.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
            }
            @Override
            public void onKeyboardClose() {
            }
        });
        messages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()){
                    voice_message.setVisibility(View.INVISIBLE);
                    sendMessage.setVisibility(View.VISIBLE);
                    sendMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*notify = true;*/
                            String msg = messages.getText().toString();
                            if (!msg.equals("")){
                                SendMessage(msg);
                            }
                            else{
                                Toast.makeText(GroupChat.this, "Write something", Toast.LENGTH_SHORT).show();
                            }
                            messages.setText("");
                        }
                    });
                }
                else if (editable.toString().isEmpty()){
                    sendMessage.setVisibility(View.GONE);
                    voice_message.setVisibility(View.VISIBLE);
                    voice_message.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                                Toast.makeText(GroupChat.this, "Speak", Toast.LENGTH_SHORT).show();
                                startRecording();
                            }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                                stopRecording();
                            }
                            return true;
                        }
                    });
                }

            }
        });
        loadGroupMessages();
        loadMyGroupRole();
    }

    private void startRecording(){
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(filename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        }catch (IOException e){

        }
        mRecorder.start();
    }

    private void stopRecording(){
        try {
            mRecorder.stop();
        }catch (RuntimeException e){

        }finally {
            mRecorder.release();
            mRecorder = null;
        }
        Uri uri = Uri.fromFile(new File(filename));
        progressBarMic.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audios");
        DatabaseReference referencess = reference2.child("Group").child(groupID).child("Messages").push();
        final String messagePushIDsss = referencess.getKey();
        final StorageReference filePath = storageReference.child(messagePushIDsss + "." + "mp3");
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Group").child(groupID).child("Messages").push();
                        String messagePushIDssssh = reference.getKey();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", firebaseAuth.getUid());
                        hashMap.put("message", "🎵 Audio");
                        hashMap.put("downloadURl", uri.toString());
                        hashMap.put("date", date);
                        hashMap.put("time", time);
                        hashMap.put("type", checker);
                        hashMap.put("groupId", groupID);
                        hashMap.put("fileName", filename);
                        hashMap.put("fileExtension", "");
                        hashMap.put("fileSize", "");
                        hashMap.put("isseen", false);
                        hashMap.put("messagePushID", messagePushIDssssh);
                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressBarMic.setVisibility(View.GONE);
                                    Toast.makeText(GroupChat.this, "Audio sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarMic.setVisibility(View.GONE);
                        Toast.makeText(GroupChat.this, "Failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                progressBarMic.setProgress(progress);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBarMic.setVisibility(View.GONE);
                Toast.makeText(GroupChat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMyGroupRole() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    myRoleInGroup = ""+ds.child("role").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupMessages() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.child(groupID).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupMessagesArrayList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    GroupMessages groupMessages = ds.getValue(GroupMessages.class);
                    groupMessagesArrayList.add(groupMessages);
                }
                groupChatMessagesAdapter = new GroupChatMessagesAdapter(GroupChat.this, groupMessagesArrayList);
                recyclerView.setAdapter(groupChatMessagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupInfo() {
        groupMessagesArrayList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Group");
        databaseReference.orderByChild("groupId").equalTo(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    groupname = ""+ds.child("groupName").getValue();
                    groupPhoto = ""+ds.child("groupIcon").getValue();
                    groupDescription = ""+ds.child("groupDescription").getValue();
                    groupid = ""+ds.child("groupId").getValue();
                    groupTimeStamp = ""+ds.child("groupTimeStamp").getValue();
                    groupCreatedBy = ""+ds.child("groupCreatedBy").getValue();
                    groupName.setText(groupname);
                    try {
                        Picasso.get().load(groupPhoto).placeholder(R.mipmap.account).into(profile);
                    }catch (Exception e){
                        profile.setImageResource(R.mipmap.account);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group").child(groupID).child("Messages").push();
        messagePushId = databaseReference.getKey();
        String timeStamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseAuth.getUid());
        hashMap.put("message", message);
        hashMap.put("date", date);
        hashMap.put("groupId", groupID);
        hashMap.put("time", time);
        hashMap.put("type", "text");
        hashMap.put("messagePushID", messagePushId);
        databaseReference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                messages.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    public String getRealSize(Context context, Uri uri){
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Audio.Media.SIZE};
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            int column = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            cursor.moveToFirst();
            return cursor.getString(column);
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
    }

    public String getFileExtension(Uri uri){
        String extension="";
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }

    public String getFileName(Uri uri){
        String result = null;
        if (uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor!=null&& cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if (result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut!=-1){
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    private void captureFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Group");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Group image item description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return  result && result2;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_PERMISSION);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return  result;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        captureFromCamera();
                    }
                    else{
                        Toast.makeText(this, "Camera & Storage Permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            fileImageUri = data.getData();
            final String name = getFileName(fileImageUri);
            final String imageExtension = getFileExtension(fileImageUri);
            final String size = getRealSize(this, fileImageUri);
            if (!checker.equals("image")){

            }
            else if (checker.equals("image")){
                progressBarImage.setVisibility(View.VISIBLE);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images");
                DatabaseReference referencess = reference2.child("Group").child(groupID).child("Messages").push();
                final String messagePushIDs = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDs + "." + imageExtension);
                filePath.putFile(fileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Group").child(groupID).child("Messages").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseAuth.getUid());
                                hashMap.put("message", "📸 Image");
                                hashMap.put("downloadURl", uri.toString());
                                hashMap.put("groupId", groupID);
                                hashMap.put("date", date);
                                hashMap.put("time", time);
                                hashMap.put("type", checker);
                                hashMap.put("fileName", name);
                                hashMap.put("fileExtension", imageExtension);
                                hashMap.put("fileSize", size);
                                hashMap.put("isseen", false);
                                hashMap.put("messagePushID", messagePushIDssssh);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sentSound.start();
                                            progressBarImage.setVisibility(View.GONE);
                                            Toast.makeText(GroupChat.this, "Image sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarImage.setVisibility(View.GONE);
                                Toast.makeText(GroupChat.this, "Failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        progressBarImage.setProgress(progress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarImage.setVisibility(View.GONE);
                        Toast.makeText(GroupChat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == VIDEO_PICK && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            fileVideoUri = data.getData();
            final String name = getFileName(fileVideoUri);
            final String videoExtension = getFileExtension(fileVideoUri);
            final String size = getRealSize(this, fileVideoUri);
            if (!checker.equals("video")){

            }
            else if (checker.equals("video")){
                progressBarVideo.setVisibility(View.VISIBLE);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Videos");
                DatabaseReference referencess = reference2.child("Group").child(groupID).child("Messages").push();
                final String messagePushIDss = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDss + "." + videoExtension);
                filePath.putFile(fileVideoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Group").child(groupID).child("Messages").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseAuth.getUid());
                                hashMap.put("message", "🎥 Video");
                                hashMap.put("downloadURl", uri.toString());
                                hashMap.put("date", date);
                                hashMap.put("time", time);
                                hashMap.put("type", checker);
                                hashMap.put("groupId", groupID);
                                hashMap.put("fileName", name);
                                hashMap.put("fileExtension", videoExtension);
                                hashMap.put("fileSize", size);
                                hashMap.put("isseen", false);
                                hashMap.put("messagePushID", messagePushIDssssh);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sentSound.start();
                                            progressBarVideo.setVisibility(View.GONE);
                                            Toast.makeText(GroupChat.this, "Video sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarVideo.setVisibility(View.GONE);
                                Toast.makeText(GroupChat.this, "Failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        progressBarVideo.setProgress(progress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarVideo.setVisibility(View.GONE);
                        Toast.makeText(GroupChat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            final String name = getFileName(imageUri);
            final String imageExtension = getFileExtension(imageUri);
            final String size = getRealSize(this, imageUri);
            if (!checker.equals("camera")){

            }
            else if (checker.equals("camera")){
                progressBarVideo.setVisibility(View.VISIBLE);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Videos");
                DatabaseReference referencess = reference2.child("Group").child(groupID).child("Messages").push();
                final String messagePushIDss = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDss + "." + imageExtension);
                filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Group").child(groupID).child("Messages").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseAuth.getUid());
                                hashMap.put("message", "📸 Image");
                                hashMap.put("downloadURl", uri.toString());
                                hashMap.put("date", date);
                                hashMap.put("time", time);
                                hashMap.put("type", checker);
                                hashMap.put("groupId", groupID);
                                hashMap.put("fileName", name);
                                hashMap.put("fileExtension", imageExtension);
                                hashMap.put("fileSize", size);
                                hashMap.put("isseen", false);
                                hashMap.put("messagePushID", messagePushIDssssh);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sentSound.start();
                                            progressBarVideo.setVisibility(View.GONE);
                                            Toast.makeText(GroupChat.this, "Image sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarVideo.setVisibility(View.GONE);
                                Toast.makeText(GroupChat.this, "Failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        progressBarVideo.setProgress(progress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarVideo.setVisibility(View.GONE);
                        Toast.makeText(GroupChat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == AUDIO_PICK && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            fileAudioUri = data.getData();
            final String name = getFileName(fileAudioUri);
            final String audioExtension = getFileExtension(fileAudioUri);
            final String size = getRealSize(this, fileAudioUri);
            if (!checker.equals("audio")){

            }
            else if (checker.equals("audio")){
                progressBarAudio.setVisibility(View.VISIBLE);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audios");
                DatabaseReference referencess = reference2.child("Group").child(groupID).child("Messages").push();
                final String messagePushIDsss = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDsss + "." + audioExtension);
                filePath.putFile(fileAudioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Group").child(groupID).child("Messages").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseAuth.getUid());
                                hashMap.put("message", "🎵 Audio");
                                hashMap.put("downloadURl", uri.toString());
                                hashMap.put("date", date);
                                hashMap.put("time", time);
                                hashMap.put("type", checker);
                                hashMap.put("fileName", name);
                                hashMap.put("fileExtension", audioExtension);
                                hashMap.put("fileSize", size);
                                hashMap.put("isseen", false);
                                hashMap.put("groupId", groupID);
                                hashMap.put("messagePushID", messagePushIDssssh);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sentSound.start();
                                            progressBarAudio.setVisibility(View.GONE);
                                            Toast.makeText(GroupChat.this, "Audio sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarAudio.setVisibility(View.GONE);
                                Toast.makeText(GroupChat.this, "Failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        progressBarAudio.setProgress(progress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarAudio.setVisibility(View.GONE);
                        Toast.makeText(GroupChat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == DOCUMENT_PICK && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            fileDocumentUri = data.getData();
            final String name = getFileName(fileDocumentUri);
            final String extension = getFileExtension(fileDocumentUri);
            final String size = getRealSize(this, fileDocumentUri);
            if (!checker.equals("document")){

            }
            else if (checker.equals("document")){
                /*Toast.makeText(this, "Check Notification For Progress", Toast.LENGTH_SHORT).show();
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(this, "notify_001");
                remoteViews = new RemoteViews(getPackageName(), R.layout.n);
                remoteViews.setImageViewBitmap(R.id.profile, mBitmap);
                remoteViews.setTextViewText(R.id.name, username);
                remoteViews.setTextViewText(R.id.toWhom, "Sending document to " + username);
                remoteViews.setProgressBar(R.id.progress, 100, 50, true);
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mBuilder.setAutoCancel(true);
                mBuilder.setOngoing(false);
                mBuilder.setPriority(Notification.PRIORITY_HIGH);
                mBuilder.setOnlyAlertOnce(true);
                mBuilder.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_HIGH;
                mBuilder.setCustomContentView(remoteViews);
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    String channelId = "mjm";
                    NotificationChannel notificationChannel = new NotificationChannel(channelId, "MJMCA", NotificationManager.IMPORTANCE_HIGH);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 500});
                    notificationManager.createNotificationChannel(notificationChannel);
                    mBuilder.setChannelId(channelId);
                }
                notification = mBuilder.build();
                notificationManager.notify(NotificationID, notification);*/
                Toast.makeText(this, "Check Up", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.VISIBLE);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documents");
                DatabaseReference referencess = reference2.child("Group").child(groupID).child("Messages").push();
                final String messagePushIDssssh = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDssssh + "." + extension);
                filePath.putFile(fileDocumentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Group").child(groupID).child("Messages").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseAuth.getUid());
                                hashMap.put("message", "📄 Document");
                                hashMap.put("groupId", groupID);
                                hashMap.put("downloadURl", uri.toString());
                                hashMap.put("date", date);
                                hashMap.put("time", time);
                                hashMap.put("type", checker);
                                hashMap.put("fileName", name);
                                hashMap.put("fileExtension", extension);
                                hashMap.put("fileSize", size);
                                hashMap.put("isseen", false);
                                hashMap.put("messagePushID", messagePushIDssssh);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sentSound.start();
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(GroupChat.this, "Document sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(GroupChat.this, "Failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        progressBar.setProgress(progress);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GroupChat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
