package com.example.deeblog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    List<Comments> commentsList;
    Context context;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;


    public CommentsAdapter(List<Comments> commentsList){
        this.commentsList=commentsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout,parent,false);
        context=parent.getContext();
        firebaseFirestore= FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        holder.itemView.setTag(commentsList.get(position));
        String commentMessage=commentsList.get(position).getMessage();
        holder.setMessage(commentMessage);
        String user=commentsList.get(position).getUser_id();

        if(auth.getCurrentUser()!=null) {


            //Getting the username and thumbnail
            firebaseFirestore.collection("Users").document(user).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        holder.tvUser.setText(task.getResult().getString("name"));
                        holder.setThumbnail(task.getResult().getString("image"));

                    } else {
                        Toast.makeText(context, "Failed getting username", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if(commentsList!=null){
        return commentsList.size();}
        else{
            return 0;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

       CircleImageView ivThumbComment;
     TextView tvUser, tvCommentDisplay;
        View mview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
            tvUser=itemView.findViewById(R.id.tvUser);
            tvCommentDisplay=itemView.findViewById(R.id.tvCommentDisplay);
        }

        public void setMessage(String commentMessage) {
           tvCommentDisplay.setText(commentMessage+"");
        }

        public void setThumbnail(String image) {
            ivThumbComment=mview.findViewById(R.id.ivThumbComment);
            Picasso.get().load(image).placeholder(R.drawable.dp3).into(ivThumbComment);
        }
    }
}
