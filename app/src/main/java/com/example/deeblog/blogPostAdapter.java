package com.example.deeblog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class blogPostAdapter extends RecyclerView.Adapter<blogPostAdapter.ViewHolder> implements Filterable{
    FirebaseFirestore firebaseFirestore;

    FirebaseAuth auth;
    List<blogPost> blogList;
    List<blogPost> blogListFull;
    List<Users> usersList;
    Context context;



    String searched="";

    public blogPostAdapter( List<blogPost> blogList, List<Users> usersList) {
        this.blogList=blogList;
        this.usersList=usersList;
        this.blogListFull=blogList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_layout,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        final String blogPostID = blogListFull.get(position).BlogPostId;
        final String currentUser = auth.getCurrentUser().getUid();

        holder.itemView.setTag(blogListFull.get(position));
        holder.tvTitle.setText(blogListFull.get(position).getTitle());
        holder.tvDesc.setText(blogListFull.get(position).getDesc());


        //Highlighting search results
        Spannable spanString = null;
        Spannable spanString1 = null;
        if ((searched != null) && (!searched.isEmpty())) {
            spanString = Spannable.Factory.getInstance().newSpannable(holder.tvTitle.getText());
            Pattern pattern = Pattern.compile(searched, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(holder.tvTitle.getText());
            while (matcher.find()) {
                spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }
        if (spanString != null) {
            holder.tvTitle.setText(spanString);
        }


        if ((searched != null) && (!searched.isEmpty())) {
            spanString1 = Spannable.Factory.getInstance().newSpannable(holder.tvDesc.getText());
            Pattern pattern = Pattern.compile(searched, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(holder.tvDesc.getText());
            while (matcher.find()) {
                spanString1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }
        if (spanString1 != null) {
            holder.tvDesc.setText(spanString1);
        }


        String image_url = blogListFull.get(position).getImage();
        holder.setBlogImage(image_url);
        long milliseconds = blogListFull.get(position).getTimestamp().getTime();
        String date = DateFormat.format("dd/MM/yyyy", new Date(milliseconds)).toString();
        holder.tvDate.setText(date);

        final String blogUserId = blogListFull.get(position).getUser();
        if (blogUserId.equals(currentUser)) {
            holder.ivDel.setVisibility(View.VISIBLE);
        }

        holder.tvName.setText(usersList.get(position).getName());
        holder.setThumbnail(usersList.get(position).getImage());

        if(auth.getCurrentUser()!=null){
        //Likes Feature
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts").document(blogPostID).collection("Likes")
                        .document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likeMap = new HashMap<>();
                            likeMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts").document(blogPostID).collection("Likes")
                                    .document(currentUser).set(likeMap);

                            Map<String, Object> notifMap = new HashMap<>();
                            notifMap.put("fromUser", currentUser);
                            notifMap.put("type", "liked");
                            notifMap.put("toUser", blogUserId);
                            notifMap.put("timestamp", FieldValue.serverTimestamp());
                            notifMap.put("post", blogPostID);
                            firebaseFirestore.collection("Users").document(blogUserId).collection("Notifications")
                                    .add(notifMap);
                        } else {

                            firebaseFirestore.collection("Posts").document(blogPostID).collection("Likes")
                                    .document(currentUser).delete();

                        }
                    }
                });


            }
        });

        //Get likes
        firebaseFirestore.collection("Posts").document(blogPostID).collection("Likes")
                .document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshot.exists()) {
                        holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.liked_foreground));
                    } else {
                        holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_not_foreground));
                    }
                }
            }
        });


        //Get Likes Count
        firebaseFirestore.collection("Posts").document(blogPostID).collection("Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int count = queryDocumentSnapshots.size();
                            holder.updateLikeCount(count);
                        } else {
                            holder.updateLikeCount(0);
                        }
                        }
                    }

                });

        //Go to Comments Activity for viewing comments
        holder.ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, CommentsActivity.class).putExtra("blog_post_id", blogPostID).putExtra("blogUserId", blogUserId));
            }
        });
        holder.tvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, CommentsActivity.class).putExtra("blog_post_id", blogPostID).putExtra("blogUserId", blogUserId));
            }
        });

        //Get Comment Number
        firebaseFirestore.collection("Posts").document(blogPostID).collection("Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                int countComment = queryDocumentSnapshots.size();
                                holder.updateCommentCount(countComment);
                            } else {
                                holder.updateCommentCount(0);
                            }
                        }
                    }

                });


        //Delete Button
        holder.ivDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Query query=firebaseFirestore.collection("Users").document(currentUser).collection("Notifications")
                                        .whereEqualTo("post",blogPostID);

                                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()) {
                                                for (DocumentSnapshot documentSnapshot: task.getResult()){
                                                    firebaseFirestore.collection("Users").document(currentUser).collection("Notifications")
                                                            .document(documentSnapshot.getId()).delete();
                                                }
                                            }

                                            }

                                    });

//
                                firebaseFirestore.collection("Posts").document(blogPostID).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                blogListFull.remove(position);
                                                usersList.remove(position);
                                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Couldn't delete the post " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });




                            }
                        }).setNegativeButton("Cancel", null)
                        .show();

            }
        });
    }
    }

    @Override
    public int getItemCount() {

        return blogListFull.size();

    }





    public class ViewHolder extends RecyclerView.ViewHolder{
         TextView tvTitle, tvDesc, tvName, tvDate;
         ImageView ivPost;
         CircleImageView ivThumb;
         View mview;

         TextView tvLike;
         ImageView ivLike;

         TextView tvComment;
         ImageView ivComment;

        ImageView ivDel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
            tvDesc=itemView.findViewById(R.id.tvDesc);
            tvTitle=itemView.findViewById(R.id.tvTitle);
            tvName=itemView.findViewById(R.id.tvName);
            tvDate=itemView.findViewById(R.id.tvDate);
            tvDate=itemView.findViewById(R.id.tvDate);

            ivLike=itemView.findViewById(R.id.ivLike);
            tvLike=itemView.findViewById(R.id.tvLike);

            ivComment=itemView.findViewById(R.id.ivComment);
            tvComment=itemView.findViewById(R.id.tvComment);

            ivDel=itemView.findViewById(R.id.ivDel);
        }

        public void setBlogImage(String downloadUri){
            ivPost=mview.findViewById(R.id.ivPost);
            Picasso.get().load(downloadUri).into(ivPost);

        }
        public void setThumbnail(String url){
            ivThumb=mview.findViewById(R.id.ivThumb);
            Picasso.get().load(url).placeholder(R.drawable.dp3).into(ivThumb);
        }


        public void updateLikeCount(int count) {
            tvLike.setText(count+" Likes");

        }

        public void updateCommentCount(int countComment) {
            tvComment.setText(countComment+ " Comments");
        }
    }

    @Override
    public Filter getFilter() {

        return exampleFilter;
    }

    public Filter exampleFilter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            searched= ((String) constraint);
            List<blogPost> filteredList= new ArrayList<>();
            if(constraint==null || constraint.length()==0){
                blogListFull=blogList;
            }
            else {

                String filterPattern = constraint.toString().toLowerCase().trim();
                for (blogPost post : blogList) {
                    if ((post.getTitle().toLowerCase().contains(filterPattern)) || (post.getDesc().toLowerCase().contains(filterPattern))) {
                        filteredList.add(post);
                    }
                }
                blogListFull=filteredList;
            }
            FilterResults filterResults=new FilterResults();
            filterResults.values=blogListFull;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            blogListFull=(ArrayList<blogPost>)results.values;
            notifyDataSetChanged();

        }


    };




}

