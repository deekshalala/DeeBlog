package com.example.deeblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    ImageView ivImage;
    EditText etTitle, etDesc;
    Button btnPostBlog;
    
    ProgressDialog pd;
    Uri postImageUri=null;
    String downloadUri;

    FirebaseFirestore firebaseFirestore;
    StorageReference sref;
    FirebaseAuth auth;
    String currentUser;

     File compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        
        ivImage=findViewById(R.id.ivImage);
        etTitle=findViewById(R.id.etTitle);
        etDesc=findViewById(R.id.etDesc);
        btnPostBlog=findViewById(R.id.btnPostBlog);
        pd=new ProgressDialog(this);

        sref= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser().getUid();

       ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(2,1)
                        .setFixAspectRatio(true)
                        .setRequestedSize(300,300)
                        .start(NewPostActivity.this);
            }
        });
        
        btnPostBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title=etTitle.getText().toString().trim();
                final String desc=etDesc.getText().toString().trim();
                if(title.isEmpty()|| desc.isEmpty()|| postImageUri==null){

                    Toast.makeText(NewPostActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }else{
                    pd.setMessage("Posting Blog...");
                    pd.show();

                    final String random= UUID.randomUUID().toString();
                    final StorageReference path=sref.child("post_images").child(random+ ".jpg");
                    path.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                     downloadUri = uri.toString();

                                    File imageFile= new File(postImageUri.getPath());
                                    try {
                                        compressedImageFile= new Compressor(NewPostActivity.this)
                                                .setMaxHeight(300)
                                                .setMaxWidth(150)
                                                .setQuality(8)
                                                .compressToFile(imageFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    final StorageReference thumbFilePath= sref.child("post_images/thumbs").child(random+".jpg");
                                    thumbFilePath.putFile(Uri.fromFile(imageFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            thumbFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String downloadUriThumb= uri.toString();
                                                    Map<String, Object> postMap= new HashMap<>();
                                                    postMap.put("image",downloadUri);
                                                    postMap.put("thumb",downloadUriThumb);
                                                    postMap.put("title",title);
                                                    postMap.put("desc",desc);
                                                    postMap.put("user",currentUser);
                                                    postMap.put("timestamp",FieldValue.serverTimestamp());

                                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if(task.isSuccessful()){
                                                                pd.dismiss();
                                                                Toast.makeText(NewPostActivity.this, "Blog Posted!", Toast.LENGTH_SHORT).show();
                                                                startActivity(new Intent(NewPostActivity.this,MainActivity.class));
                                                                finish();
                                                            }
                                                            else{
                                                                Toast.makeText(NewPostActivity.this, "Firestore Error! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pd.dismiss();
                                                    Toast.makeText(NewPostActivity.this, "Cannot get Url of thumbnail"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(NewPostActivity.this, "Cannot upload thumbnail! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(NewPostActivity.this, "Cannot get Url of Image"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(NewPostActivity.this, "Cannot upload image! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri=result.getUri();
                ivImage.setImageURI(postImageUri);
                

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
