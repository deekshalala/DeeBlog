package com.example.deeblog;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    Context context;
    public List<Notifications> notificationsList;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;

    String  from_user_id, post;


    public NotificationsAdapter(List<Notifications> notificationsList, Context context){
        this.notificationsList=notificationsList;
        this.context=context;
    }

    public List<Notifications> getNotificationsList() {
        return notificationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notif_layout,parent,false);

        return new NotificationsAdapter.ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull final NotificationsAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        firebaseFirestore= FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        holder.itemView.setTag(notificationsList.get(position));
        from_user_id = notificationsList.get(position).getFromUser();
        post=notificationsList.get(position).getPost();

          // Toast.makeText(context, from_user_id+ " "+ type, Toast.LENGTH_LONG).show();



            firebaseFirestore.collection("Users").document(from_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        holder.setThumbnail(task.getResult().getString("image"));
                        String name = task.getResult().getString("name");
                        if (!notificationsList.get(holder.getAdapterPosition()).getType().isEmpty()) {
                            if (notificationsList.get(holder.getAdapterPosition()).getType().equals("liked")) {
                                holder.tvNotifDisplay.setText(name+ " liked your post!");
                            }
                            else if( notificationsList.get(holder.getAdapterPosition()).getType().equals("commented on")) {
                                holder.tvNotifDisplay.setText(name + " commented on your post!");
                            }
                        }

                    } else {
                        Toast.makeText(context, "Failed getting data", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            firebaseFirestore.collection("Posts").document(post).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        String pic=task.getResult().getString("image");
                        holder.setIvPostThumb(pic);
                }
            }});


    }

    @Override
    public int getItemCount() {
        if(!notificationsList.isEmpty()) {
            return notificationsList.size();
        }
        else{
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView ivThumbNotif;
        TextView tvNotifDisplay;
        ImageView ivPostThumb;

        View mview;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;

            ivThumbNotif=itemView.findViewById(R.id.ivThumbNotif);
            tvNotifDisplay=itemView.findViewById(R.id.tvNotifDisplay);
            ivPostThumb=itemView.findViewById(R.id.ivPostThumb);
        }

        public void setThumbnail(String image) {
            ivThumbNotif=mview.findViewById(R.id.ivThumbNotif);
            Picasso.get().load(image).placeholder(R.drawable.dp3).into(ivThumbNotif);
        }

        public void setIvPostThumb(String pic){
            Picasso.get().load(pic).placeholder(R.drawable.dp3).into(ivPostThumb);
        }
    }
    }
