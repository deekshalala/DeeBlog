package com.example.deeblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    CircleImageView displayPic;
    private static final int REQ_EXTERNAL_STORAGE=4;

    FirebaseAuth auth;
    StorageReference sref;
    StorageReference image_path;
    FirebaseFirestore firebaseFirestore;

    String user_name;
    String user_id;

    Boolean isChanged=false;

    EditText etName;
    Button btnSave;

    Uri resultUri;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Account Settings");

        auth=FirebaseAuth.getInstance();
        sref= FirebaseStorage.getInstance().getReference().child("Users");
        firebaseFirestore=FirebaseFirestore.getInstance();
        pd=new ProgressDialog(this);

        user_id=auth.getCurrentUser().getUid();

        displayPic=findViewById(R.id.displayPic);
        etName=findViewById(R.id.etName);
        btnSave=findViewById(R.id.btnSave);


        pd.setMessage("Loading...");
        pd.show();
        btnSave.setEnabled(false);
        //retrieving data from firestore to image pic and name space

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                        String name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");
                        resultUri=Uri.parse(image);
                        etName.setText(name);
                        Picasso.get().load(image).placeholder(R.drawable.dp3).into(displayPic);
                    }

                }
                else{

                    Toast.makeText(SetupActivity.this, "Firestore Retrieve Data Error"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
                btnSave.setEnabled(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(SetupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        displayPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED)||(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED)){

                    ActivityCompat.requestPermissions(SetupActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQ_EXTERNAL_STORAGE);

                }
                else{
                    selectImage();
                }

            }
        });



        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name=etName.getText().toString().trim();
                if (user_name.isEmpty() || resultUri == null) {
                         Toast.makeText(SetupActivity.this, "Please provide all information!", Toast.LENGTH_SHORT).show();
                     }
                else {
                    if(isChanged) {
                         user_id = auth.getCurrentUser().getUid();

                         image_path = sref.child("profile_images").child(user_id + ".jpg");
                         image_path.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                             @Override
                             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                 image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                     @Override
                                     public void onSuccess(Uri uri) {
                                         Uri url = uri;
                                         storeFirestore(url, user_name);


                                     }}).addOnFailureListener(new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {
                                         pd.dismiss();
                                         Toast.makeText(SetupActivity.this, "Cannot get Url"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                     }
                                 });

                             }
                         }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 pd.dismiss();
                                 Toast.makeText(SetupActivity.this, "Cannot upload image! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                             }
                         });


                     }
                    else{
                        Uri url=resultUri;
                        storeFirestore(url,user_name);
                    }
                    pd.setMessage("Saving Settings...");
                    pd.show();

                 }




            }



    });





    }

    private void storeFirestore(Uri url, final String user_name) {
        final Uri download_url;
        if(url!=null) {
            download_url = url;
        }else {
            download_url=resultUri;
        }

               final Map<String, String> userMap = new HashMap<>();
                  userMap.put("name", user_name);
                  userMap.put("image", download_url.toString());
                  FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful()) {
                    String token_id = task.getResult().getToken();
                    userMap.put("token_id", token_id);
                }
                else {
                    Toast.makeText(SetupActivity.this, "No token created", Toast.LENGTH_SHORT).show();
                }
            }
        });

                  firebaseFirestore.collection("Users").document(user_id).set(userMap)
                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  if (task.isSuccessful()) {
                                      pd.dismiss();
                                      Toast.makeText(SetupActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                                      startActivity(new Intent(SetupActivity.this, MainActivity.class));
                                      finish();
                                  } else {
                                      Toast.makeText(SetupActivity.this, "Cannot save" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                  }
                              }
                          });




    }






    private void selectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .setRequestedSize(300,300)
                .setFixAspectRatio(true)
                .start(SetupActivity.this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==REQ_EXTERNAL_STORAGE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else {
                Toast.makeText(this, "We need permission to set your profile image!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                 resultUri= result.getUri();
                 displayPic.setImageURI(resultUri);
                 isChanged=true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
