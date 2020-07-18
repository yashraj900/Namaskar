package mjm.mjmca.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.R;

public class EditGroup extends AppCompatActivity  {
    private ActionBar actionBar;
    private String groupID;

    private CircleImageView group_photo;
    private EditText groupName, groupDescription;
    private RelativeLayout createGroup;
    private static final int STORAGE_REQUEST_PERMISSION = 124;
    private static final int CAMERA_REQUEST_CODE = 224;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 324;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 424;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri imageUri = null;
    private Dialog progressBar;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    String currentUser = null;
    String name;

    private String groupname, groupPhoto, groupDescriptionString, groupid, groupTimeStamp, groupCreatedBy;
    String saveCurrentTime, saveCurrentDate;
    String timeStamp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_group);
        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        actionBar.setTitle("Edit Group");
        progressBar = new Dialog(this);
        if (progressBar.getWindow()!=null){
            progressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


        progressBar.setContentView(R.layout.creating);
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setCancelable(false);
        group_photo = findViewById(R.id.group_photo);
        groupName = findViewById(R.id.group_name);
        groupDescription = findViewById(R.id.group_description);
        createGroup = findViewById(R.id.create_group);
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        loadGroupInfo();
        group_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog();
            }
        });
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateGroup();
            }
        });

    }

    private void updateGroup() {
        progressBar.show();
        final String groupTitle = groupName.getText().toString().trim();
        final String description = groupDescription.getText().toString().trim();
        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Group name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null){
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupName", groupTitle);
            hashMap.put("groupDescription", description);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group");
            reference.child(groupID).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.dismiss();
                    Toast.makeText(EditGroup.this, "Group Status Updated...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditGroup.this, GroupInfo.class);
                    intent.putExtra("groupID", groupID);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.dismiss();
                    Toast.makeText(EditGroup.this, "Failure: " + e.getMessage() + " Try Again ", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            String fileName = "GroupProfile/" + "image" + timeStamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);
            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadUri = uriTask.getResult();
                    if (uriTask.isSuccessful()){

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("groupName", groupTitle);
                        hashMap.put("groupDescription", description);
                        hashMap.put("groupIcon", downloadUri.toString());
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group");
                        reference.child(groupID).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBar.dismiss();
                                Toast.makeText(EditGroup.this, "Group Status Updated...", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditGroup.this, GroupInfo.class);
                                intent.putExtra("groupID", groupID);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.dismiss();
                                Toast.makeText(EditGroup.this, "Failure: " + e.getMessage() + " Try Again ", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.dismiss();
                    Toast.makeText(EditGroup.this, "Failure" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadGroupInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group");
        databaseReference.orderByChild("groupId").equalTo(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    groupname = ""+ds.child("groupName").getValue();
                    groupPhoto = ""+ds.child("groupIcon").getValue();
                    groupDescriptionString = ""+ds.child("groupDescription").getValue();
                    groupid = ""+ds.child("groupId").getValue();
                    groupTimeStamp = ""+ds.child("groupTimeStamp").getValue();
                    groupCreatedBy = ""+ds.child("groupCreatedBy").getValue();
                    actionBar.setTitle(groupname);
                    groupDescription.setText(groupDescriptionString);
                    groupName.setText(groupname);
                    try {
                        Picasso.get().load(groupPhoto).placeholder(R.mipmap.account).into(group_photo);
                    }
                    catch (Exception e){
                        group_photo.setImageResource(R.mipmap.account);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        captureFromCamera();
                    }
                }
                else{
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }
            }
        }).show();
    }
    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_REQUEST_CODE);
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
            case STORAGE_REQUEST_PERMISSION:{
                if (grantResults.length > 0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE){
                imageUri = data.getData();
                group_photo.setImageURI(imageUri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                group_photo.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
