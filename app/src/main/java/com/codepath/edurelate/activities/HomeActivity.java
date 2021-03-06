package com.codepath.edurelate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.edurelate.BaseActivity;
import com.codepath.edurelate.MainActivity;
import com.codepath.edurelate.R;
import com.codepath.edurelate.adapters.GroupsAdapter;
import com.codepath.edurelate.databinding.ActivityHomeBinding;
import com.codepath.edurelate.databinding.TitleActivityBinding;
import com.codepath.edurelate.databinding.ToolbarMainBinding;
import com.codepath.edurelate.models.Group;
import com.codepath.edurelate.models.Member;
import com.codepath.edurelate.models.Notification;
import com.codepath.edurelate.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends BaseActivity {

    public static final String TAG = "HomeActivity";
    public static final int SPAN_COUNT = 2;

    ActivityHomeBinding binding;
    ToolbarMainBinding tbMainBinding;
    BottomNavigationView bottomNavigation;
    List<Group> groups;
    GroupsAdapter groupsAdapter;
    GridLayoutManager glManager;
    Notification latestNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setupToolbar(getString(R.string.home_title));

        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.action_home);

        groups = new ArrayList<>();
        groupsAdapter = new GroupsAdapter(HomeActivity.this,groups,null);
        setAdapterInterface();
        glManager = new GridLayoutManager(HomeActivity.this,SPAN_COUNT,
                GridLayoutManager.VERTICAL,false);
        binding.rvGroups.setAdapter(groupsAdapter);
        binding.rvGroups.setLayoutManager(glManager);
        initializeViews();
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        groupsAdapter.notifyDataSetChanged();
        initializeViews();
        queryExtraGroups();
        queryNotification();
    }

    @Override
    public void onBackPressed() {

    }

    private void initializeViews() {
        ParseFile image = ParseUser.getCurrentUser().getParseFile(User.KEY_USER_PIC);
        Log.i(TAG, ParseUser.getCurrentUser().getUsername());
        Log.i(TAG,"curr user image: " + image);
        if (image != null) {
            Log.i(TAG,"about to glide curr user pic");
            Glide.with(this).load(image.getUrl()).into(binding.ivUserPic);
        }
        binding.tvName.setText(User.getFullName(ParseUser.getCurrentUser()));
        binding.tvUsername.setText("@" + ParseUser.getCurrentUser().getUsername());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    private void setClickListeners() {
        Log.i(TAG,"click listeners to be set");
        setBottomNavigationListener(bottomNavigation, HomeActivity.this);
        setUserClickListeners();
        binding.tvNotifText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNotificationsActivity(HomeActivity.this);
            }
        });
        binding.ivNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this,NoteEditorActivity.class);
                startActivity(i);
            }
        });
    }

    public static void setBottomNavigationListener(BottomNavigationView bottomNavigation, Activity activity) {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                Log.i(TAG,"bottom navigation item selected");
                // TODO: correct if-else structure to remove else if possible
                if (item.getItemId() == R.id.action_chats) {
                    if (!(activity instanceof AllChatsActivity)) {
                        navAllChatsActivity(activity);
                    }
                }
                else if (item.getItemId() == R.id.action_home) {
                    if (!(activity instanceof HomeActivity)) {
                        navHomeActivity(activity);
                    }
                }
                else if (item.getItemId() == R.id.action_profile) {
                    if (!(activity instanceof ProfileActivity)) {
                        navProfileActivity(activity);
                    }
                }
                return true;
            }
        });
    }

    private void setUserClickListeners() {
        binding.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navProfileActivity(HomeActivity.this);
            }
        });
        binding.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navProfileActivity(HomeActivity.this);
            }
        });
        binding.cvUserPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navProfileActivity(HomeActivity.this);
            }
        });
    }

    private void updateLatestNotif() {
        if (latestNotif != null) {
            binding.tvNotifText.setText(latestNotif.getNotifText());
        }
    }

    /* ------------------------- PARSE QUERIES --------------------------- */
    private void queryExtraGroups() {
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        query.include(Member.KEY_GROUP+"."+Group.KEY_OWNER);
        query.include(Member.KEY_GROUP+"."+Group.KEY_CATEGORIES);
        query.whereEqualTo(Member.KEY_IS_FRIEND_GROUP,false);
        query.whereContainedIn(Member.KEY_USER, Arrays.asList(ParseUser.getCurrentUser()));
        query.whereNotContainedIn(Member.KEY_GROUP,User.currUserGroups);
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while querying for members: " + e.getMessage(),e);
                    return;
                }
                Log.i(TAG,"Members queried successfully. Size: " + objects.size());
                User.currUserMemberships.addAll(objects);
                List<Group> extraGroups = Member.getGroups(objects);
                User.updateCurrUserGroups(extraGroups);
                groups.clear();
                if (User.currUserGroups.size() >= 4) {
                    groups.addAll(User.currUserGroups.subList(0,4));
                    groupsAdapter.notifyDataSetChanged();
                    return;
                }
                int size = User.currUserGroups.size();
                groups.addAll(User.currUserGroups.subList(0,size));
                groupsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void queryNotification() {
        ParseQuery<Notification> query = ParseQuery.getQuery(Notification.class);
        query.whereEqualTo(Notification.KEY_USER,ParseUser.getCurrentUser());
        query.orderByDescending(Notification.KEY_CREATED_AT);
        query.getFirstInBackground(new GetCallback<Notification>() {
            @Override
            public void done(Notification object, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while getting notification: " + e.getMessage(),e);
                    return;
                }
                latestNotif = object;
                updateLatestNotif();
            }
        });
    }

    /* ------------------------- interface methods --------------------------- */
    private void setAdapterInterface() {
        groupsAdapter.setAdapterListener(new GroupsAdapter.GroupsAdapterInterface() {
            @Override
            public void groupClicked(Group group,View groupPic) {
                goGroupDetailsActivity(group,groupPic);
            }

            @Override
            public void ownerClicked(ParseUser owner) {
                goProfileActivity(owner);
            }

            @Override
            public void joinGroup(Group group) {
            }
        });
    }

    /* --------------------- intent navigation methods ------------------------ */
    private void goProfileActivity(ParseUser owner) {
        Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
        i.putExtra(User.KEY_USER,Parcels.wrap(owner));
        this.startActivity(i);
    }

    private void goGroupDetailsActivity(Group group,View groupPic) {
        Intent i = new Intent(HomeActivity.this, GroupDetailsActivity.class);
        i.putExtra(Group.KEY_GROUP,Parcels.wrap(group));
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(HomeActivity.this, groupPic, "groupPic");
        this.startActivity(i,options.toBundle());
    }

    /* ------------------ static activity intent methods ----------------------- */
    public static void navAllChatsActivity(Activity activity) {
        Intent i = new Intent(activity, AllChatsActivity.class);
        activity.startActivity(i);
    }

    public static void navHomeActivity(Activity activity) {
        Intent i = new Intent(activity, HomeActivity.class);
        activity.startActivity(i);
    }

    public static void navProfileActivity(Activity activity) {
        Intent i = new Intent(activity, ProfileActivity.class);
        i.putExtra(User.KEY_USER, Parcels.wrap(ParseUser.getCurrentUser()));
        activity.startActivity(i);
    }

    public static void goAllGroupsActivity(Activity activity) {
        Intent i = new Intent(activity, AllGroupsActivity.class);
        activity.startActivity(i);
    }

    public static void goAllUsersActivity(Activity activity) {
        Log.i(TAG,"Go to all users activity from " + activity.getLocalClassName());
        Intent i = new Intent(activity, AllUsersActivity.class);
        activity.startActivity(i);
    }

    public static void goNotificationsActivity(Activity activity) {
        Intent i = new Intent(activity, NotificationsActivity.class);
        activity.startActivity(i);
    }
}