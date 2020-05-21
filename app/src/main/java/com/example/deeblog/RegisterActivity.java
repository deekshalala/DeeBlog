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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    EditText etEmailreg, etPassreg, etConPassreg;
    Button btnRegister, btnAlreadyAcc;

    FirebaseAuth auth;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        etEmailreg=findViewById(R.id.etEmailreg);
        etPassreg=findViewById(R.id.etPassreg);
        etConPassreg=findViewById(R.id.etConPassreg);
        btnRegister=findViewById(R.id.btnRegister);
        btnAlreadyAcc=findViewById(R.id.btnAlreadyAcc);

        auth=FirebaseAuth.getInstance();
        pd=new ProgressDialog(this);

        btnAlreadyAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Creating new Account...");
                String email=etEmailreg.getText().toString().trim();
                String password=etPassreg.getText().toString().trim();
                String conPass=etConPassreg.getText().toString().trim();

                if(email.isEmpty()|| password.isEmpty()|| conPass.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Fill all fields!", Toast.LENGTH_LONG).show();}
                    else
                        {
                            if(!password.equals(conPass)){
                                Toast.makeText(RegisterActivity.this, "Check Confirm Password!", Toast.LENGTH_LONG).show();
                            }
                            else{
                                pd.show();
                                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){

                                            pd.dismiss();
                                            Toast.makeText(RegisterActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, SetupActivity.class));
                                        }
                                        else{
                                            pd.dismiss();
                                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser!=null){
            sentToMain();

        }
    }

    private void sentToMain() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }
}
