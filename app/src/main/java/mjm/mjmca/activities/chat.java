package mjm.mjmca.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import mjm.mjmca.Interface.APIService;
import mjm.mjmca.R;
import mjm.mjmca.adapter.MessageAdapter;
import mjm.mjmca.model.ChatMessages;
import mjm.mjmca.model.Contact;
import mjm.mjmca.service.Notification.Client;
import mjm.mjmca.service.Notification.Data;
import mjm.mjmca.service.Notification.Response;
import mjm.mjmca.service.Notification.Sender;
import mjm.mjmca.service.Notification.Token;
import retrofit2.Call;
import retrofit2.Callback;

public class chat extends AppCompatActivity {
    boolean isBlocked;
    private MediaPlayer sentSound;
    CircleImageView profileImage;
    TextView name;
    ImageView sendMessage;

    FirebaseUser firebaseUser;
    DatabaseReference reference, reference2;

    private String currentUserID;
    ImageView file;
    ImageView voice_message;


    private static final int STORAGE_REQUEST_PERMISSION = 124;
    private static final int CAMERA_REQUEST_CODE = 224;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 324;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 424;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri imageUri = null;

    Intent mjm;
    EmojiconEditText messages;

    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<ChatMessages> chatMessages;
    String userID;
    ImageView videoCall;
    ValueEventListener seenListener;
    TextView online;
    private String time, date;
    APIService apiService;
    boolean notify = false;
    String checker = null;
    private static final int IMAGE_PICK = 1;
    private static final int VIDEO_PICK = 2;
    private static final int AUDIO_PICK = 3;
    private static final int DOCUMENT_PICK = 4;
    StorageTask uploadTask;
    Uri fileImageUri, fileVideoUri, fileAudioUri, fileDocumentUri;
    String myURL="null";

    private MediaRecorder mRecorder;
    String filename = "null";
    private DatabaseReference userRef;
    private String calledBy="";

    private String i;
    private Bitmap mBitmap;

    //
    private RemoteViews remoteViews;
    private Notification notification;
    private NotificationManager notificationManager;
    private static final int NotificationID = 1006;
    private NotificationCompat.Builder mBuilder;
    private String username;
    private ProgressBar progressBar, progressBarImage, progressBarVideo, progressBarAudio, progressBarMic;
    private ImageView menu;
    private PopupMenu popupMenu;

