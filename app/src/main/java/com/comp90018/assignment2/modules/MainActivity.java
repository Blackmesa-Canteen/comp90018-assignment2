package com.comp90018.assignment2.modules;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.comp90018.assignment2.App;
import com.comp90018.assignment2.R;
import com.comp90018.assignment2.modules.categories.fragment.CategoriesFragment;
import com.comp90018.assignment2.modules.publish.activity.PublishProductActivity;
import com.comp90018.assignment2.modules.home.fragment.HomeFragment;
import com.comp90018.assignment2.modules.messages.fragment.MessagesFragment;
import com.comp90018.assignment2.modules.users.authentication.activity.LoginActivity;
import com.comp90018.assignment2.modules.users.me.fragment.MeFragment;
import com.comp90018.assignment2.base.BaseFragment;
import com.comp90018.assignment2.databinding.ActivityMainBinding;
import com.comp90018.assignment2.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import me.leefeng.promptlibrary.PromptDialog;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;

/**
 * main facade activity of the app
 *
 * @author xiaotian
 */
public class MainActivity extends AppCompatActivity {

    /**
     * view binding to replace butter knife, see android documents
     */
    private ActivityMainBinding binding;

    /**
     * holds all fragments: home, categories, messages and me
     */
    private ArrayList<BaseFragment> fragments;

    /**
     * used for picking fragments
     */
    private int position;

    /**
     * the Fragment that is shown before
     */
    private Fragment prevFragment;
    /**
     * the button that is selected before, default is home button
     */
    private int prevButtonId = R.id.button_main_home;

    /**
     * firebase authenticator
     */
    private FirebaseAuth firebaseAuth;

    private RadioButton messageBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        messageBtn = binding.buttonMainMessages;

        firebaseAuth = FirebaseAuth.getInstance();

        // Subscribe to receive messages from IM
        JMessageClient.registerEventReceiver(this);

        if (firebaseAuth.getCurrentUser() != null && JMessageClient.getMyInfo() != null) {
            Toast.makeText(this, "Welcome back, " + firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        }

        // attach to layout file
        setContentView(view);

        // load fragments
        loadFragments();

        // set up bottom buttons' event listeners
        setClickListener();
    }

    /**
     * put fragments in activity
     */
    private void loadFragments() {
        // used to store all fragments
        fragments = new ArrayList<>();

        fragments.add(new HomeFragment());
        fragments.add(new CategoriesFragment());
        fragments.add(new MessagesFragment());
        fragments.add(new MeFragment());
    }

    /**
     * set up click events for changing fragment
     */
    private void setClickListener() {

        // behavior when select different buttons
        binding.radioGroupMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            // Reminder: position's sequence matches the fragments arrayList
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.button_main_home:
                        position = 0;
                        break;

                    case R.id.button_main_categories:
                        position = 1;
                        break;

                    case R.id.button_main_messages:
                        position = 2;

                        // if not logged in, prevent user go into this fragment
                        if(firebaseAuth.getCurrentUser() == null || JMessageClient.getMyInfo() == null) {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(loginIntent);

                            // prevent go into this fragment
//                            checkedId = prevButtonId;
                            binding.radioGroupMain.check(R.id.button_main_home);
                            position = 0;
                        }


                        break;
                    case R.id.button_main_me:
                        position = 3;

                        // if not logged in, prevent user go into this fragment
                        if(firebaseAuth.getCurrentUser() == null || JMessageClient.getMyInfo() == null) {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(loginIntent);

                            // prevent go into this fragment
//                            checkedId = prevButtonId;
                            binding.radioGroupMain.check(R.id.button_main_home);
                            position = 0;
                        }
                        break;

                    case R.id.button_main_publish:
                        // publish button
                        if(firebaseAuth.getCurrentUser() == null || JMessageClient.getMyInfo() == null) {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(loginIntent);

                            // prevent go into this fragment
//                            checkedId = prevButtonId;
                            binding.radioGroupMain.check(R.id.button_main_home);
                            position = 0;
                        } else {
                            Toast.makeText(MainActivity.this, "Start publish activity", Toast.LENGTH_SHORT).show();
                            Intent publishIntent = new Intent(MainActivity.this, PublishProductActivity.class);
                            startActivity(publishIntent);
                            binding.radioGroupMain.check(R.id.button_main_home);
                            position = 0;
                        }
                        break;
                    default:
                        position = 0;
                        break;
                }

                prevButtonId = checkedId;
                // got position, then change fragments
                BaseFragment newFragment = pickFragment(position);

                // change Fragment
                changeFragment(prevFragment, newFragment);
            }
        });

        // after binding event, check home first
        binding.radioGroupMain.check(R.id.button_main_home);
    }

    /**
     * pick fragment of a specific position from fragments arraylist
     */
    private BaseFragment pickFragment(int position) {
        if (fragments != null && fragments.size() > 0) {
            return fragments.get(position);
        }

        return null;
    }

    /**
     * switch fragment
     *
     * @param fromFragment original fragment
     * @param newFragment  new picked fragment
     */
    private void changeFragment(Fragment fromFragment, BaseFragment newFragment) {

        // make sure picked different fragment
        if (prevFragment != newFragment) {
            // refresh the previous one
            prevFragment = newFragment;

            if (newFragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                /*
                 * check whether the new fragment is currently added to its activity or not.
                 */
                if (!newFragment.isAdded()) {
                    if (fromFragment != null) {
                        transaction.hide(fromFragment);
                    }
                    transaction.add(R.id.frameLayout_main, newFragment).commit();

                } else {
                    // if the new Fragment has been added to the transaction, just show it!
                    if (fromFragment != null) {
                        transaction.hide(fromFragment);
                    }

                    transaction.show(newFragment).commit();
                }
            }
        }
    }

    /**
     * switch back to home fragment
     */
    public void goHomeFragment() {
        binding.radioGroupMain.check(R.id.button_main_home);

        prevButtonId = R.id.button_main_home;
        // home position
        position = 0;
        // got position, then change fragments
        BaseFragment newFragment = pickFragment(position);

        // change Fragment
        changeFragment(prevFragment, newFragment);
    }

    public void switchRedSpotOnMessageBtn(boolean b) {

        Drawable newMessageDrawable = ContextCompat.getDrawable(this, R.drawable.button_messages_has_new_selector);
        Drawable originalMessageDrawable = ContextCompat.getDrawable(this, R.drawable.button_messages_selector);
        if (b) {
            messageBtn.setCompoundDrawablesWithIntrinsicBounds(null, newMessageDrawable, null, null);
            Log.d("MainActivity[dev]", "show red pod on message btn.");

        } else {
            messageBtn.setCompoundDrawablesWithIntrinsicBounds(null, originalMessageDrawable, null, null);
            Log.d("MainActivity[dev]", "NOT show red pod on message btn.");
        }
    }

    public void onEventMainThread(LoginStateChangeEvent event) {
        LoginStateChangeEvent.Reason reason = event.getReason();
        if (reason == LoginStateChangeEvent.Reason.user_logout) {
            Toast.makeText(this, "Login expired, please login again.", Toast.LENGTH_SHORT).show();
            if (!isFinishing()) {
                JMessageClient.logout();
                firebaseAuth.signOut();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // if main activity is destroyed, unregister event listener
        JMessageClient.unRegisterEventReceiver(this);
    }

    public ActivityMainBinding getBinding() {
        return binding;
    }
}