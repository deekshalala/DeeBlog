package com.example.deeblog;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {
    TextView tvHeadNotif;
    RecyclerView notif_list_view;
    public List<Notifications> notificationsList;
    NotificationsAdapter notificationsAdapter;

    FirebaseAuth auth;
    String currentUser;
    FirebaseFirestore firebaseFirestore;


    public NotificationsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        setHasOptionsMenu(true);

        tvHeadNotif = view.findViewById(R.id.tvHeadNotif);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        notif_list_view = (RecyclerView) view.findViewById(R.id.notif_list_view);
        notificationsList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationsList,getContext());
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,true);
        layoutManager.setStackFromEnd(true);
        notif_list_view.setLayoutManager(layoutManager);
        notif_list_view.setAdapter(notificationsAdapter);
        notif_list_view.setHasFixedSize(true);


        firebaseFirestore.collection("Users").document(currentUser).collection("Notifications")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                tvHeadNotif.setText("Notifications");
                                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                        Notifications notifications = documentChange.getDocument().toObject(Notifications.class);
                                        notificationsList.add(notifications);
                                        notificationsAdapter.notifyDataSetChanged();
                                    }
                                }

                            }
                            else {
                                tvHeadNotif.setText("No Notifications!");
                            }
                        }
                    }
                });


        return  view;
    }

    //to hide search bar in action bar
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        notificationsAdapter.notifyDataSetChanged();
    }
}
