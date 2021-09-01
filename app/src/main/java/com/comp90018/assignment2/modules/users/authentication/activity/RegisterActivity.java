package com.comp90018.assignment2.modules.users.authentication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.comp90018.assignment2.R;
import com.comp90018.assignment2.databinding.ActivityLoginBinding;
import com.comp90018.assignment2.databinding.ActivityRegisterBinding;
import com.comp90018.assignment2.dto.UserDTO;
import com.comp90018.assignment2.utils.ClearWriteEditText;
import com.comp90018.assignment2.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.options.RegisterOptionalUserInfo;
import cn.jpush.im.api.BasicCallback;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity DEBUG";
    private ActivityRegisterBinding binding;

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // init view binding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        // attach to layout file
        setContentView(view);

        // init firebase service
        firebaseAuth = FirebaseAuth.getInstance();
        // init db
        db = FirebaseFirestore.getInstance();

        // setup register listeners
        // back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RadioGroup genderGroup = binding.genderRadioGroup;
        final Integer[] gender = {Constants.GENDER_UNKNOWN};

        // get gender info
        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == binding.radioGenderFemale.getId()) {
                    gender[0] = Constants.FEMALE;
                } else if (checkedId == binding.radioGenderMale.getId()) {
                    gender[0] = Constants.MALE;
                } else {
                    gender[0] = Constants.GENDER_UNKNOWN;
                }
            }
        });

        genderGroup.check(binding.radioGenderFemale.getId());

        // go to login
        binding.textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toLoginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(toLoginActivity);
            }
        });

        // submit
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameStr = binding.registerEmail.getText().toString();
                String loginPassword = binding.registerPassWord.getText().toString();
                String repeatPassword = binding.registerRepeatPassword.getText().toString();

                String nickName = binding.registerNickname.getText().toString();

                // get genderType
                Integer genderType = gender[0];

                // check size input
                // size of input will not greater than 80 characters
                if (usernameStr == null || loginPassword == null || repeatPassword == null || nickName == null
                        || usernameStr.length() == 0 || loginPassword.length() == 0
                        || usernameStr.length() >= 80 || loginPassword.length() >= 80
                        || repeatPassword.length() >= 80 || repeatPassword.length() == 0
                        || nickName.length() >= 20) {
                    new AlertDialog.Builder(RegisterActivity.this).setMessage("Please input all information correctly.").setPositiveButton("ok", null).show();
                    return;
                }

                // validate inputs
                // check legal inputs
                // email regex
                String emailRegex = "^[a-z\\d]+(\\.[a-z\\d]+)*@([\\da-z](-[\\da-z])?)+(\\.{1,2}[a-z]+)+$";
                // No password regex, because it is login
                if (!usernameStr.matches(emailRegex)) {
                    new AlertDialog.Builder(RegisterActivity.this).setMessage("username should be email.").setPositiveButton("ok", null).show();
                    return;
                }

                //check nickname
                String nicknameRegex = "^[a-z0-9_-]{0,20}$";
                // No password regex, because it is login
                if (!nickName.matches(nicknameRegex)) {
                    new AlertDialog.Builder(RegisterActivity.this).setMessage("Nickname should be letters, underscores and dashes.").setPositiveButton("ok", null).show();
                    return;
                }

                // check password strong
                String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\s\\S]{6,}$";
                if (!loginPassword.matches(passwordRegex)) {
                    new AlertDialog.Builder(RegisterActivity.this).setMessage("Password contains at least 6 characters, at least 1 letter and 1 digit.").setPositiveButton("ok", null).show();
                    binding.registerPassWord.setText("");
                    binding.registerRepeatPassword.setText("");
                    return;
                }

                // check repeat equality
                if (!repeatPassword.equals(loginPassword)) {
                    new AlertDialog.Builder(RegisterActivity.this).setMessage("Repeat password not matched.").setPositiveButton("ok", null).show();
                    binding.registerPassWord.setText("");
                    binding.registerRepeatPassword.setText("");
                    return;
                }

                // register user on Firebase
                firebaseAuth.createUserWithEmailAndPassword(usernameStr, loginPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = firebaseAuth.getCurrentUser();

                                    String userId = user.getUid();

                                    // JMeassage 注册
                                    RegisterOptionalUserInfo jmessageOptionalInfo = new RegisterOptionalUserInfo();
                                    // Jmessage extra info
                                    jmessageOptionalInfo.setAvatar(Constants.DEFAULT_AVATAR_PATH);
                                    jmessageOptionalInfo.setNickname(nickName);
                                    JMessageClient.register(userId, loginPassword, jmessageOptionalInfo, new BasicCallback() {
                                        @Override
                                        public void gotResult(int i, String s) {
                                            // 0 表示正常。大于 0 表示异常，responseMessage 会有进一步的异常信息。
                                            if (i == 0) {
                                                Log.d(TAG, "Jmessage register:success");
                                            } else {
                                                Log.w(TAG, "Jmessage register:failure:" + s);
                                            }
                                        }
                                    });

                                    // store userDTO in the database
                                    UserDTO newUserDto = new UserDTO();
                                    newUserDto.setEmail(usernameStr);
                                    newUserDto.setAvatar_address(Constants.DEFAULT_AVATAR_PATH);
                                    newUserDto.setCreated_time(Timestamp.now());
                                    newUserDto.setDescription("");
                                    newUserDto.setGender(genderType);
                                    newUserDto.setPayment_info("");
                                    newUserDto.setSold_number(0);
                                    newUserDto.setStar_number(4.0);
                                    newUserDto.setLocation_text("");
                                    newUserDto.setFavorite_refs(new ArrayList<>());
                                    newUserDto.setFollower_refs(new ArrayList<>());
                                    newUserDto.setFollowing_refs(new ArrayList<>());
                                    // database don't need not store password info
                                    newUserDto.setPassword("Deprecated!");

                                    // write db
                                    db.collection(Constants.USERS_COLLECTION)
                                            .document(userId).set(newUserDto)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "createUserRecordInDB:success");
                                                        Toast.makeText(RegisterActivity.this, "Hello! "+ nickName, Toast.LENGTH_SHORT).show();

                                                        // finish the register activity
                                                        finish();
                                                    } else {
                                                        Log.w(TAG, "createUserWithEmail:failed", task.getException());
                                                        Toast.makeText(RegisterActivity.this, "Authentication failed. Try again please.", Toast.LENGTH_SHORT).show();
                                                        // TODO: Rollback current modifications
                                                    }
                                                }
                                            });

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "Authentication failed. Try again please.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // check if logged in, if so, go to me fragment
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // 已经登陆了，退出activity.
            Log.d(TAG, "signUpWithEmail:Already login");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check if logged in, if so, go to me fragment
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // 已经登陆了，退出activity.
            Log.d(TAG, "signUpWithEmail:Already login");
            finish();
        }
    }
}