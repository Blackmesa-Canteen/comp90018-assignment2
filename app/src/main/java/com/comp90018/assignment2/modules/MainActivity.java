package com.comp90018.assignment2.modules;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.comp90018.assignment2.R;
import com.comp90018.assignment2.modules.categories.fragment.CategoriesFragment;
import com.comp90018.assignment2.modules.home.fragment.HomeFragment;
import com.comp90018.assignment2.modules.messages.fragment.MessagesFragment;
import com.comp90018.assignment2.modules.users.authentication.activity.LoginActivity;
import com.comp90018.assignment2.modules.users.me.fragment.MeFragment;
import com.comp90018.assignment2.base.BaseFragment;
import com.comp90018.assignment2.databinding.ActivityMainBinding;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
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
                        break;
                    case R.id.button_main_me:
                        position = 3;
                        break;

                    case R.id.button_main_publish:
                        // publish button

                        /* TODO： start publish activity */
                        // debug
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(loginIntent);

                        // check the original one
                        binding.radioGroupMain.check(prevButtonId);
                        return;
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
}