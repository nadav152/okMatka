package com.example.okmatka.Activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class Activity_Settings extends AppCompatActivity {

    private TextView setting_LBL_userName;
    private ImageView setting_IMG_profilePic;
    private DatabaseReference myRef;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> storageTask;
    private static final int IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViews();
        initFireBase();
        setListeners();
    }

    private void setListeners() {
        myRef.addValueEventListener(mySettingsListener());
        setting_IMG_profilePic.setOnClickListener(profilePicListener());
    }

    private View.OnClickListener profilePicListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        };
    }

    private ValueEventListener mySettingsListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User mySelf = snapshot.getValue(User.class);
                assert mySelf != null;
                setting_LBL_userName.setText(mySelf.getName());

                if(mySelf.getImageURL().equalsIgnoreCase("default"))
                    Glide.with(getApplicationContext()).load(R.drawable.general_user).into(setting_IMG_profilePic);
                else
                    Glide.with(getApplicationContext()).load(mySelf.getImageURL()).into(setting_IMG_profilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadMyImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imageUri != null){
            final StorageReference fileReference = storageReference.
                    child(System.currentTimeMillis() + "." + getFileExtention(imageUri));
            storageTask = fileReference.putFile(imageUri);
            storageTask.continueWithTask(continuationListener(fileReference))
                    .addOnCompleteListener(continuationCompleteListener(progressDialog))
                    .addOnFailureListener(continuationFailureListener(progressDialog));
        }else
            MySignal.getInstance().showToast("No Image Selected");
    }

    private Continuation<UploadTask.TaskSnapshot, Task<Uri>> continuationListener(final StorageReference fileReference) {
        return new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public Task<Uri> then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw Objects.requireNonNull(task.getException());
                return fileReference.getDownloadUrl();
            }
        };
    }

    private OnCompleteListener<Uri> continuationCompleteListener(final ProgressDialog progressDialog) {
        return new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downLoadUri = task.getResult();
                    String myUri = String.valueOf(downLoadUri);
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("imageURL", myUri);
                    myRef.updateChildren(hashMap);
                    progressDialog.dismiss();
                }else
                    MySignal.getInstance().showToast("failed adding picture");
            }
        };
    }

    private OnFailureListener continuationFailureListener(final ProgressDialog progressDialog) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MySignal.getInstance().showToast(e.getMessage());
                progressDialog.dismiss();
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null){
            imageUri = data.getData();
            if (storageTask != null && storageTask.isInProgress())
                MySignal.getInstance().showToast("Upload In Progress");
            else
                uploadMyImage();
        }
    }

    private void initFireBase() {
        storageReference = FirebaseStorage.getInstance().getReference("UPLOADS");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().
                getReference("USERS_LIST").child(firebaseUser.getUid());
    }

    private void findViews() {
        setting_LBL_userName = findViewById(R.id.setting_LBL_userName);
        setting_IMG_profilePic = findViewById(R.id.setting_IMG_profilePic);
    }
}

//todo change to roll selection