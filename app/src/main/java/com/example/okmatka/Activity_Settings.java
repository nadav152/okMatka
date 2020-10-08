//package com.example.okmatka;
//
//import android.content.SharedPreferences;
//import android.os.Build;
//import android.os.Bundle;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.preference.PreferenceFragmentCompat;
//
//import com.google.firebase.database.DatabaseReference;
//
//import java.util.Objects;
//import java.util.regex.Pattern;
//
//public class Activity_Settings extends AppCompatActivity {
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings);
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.settings_LAY_settings, new SettingsFragment())
//                .commit();
//    }
//
//    public static class SettingsFragment extends PreferenceFragmentCompat {
//
//        private User mySelf;
//        private SharedPreferences.OnSharedPreferenceChangeListener listener;
//        DatabaseReference reference;
//
//        @Override
//        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.root_preferences, rootKey);
//            mySelf = MySP.getInstance().getObject(MySP.KEYS.CURRENT_USER, User.class);
//            setListener();
//        }
//
//        private void setListener() {
//            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
//                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//                @Override
//                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//                    savePreferenceChange(sharedPreferences, key);
//                }
//            };
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//        private void savePreferenceChange(SharedPreferences sharedPreferences, String key) {
//
//            switch (key) {
//                case MySP.KEYS.NAME:
//                    mySelf.setName(sharedPreferences.getString(key, ""));
//                    break;
//                case MySP.KEYS.lAST_NAME:
//                    mySelf.setLastName(sharedPreferences.getString(MySP.KEYS.lAST_NAME, ""));
//                    break;
//                case MySP.KEYS.FAVOURITE_BEACH:
//                    mySelf.setFavouriteBeach(sharedPreferences.getString(MySP.KEYS.FAVOURITE_BEACH, ""));
//                    break;
//                case MySP.KEYS.EMAIL:
//                    String email = sharedPreferences.getString(MySP.KEYS.EMAIL, "");
//                    if (checkEmail(email))
//                        mySelf.setEmail(email);
//                    break;
//                case MySP.KEYS.AGE:
//                    mySelf.setAge(Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(MySP.KEYS.AGE, "0"))));
//                    break;
//                case MySP.KEYS.ROLL:
//                    mySelf.setRoll(sharedPreferences.getString(MySP.KEYS.ROLL, ""));
//                    break;
//                case MySP.KEYS.EXPERIENCE:
//                    mySelf.setExperience(sharedPreferences.getString(MySP.KEYS.EXPERIENCE, ""));
//                    break;
//            }
//            //save
//            updateUserChangesInList();
//        }
//
//        private void updateUserChangesInList() {
//            reference = MyFireBase.getInstance().getReference(MyFireBase.KEYS.USERS_LIST);
//            reference.child(mySelf.getUserName()).setValue(mySelf);
//            MySP.getInstance().putObject(MySP.KEYS.CURRENT_USER, mySelf);
//        }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//            mySelf = MySP.getInstance().getObject(MySP.KEYS.CURRENT_USER, User.class);
//            MySignal.getInstance().showToast(mySelf.getName());
//        }
//
//        @Override
//        public void onResume() {
//            super.onResume();
//            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
//        }
//
//        @Override
//        public void onPause() {
//            super.onPause();
//            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
//        }
//
//        private boolean isValidEmail(String email) {
//
//            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
//                    "[a-zA-Z0-9_+&*-]+)*@" +
//                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
//                    "A-Z]{2,7}$";
//
//            Pattern pat = Pattern.compile(emailRegex);
//            if (email == null)
//                return false;
//            return pat.matcher(email).matches();
//
//        }
//
//        private boolean checkEmail(String email) {
//            if (!(isValidEmail(email))) {
//                MySignal.getInstance().showToast("Not a valid e-mail\nTry Again");
//                return false;
//            }
//            return true;
//        }
//    }
//}
//
////todo change to roll selection