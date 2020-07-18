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

public class login extends AppCompatActivity {

    private EditText phoneNumber, otp_code;
    private RelativeLayout send_otp, verify;
    private RelativeLayout resend;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String mVerificationId;
    CountryCodePicker ccp;
    String number;
    private ProgressDialog progressDialog, progressDialog2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        FirebaseApp.initializeApp(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog2 = new ProgressDialog(this);
        phoneNumber = findViewById(R.id.phone_number);
        otp_code = findViewById(R.id.enter_otp);
        send_otp = findViewById(R.id.send_otp);
        verify = findViewById(R.id.verify);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneNumber);

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
                Toast.makeText(login.this, "Verification Failed", Toast.LENGTH_SHORT).show();
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
                        startActivity(new Intent(login.this, MainActivity.class));
                    }

                }
                if (!task.isSuccessful()){
                    Toast.makeText(login.this, "Verification Failed", Toast.LENGTH_SHORT).show();
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
                        startActivity(new Intent(login.this, MainActivity.class));
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
}
