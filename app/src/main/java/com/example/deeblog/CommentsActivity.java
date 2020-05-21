package com.example.deeblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    EditText etAddComment;
    ImageView ivSend;
    String blogPostID,blogUserId;

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String currentUser;

    RecyclerView comment_list_view;
    List<Comments> commentsList;
    CommentsAdapter commentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        blogPostID=getIntent().getStringExtra("blog_post_id");
        blogUserId=getIntent().getStringExtra("blogUserId");
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        currentUser=auth.getCurrentUser().getUid();

        etAddComment=findViewById(R.id.etAddComment);
        ivSend=findViewById(R.id.ivSend);

        commentsList=new ArrayList<>();
        comment_list_view= (RecyclerView) findViewById(R.id.comment_list_view);
        commentAdapter=new CommentsAdapter(commentsList);
        comment_list_view.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
        comment_list_view.setAdapter(commentAdapter);
        comment_list_view.setHasFixedSize(true);




        //Retreiving Comments
        firebaseFirestore.collection("Posts").document(blogPostID).collection("Comments")
                .addSnapshotListener(CommentsActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){

                                if(documentChange.getType()==DocumentChange.Type.ADDED){
                                    Comments comments=documentChange.getDocument().toObject(Comments.class);
                                    commentsList.add(comments);
                                    commentAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });


        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentMessage= etAddComment.getText().toString().trim();
                if(!commentMessage.isEmpty()){
                    Map<String, Object> commentMap= new HashMap<>();
                    commentMap.put("message",commentMessage);
                    commentMap.put("user_id",currentUser);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts").document(blogPostID).collection("Comments")
                            .add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "Couldn't post comment!"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            else{
                                etAddComment.setText(" ");

                                Map<String,Object> notifMap=new HashMap<>();
                                notifMap.put("fromUser",currentUser);
                                notifMap.put("type","commented on");
                                notifMap.put("toUser",blogUserId);
                                notifMap.put("post",blogPostID);
                                notifMap.put("timestamp",FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Users").document(blogUserId).collection("Notifications")
                                        .add(notifMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(CommentsActivity.this, "NOtiff", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(CommentsActivity.this, "Please Add Comment!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
