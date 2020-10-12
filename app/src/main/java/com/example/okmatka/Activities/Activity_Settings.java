package com.example.okmatka.Activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
    private TextInputEditText settings_EDT_username, settings_EDT_age,
             settings_EDT_experience, settings_EDT_favouriteBeach;
    private TextInputLayout settings_LAY_username, settings_LAY_age
            , settings_LAY_experience, settings_LAY_favouriteBeach;
    private FloatingActionButton setting_FAB_nameBtn, setting_FAB_ageBtn,
             setting_FAB_experience, setting_FAB_favouriteBeach;
    private Spinner settings_SPN_roll;
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
        setFloatingButtons();
        setSpinner();
        initFireBase();
        setListeners();

    }

    private void setSpinner() {
        ArrayAdapter<CharSequence> arrayAdapter =
                ArrayAdapter.createFromResource(this,R.array.Roll,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        settings_SPN_roll.setAdapter(arrayAdapter);
        settings_SPN_roll.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = String.valueOf(parent.getItemAtPosition(position));
                myRef.child(MyFireBase.KEYS.ROLL).setValue(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        myRef.addValueEventListener(mySettingsFireBaseListener());
    }

    @Override
    protected void onStop() {
        super.onStop();
        myRef.removeEventListener(mySettingsFireBaseListener());
    }

    private ValueEventListener mySettingsFireBaseListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User mySelf = snapshot.getValue(User.class);
                assert mySelf != null;
                //implementing my changes
                setting_LBL_userName.setText(mySelf.getName());
                updateMyPhoto(mySelf);
                updateMyEditTexts(mySelf);
                updateMySpinner(mySelf);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void updateMySpinner(User mySelf) {
        if(mySelf.getRoll().equalsIgnoreCase("attack"))
        settings_SPN_roll.setSelection(0);
        else
            settings_SPN_roll.setSelection(1);
    }

    private void updateMyPhoto(User mySelf) {
        if(mySelf.getImageURL().equalsIgnoreCase("default"))
            Glide.with(getApplicationContext()).load(R.drawable.general_user).into(setting_IMG_profilePic);
        else
            Glide.with(getApplicationContext()).load(mySelf.getImageURL()).into(setting_IMG_profilePic);
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
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
                    child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
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

    private View.OnClickListener myDetailsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (((String) view.getTag())){
                    case "profilePic":
                        selectImage();
                        break;
                    case "username" :
                        submitData(settings_LAY_username,settings_EDT_username,MyFireBase.KEYS.NAME,false);
                        break;
                    case "age":
                        submitData(settings_LAY_age, settings_EDT_age,MyFireBase.KEYS.AGE,true);
                        break;
                    case "experience" :
                        submitData(settings_LAY_experience, settings_EDT_experience,MyFireBase.KEYS.EXPERIENCE,false);
                        break;
                    case "favouriteBeach":
                        submitData(settings_LAY_favouriteBeach, settings_EDT_favouriteBeach,MyFireBase.KEYS.FAVOURITE_BEACH,false);
                        break;
                }
            }
        };
    }

    private void submitData(TextInputLayout textInputLayout, TextInputEditText editText, String fireBaseKey, boolean isInt) {
        String newValue = String.valueOf(editText.getText());

        if (!newValue.equals("")) {
            textInputLayout.setHint(newValue);
            if (isInt)
                myRef.child(fireBaseKey).setValue(Integer.parseInt(newValue));
            else
                myRef.child(fireBaseKey).setValue(newValue);
        } else
            MySignal.getInstance().showToast("You can not send empty messages");

        editText.setText("");
    }

    private void setListeners() {
        setting_IMG_profilePic.setOnClickListener(myDetailsListener());
        setting_FAB_nameBtn.setOnClickListener(myDetailsListener());
        setting_FAB_ageBtn.setOnClickListener(myDetailsListener());
        setting_FAB_experience.setOnClickListener(myDetailsListener());
        setting_FAB_favouriteBeach.setOnClickListener(myDetailsListener());
    }

    private void updateMyEditTexts(User mySelf) {
        settings_LAY_username.setHint(mySelf.getName());
        settings_LAY_age.setHint(String.valueOf(mySelf.getAge()));
        settings_LAY_experience.setHint(mySelf.getExperience());
        settings_LAY_favouriteBeach.setHint(mySelf.getFavouriteBeach());
    }

    private void initFireBase() {
        storageReference = FirebaseStorage.getInstance().getReference(MyFireBase.KEYS.UPLOADS);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().
                getReference(MyFireBase.KEYS.USERS_LIST).child(firebaseUser.getUid());
    }

    private void setFloatingButtons() {
        Glide.with(this).load(R.drawable.submit).into(setting_FAB_nameBtn);
        Glide.with(this).load(R.drawable.submit).into(setting_FAB_ageBtn);
        Glide.with(this).load(R.drawable.submit).into(setting_FAB_experience);
        Glide.with(this).load(R.drawable.submit).into(setting_FAB_favouriteBeach);
    }

    private void findViews() {
        setting_LBL_userName = findViewById(R.id.setting_LBL_userName);
        setting_IMG_profilePic = findViewById(R.id.setting_IMG_profilePic);

        settings_EDT_username = findViewById(R.id.settings_EDT_username);
        settings_EDT_age = findViewById(R.id.settings_EDT_age);
        settings_EDT_experience = findViewById(R.id.settings_EDT_experience);
        settings_EDT_favouriteBeach = findViewById(R.id.settings_EDT_favouriteBeach);

        setting_FAB_nameBtn = findViewById(R.id.setting_FAB_nameBtn);
        setting_FAB_ageBtn = findViewById(R.id.setting_FAB_ageBtn);
        setting_FAB_experience = findViewById(R.id.setting_FAB_experience);
        setting_FAB_favouriteBeach = findViewById(R.id.setting_FAB_favouriteBeach);

        settings_LAY_username = findViewById(R.id.settings_LAY_username);
              settings_LAY_age = findViewById(R.id.settings_LAY_age);
       settings_LAY_experience = findViewById(R.id.settings_LAY_experience);
   settings_LAY_favouriteBeach = findViewById(R.id.settings_LAY_favouriteBeach);

        settings_SPN_roll = findViewById(R.id.settings_SPN_roll);
    }
}

//todo change to roll selection