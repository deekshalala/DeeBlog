package com.example.deeblog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.Sets;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FloatingActionButton btnAddPost;
    FirebaseFirestore firebaseFirestore;

    String current_user_id;
    BottomNavigationView mainBottomNav;

    HomeFragment homeFragment;
    NotificationsFragment notificationsFragment;
    AccountFragment accountFragment;
    FrameLayout mainContainer;



    public String CHANNEL_ID="DEE_CHANNEL";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Dee_Channel",NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }


        auth = FirebaseAuth.getInstance();
        btnAddPost = findViewById(R.id.btnAddPost);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mainContainer=findViewById(R.id.mainContainer);




        if (auth.getCurrentUser() != null) {

            //Fragments
            mainBottomNav = findViewById(R.id.mainBottomNav);
            homeFragment=new HomeFragment();
            notificationsFragment=new NotificationsFragment();
            accountFragment=new AccountFragment();

            initializeFragment();


            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                    switch (item.getItemId()) {
                            case R.id.action_home:
                            overridePendingTransition(0,0);
                            replaceFragment(homeFragment);
                            return true;

                            case R.id.action_notif:
                            overridePendingTransition(0,0);
                            replaceFragment(notificationsFragment);
                            return true;

                            case R.id.action_account:
                            overridePendingTransition(0,0);
                            replaceFragment(accountFragment);
                            return true;

                            default:
                            return false;

                    }

                }
            });


            btnAddPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, NewPostActivity.class));
                }
            });


        }
    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.mainContainer,homeFragment);
        fragmentTransaction.add(R.id.mainContainer,notificationsFragment);
        fragmentTransaction.add(R.id.mainContainer,accountFragment);

        fragmentTransaction.hide(notificationsFragment);
        fragmentTransaction.hide(accountFragment);
        fragmentTransaction.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user= auth.getCurrentUser();
        if(user==null){
            sentToLogin();
        }
        else{

            current_user_id=auth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                if(!task.getResult().exists()){
                                    startActivity(new Intent(MainActivity.this, SetupActivity.class));
                                    finish();
                                }
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Error! "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sentToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_settings: startActivity(new Intent(MainActivity.this, SetupActivity.class));
            finish();
            return true;

            default: return false;
        }
    }






    private void replaceFragment(Fragment fragment){


                FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();

                if(fragment==homeFragment){
                    fragmentTransaction.hide(accountFragment);
                    fragmentTransaction.hide(notificationsFragment);
                    btnAddPost.setVisibility(View.VISIBLE);
                }

        if(fragment==notificationsFragment){
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(homeFragment);

            btnAddPost.setVisibility(View.INVISIBLE);
        }

        if(fragment==accountFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationsFragment);
            btnAddPost.setVisibility(View.INVISIBLE);
        }

                fragmentTransaction.show(fragment);
                fragmentTransaction.commit();
    }


}