    ImageView imgEmojiBtn;
    ConstraintLayout rlRootView;
    EmojIconActions emojAction;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        sentSound = MediaPlayer.create(this, R.raw.doneforyou);
        userRef = FirebaseDatabase.getInstance().getReference().child("user");
        if (FirebaseAuth.getInstance().getCurrentUser().getUid()!=null){
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
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
        mRecorder = new MediaRecorder();
        filename = Environment.getExternalStorageDirectory().getAbsolutePath();
        filename +="/recorded_audio.3gp";
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        reference2 = FirebaseDatabase.getInstance().getReference();
        online = findViewById(R.id.online);
        recyclerView = findViewById(R.id.message_recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        sendMessage = findViewById(R.id.sendMessage);
        profileImage = findViewById(R.id.user_profile);
        name = findViewById(R.id.name);
        mjm = getIntent();
        userID = mjm.getStringExtra("userID");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("user").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Contact contact = dataSnapshot.getValue(Contact.class);
                if (contact!=null){
                    username = contact.getName();
                    name.setText(username);
                    i = contact.getImage();
                    Picasso.get().load(i).placeholder(R.drawable.ic_account_circle_black_24dp).into(profileImage);
                }
                displayMessage(firebaseUser.getUid(), userID);
                if (dataSnapshot.child("userstate").hasChild("state") && dataSnapshot.child("userstate").hasChild("time") && dataSnapshot.child("userstate").hasChild("date")){
                    String state = dataSnapshot.child("userstate").child("state").getValue().toString();
                    String time = dataSnapshot.child("userstate").child("time").getValue().toString();
                    String date = dataSnapshot.child("userstate").child("date").getValue().toString();
                    if (state.equals("online")){
                        online.setText("Online");
                    }
                    else if (state.equals("offline")){
                        online.setText("Last Seen: " + date + " " + time);
                    }

                }
                else{
                    online.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Picasso.get().load(i).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBitmap = Bitmap.createBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
        menu = findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu = new PopupMenu(chat.this, menu);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.phone:
                                Toast.makeText(chat.this, "Make Phone Call", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.video_call:
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(chat.this);
                                alertDialog.setTitle("Alert");
                                alertDialog.setMessage("Are you going to do video call to " + username);
                                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(chat.this, VideoCall.class);
                                        intent.putExtra("user_id", userID);
                                        startActivity(intent);
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).create().show();
                                return true;
                            case R.id.block:
                                if(isBlocked){
                                    unBlockUser(userID);
                                }
                                else{
                                    blockUser(userID);
                                }
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        isCurrentUserBlockedOrNot(userID);
        checkIsBlocked(userID);
        file = findViewById(R.id.sendFiles);
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(chat.this, R.style.BottomSheetDialogTheme);
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
                        checker = "image";
                        Intent mjms = new Intent(Intent.ACTION_GET_CONTENT);
                        mjms.setType("image/*");
                        startActivityForResult(mjms, IMAGE_PICK);
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
                Toast.makeText(chat.this, "", Toast.LENGTH_SHORT).show();
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
                            notify = true;
                            String msg = messages.getText().toString();
                            if (!msg.equals("")){
                                SendMessage(firebaseUser.getUid(), userID, msg);
                            }
                            else{
                                Toast.makeText(chat.this, "Write something", Toast.LENGTH_SHORT).show();
                            }
                            messages.setText("");
                        }
                    });
                }
                else if (editable.toString().isEmpty()){
                    sendMessage.setVisibility(View.GONE);
                    voice_message.setVisibility(View.VISIBLE);
                    /*voice_message.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                                Toast.makeText(chat.this, "Speak", Toast.LENGTH_SHORT).show();
                                startRecording();
                            }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                                stopRecording();
                            }
                            return true;
                        }
                    });*/
                }

            }
        });
        seenMessage(userID);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        date = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        time = currentTime.format(calendar.getTime());
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(chat.this, chatUserDetails.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(chat.this, chatUserDetails.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });
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
        DatabaseReference referencess = reference2.child("Chat").child(userID).push();
        final String messagePushIDsss = referencess.getKey();
        final StorageReference filePath = storageReference.child(messagePushIDsss + "." + "mp3");
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat").push();
                        String messagePushIDssssh = reference.getKey();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", firebaseUser.getUid());
                        hashMap.put("receiver", userID);
                        hashMap.put("message", "🎵 Audio");
                        hashMap.put("downloadURl", uri.toString());
                        hashMap.put("date", date);
                        hashMap.put("time", time);
                        hashMap.put("type", checker);
                        hashMap.put("fileName", name);
                        hashMap.put("fileExtension", "");
                        hashMap.put("fileSize", "");
                        hashMap.put("isseen", false);
                        hashMap.put("messagePushID", messagePushIDssssh);
                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressBarMic.setVisibility(View.GONE);
                                    Toast.makeText(chat.this, "Audio sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBarMic.setVisibility(View.GONE);
                        Toast.makeText(chat.this, "Failure", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(chat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chat");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ChatMessages chatMessages = ds.getValue(ChatMessages.class);
                    assert chatMessages != null;
                    if (chatMessages.getReceiver() != null && chatMessages.getSender() != null){
                        if (chatMessages.getReceiver().equals(firebaseUser.getUid())&&chatMessages.getSender().equals(userid)){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", true);
                            ds.getRef().updateChildren(hashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage(String sender, final String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat").push();
        String messagePushID = reference.getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("date", date);
        hashMap.put("time", time);
        hashMap.put("messagePushID", messagePushID);
        hashMap.put("type", "text");
        hashMap.put("isseen", false);
        reference.setValue(hashMap);
        sentSound.start();

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(userID);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());
        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Contact contact = dataSnapshot.getValue(Contact.class);
                if (notify){
                    if (contact != null) {
                        sendNotification(receiver, contact.getName(), msg);
                    }
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendNotification(String receiver, final String name, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, name+": "+message, "New Message",
                            userID);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            if (response.code() == 200){
                                if (response.body().success != 1){
                                    Toast.makeText(chat.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void displayMessage(final String myId, final String user_id){
        chatMessages = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chat");
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatMessages.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ChatMessages cm = ds.getValue(ChatMessages.class);
                    if (cm != null && (cm.getReceiver().equals(myId) && cm.getSender().equals(user_id) || cm.getReceiver().equals(user_id) && cm.getSender().equals(myId))) {
                        chatMessages.add(cm);
                    }
                    messageAdapter = new MessageAdapter(chat.this, chatMessages);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserState("online");
        checkForReceivingCall();
    }

    private void checkForReceivingCall() {
        userRef.child(currentUserID).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("ringing")){
                    calledBy = ""+dataSnapshot.child("ringing").getValue();
                    Intent intent = new Intent(chat.this, VideoCall.class);
                    intent.putExtra("user_id", calledBy);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateUserState(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time", saveCurrentTime);
        hashMap.put("date", saveCurrentDate);
        hashMap.put("state", state);
        reference2.child("user").child(firebaseUser.getUid()).child("userstate").updateChildren(hashMap);


    }


    private void updateToken(String token){
        DatabaseReference references = FirebaseDatabase.getInstance().getReference("Tokens");
        Token tokens = new Token(token);
        references.child(firebaseUser.getUid()).setValue(tokens);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseUser!=null){
            updateUserState("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserState("offline");
    }


    private void captureFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Camera");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Camera image item description");
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
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted && storageAccepted) {
                    captureFromCamera();
                } else {
                    Toast.makeText(this, "Camera & Storage Permission are required", Toast.LENGTH_SHORT).show();
                }
            }
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
                DatabaseReference referencess = reference2.child("Chat").child(userID).push();
                final String messagePushIDs = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDs + "." + imageExtension);
                filePath.putFile(fileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseUser.getUid());
                                hashMap.put("receiver", userID);
                                hashMap.put("message", "📸 Image");
                                hashMap.put("downloadURl", uri.toString());
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
                                            Toast.makeText(chat.this, "Image sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarImage.setVisibility(View.GONE);
                                Toast.makeText(chat.this, "Failure", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(chat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                DatabaseReference referencess = reference2.child("Chat").child(userID).push();
                final String messagePushIDss = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDss + "." + videoExtension);
                filePath.putFile(fileVideoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseUser.getUid());
                                hashMap.put("receiver", userID);
                                hashMap.put("message", "🎥 Video");
                                hashMap.put("downloadURl", uri.toString());
                                hashMap.put("date", date);
                                hashMap.put("time", time);
                                hashMap.put("type", checker);
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
                                            Toast.makeText(chat.this, "Video sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarVideo.setVisibility(View.GONE);
                                Toast.makeText(chat.this, "Failure", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(chat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images");
                DatabaseReference referencess = reference2.child("Chat").child(userID).push();
                final String messagePushIDss = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDss + "." + imageExtension);
                filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseUser.getUid());
                                hashMap.put("receiver", userID);
                                hashMap.put("message", "📸 Image");
                                hashMap.put("downloadURl", uri.toString());
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
                                            progressBarVideo.setVisibility(View.GONE);
                                            Toast.makeText(chat.this, "Image sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarVideo.setVisibility(View.GONE);
                                Toast.makeText(chat.this, "Failure", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(chat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                DatabaseReference referencess = reference2.child("Chat").child(userID).push();
                final String messagePushIDsss = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDsss + "." + audioExtension);
                filePath.putFile(fileAudioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseUser.getUid());
                                hashMap.put("receiver", userID);
                                hashMap.put("message", "🎵 Audio");
                                hashMap.put("downloadURl", uri.toString());
                                hashMap.put("date", date);
                                hashMap.put("time", time);
                                hashMap.put("type", checker);
                                hashMap.put("fileName", name);
                                hashMap.put("fileExtension", audioExtension);
                                hashMap.put("fileSize", size);
                                hashMap.put("isseen", false);
                                hashMap.put("messagePushID", messagePushIDssssh);
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sentSound.start();
                                            progressBarAudio.setVisibility(View.GONE);
                                            Toast.makeText(chat.this, "Audio sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBarAudio.setVisibility(View.GONE);
                                Toast.makeText(chat.this, "Failure", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(chat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                DatabaseReference referencess = reference2.child("Chat").child(userID).push();
                final String messagePushIDssssh = referencess.getKey();
                final StorageReference filePath = storageReference.child(messagePushIDssssh + "." + extension);
                filePath.putFile(fileDocumentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chat").push();
                                String messagePushIDssssh = reference.getKey();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender", firebaseUser.getUid());
                                hashMap.put("receiver", userID);
                                hashMap.put("message", "📄 Document");
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
                                            Toast.makeText(chat.this, "Document sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(chat.this, "Failure", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(chat.this, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void blockUser(String uid){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BlockedUser").child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(chat.this, "Blocked", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(chat.this, "Failed: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void unBlockUser(String uid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BlockedUser").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.exists()){
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(chat.this, "Unblocked", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(chat.this, "Failure: " + e.getMessage(),  Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void checkIsBlocked(String uid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BlockedUser").orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.exists())
                    {
                        popupMenu.getMenu().getItem(R.id.block).setTitle("UnBlock");
                        isBlocked = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isCurrentUserBlockedOrNot(String uid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        databaseReference.child(uid).child("BlockedUser").orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.exists()){
                        Toast.makeText(chat.this, "You are blocked by the user", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        Toast.makeText(chat.this, "you can send message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
