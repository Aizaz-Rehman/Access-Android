package com.aizaz.accessandroid;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aizaz.accessandroid.utilities.Constant;
import com.aizaz.accessandroid.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {




    private Button btnHelp;
    private final int CHANGE_CODE = 0;
    private Button btnCommand;
    private Button btnChangeCode, btnGenerateCode, btnLougOut;
    private ToggleButton btnToggle;
    Boolean check = false, state = false;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String userID = null;
    TextView txt_user_name, Forgot_Code;
    String Generate_Code ="12345", UserName, firstName, lastName;
    private PreferenceManager preferenceManager;
    String [] permission={
            "android.permission.READ_CALL_LOG",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_CONTACTS",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_CALL_LOG",
            "android.permission.SEND_SMS"

    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions(permission, 80);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        txt_user_name = findViewById(R.id.txt_Name);
        btnToggle = (ToggleButton) findViewById(R.id.btn_toogle);
        btnToggle.setOnCheckedChangeListener(this);
        btnGenerateCode = findViewById(R.id.btn_generate_code);
        btnGenerateCode.setOnClickListener(this);
        btnHelp = (Button) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(this);
        btnCommand = (Button) findViewById(R.id.btn_commond_list);
        btnCommand.setOnClickListener(this);
        btnChangeCode = (Button) findViewById(R.id.btn_codechange);
        btnChangeCode.setOnClickListener(this);
        Forgot_Code = findViewById(R.id.txt_forgot_code);
        Forgot_Code.setOnClickListener(this);
        btnLougOut = findViewById(R.id.btn_logout);
        btnLougOut.setOnClickListener(this);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constant.KEY_COLLECTION_USER)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        String myUserId = preferenceManager.getString(Constant.KEY_USER_ID);
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if (myUserId.equals(queryDocumentSnapshot.getId())) {
                                    firstName = queryDocumentSnapshot.getString(Constant.KEY_FIRSTNAME);
                                    lastName = queryDocumentSnapshot.getString(Constant.KEY_LASTNAME);
                                    UserName = firstName + " " + lastName;
                                    Generate_Code = queryDocumentSnapshot.getString(Constant.KEY_GENERATE_CODE);
                                    txt_user_name.setText(UserName);
                                    check = queryDocumentSnapshot.getBoolean(Constant.KEY_CHECK);
                                    state = queryDocumentSnapshot.getBoolean(Constant.KEY_STATE);
                                }
                            }
                        }
                    }
                });


        /*txt_user_name.setText(preferenceManager.getString(Constant.KEY_FIRSTNAME) +" "+ (preferenceManager.getString(Constant.KEY_LASTNAME)));*/
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {

                if (task.isSuccessful() && task.getResult() != null) {
                    sendFCMTokentodatabase(task.getResult().getToken());
                }
            }
        });

        setDefaultButtonChecked();
    }
    private void setDefaultButtonChecked() {

        // TODO Auto-generated method stub

        state = preferenceManager.getBoolen(Constant.KEY_STATE);
        //String value = getSharedPreferences();
        if (state.equals(true)) {
            btnToggle.setChecked(true);
        } else if (state.equals(false)) {
            btnToggle.setChecked(false);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub

        if (btnToggle.isChecked()) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> user = new HashMap<>();
            user.put(Constant.KEY_STATE, true);

            DocumentReference documentReference =
                    database.collection(Constant.KEY_COLLECTION_USER).document(
                            preferenceManager.getString(Constant.KEY_USER_ID)
                    );
            documentReference.update(Constant.KEY_STATE, true);
            preferenceManager.putBoolen(Constant.KEY_STATE, true);
        } else {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> user = new HashMap<>();
            user.put(Constant.KEY_STATE, false);

            DocumentReference documentReference =
                    database.collection(Constant.KEY_COLLECTION_USER).document(
                            preferenceManager.getString(Constant.KEY_USER_ID)
                    );
            documentReference.update(Constant.KEY_STATE, false);
            preferenceManager.putBoolen(Constant.KEY_STATE, false);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_generate_code:
                if (check == false) {
                    generateCode();
                } else {
                    Toast.makeText(this, "Your code already generated. Press forgot code or change code. ", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.txt_forgot_code:
                forgotCode();
                break;
            case R.id.btnHelp:
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case R.id.btn_commond_list:
                startActivity(new Intent(this, CommandActivity.class));
                break;
            case R.id.btn_codechange:
                // input dialog
                changeCode(CHANGE_CODE);
                //Toast.makeText(getApplicationContext(), ""+myPrefs.getString("code", null), Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_logout:

                LogOutDialog();
                break;

            default:
                break;
        }

    }

    private void LogOutDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setTitle("Log Out");
        alert.setMessage("Are you sure you want to logout!");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                DocumentReference documentReference =
                        database.collection(Constant.KEY_COLLECTION_USER).document(
                                preferenceManager.getString(Constant.KEY_USER_ID)
                        );
                HashMap<String, Object> updates = new HashMap<>();
                updates.put(Constant.KEY_FCM_TOKEN, FieldValue.delete());
                documentReference.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        preferenceManager.clearPreferences(Constant.KEY_IS_SINGED_IN);
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this, "Unable to sign out", Toast.LENGTH_SHORT).show();
                    }
                });
                /*Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                mAuth.signOut();*/
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                Toast.makeText(MainActivity.this, "Cancel button clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();
    }

    private void forgotCode() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setTitle("Forgot Code");
        alert.setMessage("Send Code To Your Email Address? ");

        // Set an EditText view to get user input
        final TextView Login_ID = new TextView(MainActivity.this);
        Login_ID.setText(preferenceManager.getString(Constant.KEY_EMAIL));
        Login_ID.setPadding(90, 0, 0, 0);
        final String emailaddress = preferenceManager.getString(Constant.KEY_EMAIL);
        final String codesend = preferenceManager.getString(Constant.KEY_GENERATE_CODE);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(Login_ID);
        //layout.addView(confirmedPassword);
        alert.setView(layout);

        alert.setPositiveButton("Sent", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                final String username = "accessphone435@gmail.com";
                final String password = "Pakistan.123";

                /*using GMail SMTP server*/
                /*Send Email in Java SMTP with TLS Authentication*/
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                try {

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(username));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(emailaddress));
                    message.setSubject("Access Android Phone Through SMS");
                    message.setText("Last Time Generated Code is : "
                            + "" + codesend);

                    new SendMailTask().execute(message);

                } catch (MessagingException mex) {
                    mex.printStackTrace();
                }

            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                Toast.makeText(MainActivity.this, "Cancel button clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();
    }

    private void generateCode() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setTitle("Generate Code");
        alert.setMessage("Please Give Code and Confirmed Code");

        // Set an EditText view to get user input
        final EditText password = new EditText(MainActivity.this);
        password.setHint("Code(Minimum 4 digits)");
        password.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final EditText confirmedPassword = new EditText(MainActivity.this);
        confirmedPassword.setHint("Confirmed Code");
        confirmedPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(80, 0, 40, 0);
        password.setLayoutParams(lp);
        confirmedPassword.setLayoutParams(lp);
        layout.addView(password);
        layout.addView(confirmedPassword);
        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
                final String pasword = password.getText().toString();
                String confirmdpasword = confirmedPassword.getText().toString();
                if (pasword.isEmpty() || pasword.length() < 4) {
                    showError(password, "Password(Must be 4 digits).");
                    Toast.makeText(MainActivity.this, "Password must be 4 character.", Toast.LENGTH_SHORT).show();
                } else if (confirmdpasword.isEmpty() || !confirmdpasword.equals(pasword)) {
                    showError(confirmedPassword, "Confirmed password must be matched.");
                    Toast.makeText(MainActivity.this, "Password must be mached.", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();

                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constant.KEY_GENERATE_CODE, password.getText().toString());
                    user.put(Constant.KEY_CHECK, true);

                    DocumentReference documentReference =
                            database.collection(Constant.KEY_COLLECTION_USER).document(
                                    preferenceManager.getString(Constant.KEY_USER_ID)
                            );
                    documentReference.update(Constant.KEY_CHECK, true);
                    documentReference.update(Constant.KEY_GENERATE_CODE, pasword)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    preferenceManager.putString(Constant.KEY_GENERATE_CODE, pasword);
                                    preferenceManager.putBoolen(Constant.KEY_CHECK, true);
                                    check = true;
                                    /*Toast.makeText(MainActivity.this, "Code generated successfully " + pasword, Toast.LENGTH_SHORT).show();*/
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this, "Code not generated.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                Toast.makeText(MainActivity.this, "Cancel button clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();

    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }


    private void changeCode(final int i) {
        // TODO Auto-generated method stub

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        alert.setTitle("Change Code");
        alert.setMessage("Please Give Old Code and New Code");
        alert.setCancelable(false);

        // Set an EditText view to get user input
        final EditText oldPassword = new EditText(MainActivity.this);
        oldPassword.setHint("Old Code");
        oldPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final EditText newPassword = new EditText(MainActivity.this);
        newPassword.setHint("New Code(Minimum 4 digits)");
        newPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60, 0, 40, 0);
        oldPassword.setLayoutParams(lp);
        newPassword.setLayoutParams(lp);
        layout.addView(oldPassword);
        layout.addView(newPassword);
        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String smsCode = preferenceManager.getString(Constant.KEY_GENERATE_CODE);
                // Do something with value!
                String oldpasword = oldPassword.getText().toString();
                String newpasword = newPassword.getText().toString();
                if (oldpasword.isEmpty() || !oldpasword.equals(smsCode)) {
                    showError(oldPassword, "Password does not match.");
                    Toast.makeText(MainActivity.this, "Password does not match with your old password.", Toast.LENGTH_SHORT).show();
                } else if (newpasword.isEmpty() || newpasword.length() < 4) {
                    showError(newPassword, "Password (Must be 4 digits).");
                    Toast.makeText(MainActivity.this, "Password (Must be 4 digits).", Toast.LENGTH_SHORT).show();
                } else {

                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constant.KEY_GENERATE_CODE, newPassword.getText().toString());
                    DocumentReference documentReference =
                            database.collection(Constant.KEY_COLLECTION_USER).document(
                                    preferenceManager.getString(Constant.KEY_USER_ID)
                            );
                    documentReference.update(Constant.KEY_GENERATE_CODE, newpasword);
                    preferenceManager.putString(Constant.KEY_GENERATE_CODE, newpasword);
                    Toast.makeText(MainActivity.this, "Succesfully added " + newpasword, Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                Toast.makeText(MainActivity.this, "Cancel button clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();
    }

    private void sendFCMTokentodatabase(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constant.KEY_COLLECTION_USER).document(
                        preferenceManager.getString(Constant.KEY_USER_ID)
                );
        documentReference.update(Constant.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        /*Toast.makeText(MainActivity.this, "Token added successfully", Toast.LENGTH_SHORT).show();*/
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                /* Toast.makeText(MainActivity.this, "Unable to send token.", Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    private class SendMailTask extends AsyncTask<Message, String, String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, null, "Sending mail", true, false);
        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
                return "Success";
            } catch (SendFailedException ee) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                return "error1";
            } catch (MessagingException e) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                return "error2";
            }

        }
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Success")) {

                super.onPostExecute(result);
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Mail Sent Successfully", Toast.LENGTH_LONG).show();
            } else if (result.equals("error1"))
                Toast.makeText(MainActivity.this, "Email Failure", Toast.LENGTH_LONG).show();
            else if (result.equals("error2"))
                Toast.makeText(MainActivity.this, "Email Sent problem2", Toast.LENGTH_LONG).show();

        }
    }
}