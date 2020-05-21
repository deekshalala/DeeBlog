package com.example.deeblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPass;
    Button btnLogin, btnNewAcc;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        pd=new ProgressDialog(this);

        etEmail=findViewById(R.id.etEmail);
        etPass=findViewById(R.id.etPass);
        btnLogin=findViewById(R.id.btnLogin);
        btnNewAcc=findViewById(R.id.btnNewAcc);

        btnNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Logging in...");
                pd.show();
                String email=etEmail.getText().toString().trim();
                String password=etPass.getText().toString().trim();

                if(email.isEmpty()||password.isEmpty()){
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Fill all fields!", Toast.LENGTH_SHORT).show();
                }
                else{
                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){



                                        final String currentUser=auth.getCurrentUser().getUid();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        String token_id=task.getResult().getToken();
                                        Map<String, Object> tokenMap=new HashMap<>();
                                        tokenMap.put("token_id",token_id);

                                        firebaseFirestore.collection("Users").document(currentUser).update(tokenMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //Toast.makeText(LoginActivity.this, "Id created", Toast.LENGTH_SHORT).show();
                                                        sentToMain();
                                                    }
                                                });
                                    }
                                });



                            }else
                                {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                           }
                            pd.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //checking if the user has logged in or not
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser!=null){
            sentToMain();
        }
    }

    private void sentToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
