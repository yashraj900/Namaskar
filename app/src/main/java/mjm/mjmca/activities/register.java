package mjm.mjmca.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import mjm.mjmca.MainActivity;
import mjm.mjmca.R;
import mjm.mjmca.ccp.CountryCodePicker;
import mjm.mjmca.model.UserModel;

public class register extends AppCompatActivity {

    private EditText phoneNumber, otp_code;
    private RelativeLayout send_otp, verify;
    private RelativeLayout resend;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String mVerificationId;
    CountryCodePicker ccp;
    String number;
    private ProgressDialog progressDialog, progressDialog2;

    private TextView login;


    private static final int STORAGE_REQUEST_PERMISSION = 124;
    private static final int CAMERA_REQUEST_CODE = 224;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 324;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 424;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri imageUri = null;
    private CircleImageView profile;
    private EditText name;
    StorageReference userProfileImageReference;
    String currentUserID;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!=null){
            currentUserID = firebaseAuth.getCurrentUser().getUid();
        }

        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(register.this, mjm.mjmca.activities.login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference();
        name = findViewById(R.id.name);
        profile = findViewById(R.id.profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog();
            }
        });
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        progressDialog = new ProgressDialog(this);
        progressDialog2 = new ProgressDialog(this);
        phoneNumber = findViewById(R.id.phone_number);
        otp_code = findViewById(R.id.enter_otp);
        send_otp = findViewById(R.id.send_otp);
        verify = findViewById(R.id.verify);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneNumber);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        resend = findViewById(R.id.resend_otp);
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPhoneVerification();
                resend.setVisibility(View.GONE);
                otp_code.setVisibility(View.VISIBLE);
                otp_code.setText("");
                verify.setVisibility(View.VISIBLE);
            }
        });



        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog2.setMessage("Verifying");
                progressDialog2.show();
                verify();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
                progressDialog.dismiss();
                progressDialog2.dismiss();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(register.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                resend.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
                progressDialog2.dismiss();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                number = ccp.getFullNumberWithPlus();
                mVerificationId = s;
                progressDialog.dismiss();




            }


        };

        send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPhoneVerification();
            }
        });
    }

    private void verify(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp_code.getText().toString());
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    progressDialog2.dismiss();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null){
                        UpdateProfile();
                    }

                }
                if (!task.isSuccessful()){
                    Toast.makeText(register.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    progressDialog2.dismiss();
                    resend.setVisibility(View.VISIBLE);
                    otp_code.setVisibility(View.GONE);
                    verify.setVisibility(View.GONE);
                }
            }
        });

    }

    private void signIn(PhoneAuthCredential phoneAuthCredential){
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null){
                        UpdateProfile();
                    }

                }
            }
        });
    }


    private void startPhoneVerification(){
        number = ccp.getFullNumberWithPlus();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
        progressDialog.setMessage("Sending sms verification code to " + number);
        progressDialog.show();
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
                profile.setImageURI(imageUri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                profile.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void UpdateProfile() {
        if (imageUri!=null){
            progressDialog.setMessage("Saving data");
            progressDialog.show();
            final StorageReference filePath = userProfileImageReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + ".jpg");
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String downloadUrl = uri.toString();
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            /*String username_String = name.getText().toString();*/
                            String name_String = name.getText().toString();
                            /*String about_String = about.getText().toString();*/
                            String phone = null;
                            if (user != null) {
                                phone = user.getPhoneNumber();
                            }
                            if (TextUtils.isEmpty(name_String)){
                                name.setFocusable(true);
                            }
                            else{
                                UserModel userModel = new UserModel(
                                        uid,
                                        "",
                                        name_String,
                                        "",
                                        phone,
                                        downloadUrl
                                );
                                reference.child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Toast.makeText(register.this, "Profile Saved", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String error = e.getMessage();
                                        Toast.makeText(register.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });


                }
            });
        }
        else{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            /*String username_String = name.getText().toString();*/
            String name_String = name.getText().toString();
            /*String about_String = about.getText().toString();*/
            String phone = null;
            if (user != null) {
                phone = user.getPhoneNumber();
            }
            if (TextUtils.isEmpty(name_String)){
                name.setFocusable(true);
            }
            else{
                UserModel userModel = new UserModel(
                        uid,
                        "",
                        name_String,
                        "",
                        phone,
                        ""
                );
                reference.child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(register.this, "Profile Saved", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String error = e.getMessage();
                        Toast.makeText(register.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


    }
}
