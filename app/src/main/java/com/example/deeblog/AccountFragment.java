package com.example.deeblog;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    CircleImageView ivAcc;
    TextView tvAccUser;
    Button btnEdit, btnLogout;

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String current_user_id;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);

        ivAcc=view.findViewById(R.id.ivAcc);
        tvAccUser=view.findViewById(R.id.tvAccUser);
        btnEdit=view.findViewById(R.id.btnEdit);
        btnLogout=view.findViewById(R.id.btnLogout);

        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        current_user_id=auth.getCurrentUser().getUid();

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getActivity(), SetupActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        if(auth.getCurrentUser()!=null) {


            //Getting the username and thumbnail
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        tvAccUser.setText(task.getResult().getString("name"));
                        setThumbnail(task.getResult().getString("image"));

                    } else {
                        Toast.makeText(getActivity(), "Failed getting username", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return view;
    }

    private void setThumbnail(String image) {
        Picasso.get().load(image).placeholder(R.drawable.dp3).into(ivAcc);
    }

    private void logout() {

        Map<String, Object> tokenMapremove= new HashMap<>();
        tokenMapremove.put("token_id", "");
        firebaseFirestore.collection("Users").document(current_user_id).update(tokenMapremove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                auth.signOut();
                sentToLogin();
            }
        });
    }

    private void sentToLogin() {
        Intent intent= new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    //to hide search bar in action bar
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
    }
}
