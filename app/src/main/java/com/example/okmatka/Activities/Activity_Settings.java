package com.example.okmatka.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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

import java.util.Objects;

public class Activity_Settings extends AppCompatActivity {

    private TextView setting_EDT_rate;
    private ImageView setting_IMG_profilePic;
    private TextInputEditText settings_EDT_username, settings_EDT_age,
             settings_EDT_experience, settings_EDT_favouriteBeach;
    private TextInputLayout settings_LAY_username, settings_LAY_age
            , settings_LAY_experience, settings_LAY_favouriteBeach, settings_LAY_roll;
    private MaterialButton setting_BTN_submit;
    private AutoCompleteTextView settings_LST_roll;
    private DatabaseReference myRef;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> storageTask;
    private User mySelf;
    private String myUri = "";
    private static final int IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViews();
        setAutoCompleteTextView();
        initFireBase();
        setListeners();

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStoragePermissions();
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
                mySelf = snapshot.getValue(User.class);
                assert mySelf != null;
                //implementing my changes
                updateMyPhoto(mySelf);
                updateMyEditTexts(mySelf);
                updateMySpinner(mySelf);
                updateMyRate(mySelf);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void updateMyRate(User user) {
        String myRate = "Your Rate : " + user.getRate() / user.getNumberOfReviews();
        setting_EDT_rate.setText(myRate);
    }

    private void selectImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, IMAGE_REQUEST);
        }
        else
            MySignal.getInstance().showToast("you do not have permissions\nfor uploading photos");
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
                    myUri = String.valueOf(downLoadUri);
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

    private void setAutoCompleteTextView() {
        String[] eventList = {"Defense", "Attack"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(Activity_Settings.this,R.layout.list_item, eventList);
            settings_LST_roll.setAdapter(adapter);

    }

    private View.OnClickListener myDetailsListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().equals("profilePic"))
                        selectImage();
                else
                    uploadUserInformation();

            }
    };

    private void uploadUserInformation() {
        //uploading image
        if (!myUri.equals(""))
            myRef.child(MyFireBase.KEYS.IMG_URL).setValue(myUri);

        //uploading the rest user info
        submitData(settings_LAY_username,settings_EDT_username,
                    MyFireBase.KEYS.NAME,"Name - ",false);

        submitData(settings_LAY_age, settings_EDT_age,
                    MyFireBase.KEYS.AGE,"Age - ",true);

        submitData(settings_LAY_experience, settings_EDT_experience,
                    MyFireBase.KEYS.EXPERIENCE,"Experience - ",false);

        submitData(settings_LAY_favouriteBeach, settings_EDT_favouriteBeach,
                    MyFireBase.KEYS.FAVOURITE_BEACH,"Favourite Beach - ",false);

        String autoSelectText = String.valueOf(settings_LST_roll.getText());
        if (!autoSelectText.equals(""))
            myRef.child(MyFireBase.KEYS.ROLL).setValue(autoSelectText);
    }


    private void submitData(TextInputLayout textInputLayout, TextInputEditText editText, String fireBaseKey, String entryStr, boolean isInt) {
        String newValue = String.valueOf(editText.getText());

        if (!newValue.equals("")) {
            //if user edit his age
            if (isInt)
                if (android.text.TextUtils.isDigitsOnly(newValue))
                myRef.child(fireBaseKey).setValue(Integer.parseInt(newValue));
                else {
                    MySignal.getInstance().showToast("Age must be numeric");
                    return;
                }
            else
            //user edit his other info
                myRef.child(fireBaseKey).setValue(newValue);

            textInputLayout.setHint(entryStr + newValue);
        }
        editText.setText("");
    }

    private void setListeners() {
        setting_IMG_profilePic.setOnClickListener(myDetailsListener);
        setting_BTN_submit.setOnClickListener(myDetailsListener);
    }

    private void updateMyEditTexts(User mySelf) {
        settings_LAY_username.setHint("Name - " + mySelf.getName());
        settings_LAY_age.setHint("Age - " + mySelf.getAge());
        settings_LAY_experience.setHint("Experience - " + mySelf.getExperience());
        settings_LAY_favouriteBeach.setHint("Favourite Beach - " + mySelf.getFavouriteBeach());
    }

    private void updateMySpinner(User mySelf) {
        if(mySelf.getRoll().equalsIgnoreCase("attack"))
            settings_LAY_roll.setHint("Roll - Attack");
        else
            settings_LAY_roll.setHint("Roll - Defense");
    }

    private void updateMyPhoto(User mySelf) {
        if(mySelf.getImageURL().equalsIgnoreCase("default"))
            Glide.with(getApplicationContext()).load(R.drawable.general_user).into(setting_IMG_profilePic);
        else
            Glide.with(getApplicationContext()).load(mySelf.getImageURL()).into(setting_IMG_profilePic);
    }

    private void initFireBase() {
        storageReference = FirebaseStorage.getInstance().getReference(MyFireBase.KEYS.UPLOADS);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().
                getReference(MyFireBase.KEYS.USERS_LIST).child(firebaseUser.getUid());
    }

    private void checkStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }

    //checking if the the user gave permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                MySignal.getInstance().showToast("Some functions may not work..");
        }
    }

    private void findViews() {
        setting_IMG_profilePic = findViewById(R.id.setting_IMG_profilePic);
        setting_EDT_rate = findViewById(R.id.setting_EDT_rate);

        settings_EDT_username = findViewById(R.id.settings_EDT_username);
        settings_EDT_age = findViewById(R.id.settings_EDT_age);
        settings_EDT_experience = findViewById(R.id.settings_EDT_experience);
        settings_EDT_favouriteBeach = findViewById(R.id.settings_EDT_favouriteBeach);

        settings_LAY_username = findViewById(R.id.settings_LAY_username);
        settings_LAY_age = findViewById(R.id.settings_LAY_age);
        settings_LAY_experience = findViewById(R.id.settings_LAY_experience);
        settings_LAY_favouriteBeach = findViewById(R.id.settings_LAY_favouriteBeach);
        settings_LAY_roll = findViewById(R.id.settings_LAY_roll);

        setting_BTN_submit = findViewById(R.id.setting_BTN_submit);
        settings_LST_roll = findViewById(R.id.settings_LST_roll);
    }
}
