package com.aizaz.accessandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aizaz.accessandroid.utilities.Constant;
import com.aizaz.accessandroid.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Register<TAG> extends AppCompatActivity {

    TextView btn_signIn;
    private EditText inputFirstname, inputLastname,
            inputEmail, inputPassword, inputConfirmedPassword;
    private Button btn_Register;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        preferenceManager = new PreferenceManager(getApplicationContext());
        btn_signIn = findViewById(R.id.textviewSigIn);
        inputFirstname = findViewById(R.id.inputFirstName);
        inputLastname = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmedPassword = findViewById(R.id.inputConfirmedPassword);
        btn_Register = findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        //firestore = FirebaseFirestore.getInstance();
        mProgress = new ProgressDialog(Register.this);
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    CheckCredentions();
                } else {
                    Toast.makeText(Register.this, "Please make sure to connect to internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, LoginActivity.class));
            }
        });
    }
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    private void CheckCredentions() {

        String firstname = inputFirstname.getText().toString();
        String lastname = inputLastname.getText().toString();
        String email = inputEmail.getText().toString();
        String pasword = inputPassword.getText().toString();
        String confirmdpasword = inputConfirmedPassword.getText().toString();

        if (firstname.isEmpty() || inputFirstname.length() < 3) {
            showError(inputFirstname, "LoginID length must be 3 character. ");
        } else if (lastname.isEmpty() || inputLastname.length() <3) {
            showError(inputLastname,  "Last name length must be 3 character.");
        } else if (email.isEmpty() || !email.contains("@")) {
            showError(inputEmail, "Email is not Valid.");
        } else if ((pasword.isEmpty() || pasword.length() < 7) ) {
            showError(inputPassword, "Password must be 7 character.");
        } else if (confirmdpasword.isEmpty() || !confirmdpasword.equals(pasword)) {
            showError(inputConfirmedPassword, "Password must be matched.");
        } else {
            //signUp();
            mProgress.setTitle("Registration");
            mProgress.setMessage("Please wait, while registration completed");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();


            mAuth.createUserWithEmailAndPassword(email, pasword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                                        HashMap<String, Object> user = new HashMap<>();
                                        user.put(Constant.KEY_FIRSTNAME, inputFirstname.getText().toString());
                                        user.put(Constant.KEY_LASTNAME, inputLastname.getText().toString());
                                        user.put(Constant.KEY_EMAIL, inputEmail.getText().toString());
                                        user.put(Constant.KEY_CHECK, false);
                                        user.put(Constant.KEY_STATE, false);
                                        database.collection(Constant.KEY_COLLECTION_USER)
                                                .add(user)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {

                                                        preferenceManager.putBoolen(Constant.KEY_IS_SINGED_IN, false);
                                                        preferenceManager.putString(Constant.KEY_FIRSTNAME, inputFirstname.getText().toString());
                                                        preferenceManager.putString(Constant.KEY_LASTNAME, inputLastname.getText().toString());
                                                        preferenceManager.putString(Constant.KEY_EMAIL, inputEmail.getText().toString());
                                                        preferenceManager.putString(Constant.KEY_USER_ID, documentReference.getId());
                                                        preferenceManager.putBoolen(Constant.KEY_CHECK, false);
                                                        preferenceManager.putBoolen(Constant.KEY_STATE, false);
                                                        mProgress.dismiss();
                                                        Intent intent = new Intent(Register.this, LoginActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                        Toast.makeText(Register.this, "Registered Successfully! Please check your email for verification.", Toast.LENGTH_LONG).show();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                mProgress.dismiss();
                                                Toast.makeText(Register.this, "Registration failed. Please check your email address.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });

                    } else {
                        mProgress.dismiss();
                        Toast.makeText(Register.this, "The email address already register.", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
}
