package com.aizaz.accessandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {
    TextView btn_signUp, btn_forgetPassword;
    private Button btn_logIn;
    private EditText inputPasswordLogin, inputEmailLogin;
    private FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    private ProgressDialog mProgress;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            preferenceManager = new PreferenceManager(getApplicationContext());
            if (preferenceManager.getBoolen(Constant.KEY_IS_SINGED_IN)) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }

            inputPasswordLogin = findViewById(R.id.inputPasswordLogin);
            inputEmailLogin = findViewById(R.id.inputEmailLogin);
            btn_logIn = findViewById(R.id.btn_login);

            btn_forgetPassword = findViewById(R.id.forgetpassword);
            btn_signUp = findViewById(R.id.textviewSignUp);

            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            mProgress = new ProgressDialog(LoginActivity.this);

            btn_logIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isConnected()) {
                        CheckCredentions();
                    } else {
                        Toast.makeText(LoginActivity.this, "Please make sure to connect to internet.", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            btn_signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(LoginActivity.this, Register.class));
                    finish();
                }
            });

            btn_forgetPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText resetMail = new EditText(view.getContext());
                    final AlertDialog.Builder passwordresetdialog = new AlertDialog.Builder(view.getContext());
                    passwordresetdialog.setTitle("Reset Password");
                    passwordresetdialog.setMessage("Enter your email to sent Password Reset Link.");
                    passwordresetdialog.setView(resetMail);


                    passwordresetdialog.setPositiveButton("Sent", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String emailaddress = resetMail.getText().toString();
                            if (isConnected()) {
                                mAuth.sendPasswordResetEmail(emailaddress).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(LoginActivity.this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Your email is not regestered.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(LoginActivity.this, "Please make sure to connect to Internet.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    passwordresetdialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    passwordresetdialog.show();
                }
            });
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public  boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    private void CheckCredentions() {
        String email = inputEmailLogin.getText().toString();
        String pasword = inputPasswordLogin.getText().toString();

        if (email.isEmpty() || !email.contains("@")) {
            showError(inputEmailLogin, "Email is not Valid.");
            Toast.makeText(LoginActivity.this, "Email is not Valid.", Toast.LENGTH_SHORT).show();
        } else if (pasword.isEmpty() || pasword.length() < 7) {
            showError(inputPasswordLogin, "Password must be 7 character.");
            Toast.makeText(LoginActivity.this, "Password must be 7 character.", Toast.LENGTH_SHORT).show();
        } else {

            mProgress.setTitle("Login");
            mProgress.setMessage("Please wait, while checking your credentional. ");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            signinFrom();
        }
    }

    private void signinFrom() {
        mAuth.signInWithEmailAndPassword(inputEmailLogin.getText().toString(), inputPasswordLogin.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser().isEmailVerified()) {

                                FirebaseFirestore database = FirebaseFirestore.getInstance();
                                database.collection(Constant.KEY_COLLECTION_USER)
                                        .whereEqualTo(Constant.KEY_EMAIL, inputEmailLogin.getText().toString())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                                    preferenceManager.putBoolen(Constant.KEY_IS_SINGED_IN, true);
                                                    preferenceManager.putString(Constant.KEY_USER_ID, documentSnapshot.getId());
                                                    preferenceManager.putString(Constant.KEY_FIRSTNAME, documentSnapshot.getString(Constant.KEY_FIRSTNAME));
                                                    preferenceManager.putString(Constant.KEY_LASTNAME, documentSnapshot.getString(Constant.KEY_LASTNAME));
                                                    preferenceManager.putString(Constant.KEY_EMAIL, documentSnapshot.getString(Constant.KEY_EMAIL));
                                                    preferenceManager.putBoolen(Constant.KEY_STATE, documentSnapshot.getBoolean(Constant.KEY_STATE));
                                                    preferenceManager.putString(Constant.KEY_GENERATE_CODE, documentSnapshot.getString(Constant.KEY_GENERATE_CODE));
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    mProgress.dismiss();
                                                    //Toast.makeText(LoginActivity.this, "Check your email address or password cannot match.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            } else {
                                mProgress.dismiss();
                                Toast.makeText(LoginActivity.this, "Please verified your email address.", Toast.LENGTH_SHORT).show();

                            }
                        } else {
                            mProgress.dismiss();
                            Toast.makeText(LoginActivity.this, "Your email or password is incorrect.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }

}