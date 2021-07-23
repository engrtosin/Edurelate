package com.codepath.edurelate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.edurelate.BaseActivity;
import com.codepath.edurelate.R;
import com.codepath.edurelate.adapters.ChatsAdapter;
import com.codepath.edurelate.adapters.UsersAdapter;
import com.codepath.edurelate.databinding.ActivityAllChatsBinding;
import com.codepath.edurelate.databinding.ActivityAllMembersBinding;
import com.codepath.edurelate.databinding.ToolbarMainBinding;
import com.codepath.edurelate.models.Chat;
import com.codepath.edurelate.models.Group;
import com.codepath.edurelate.models.Invite;
import com.codepath.edurelate.models.Member;
import com.codepath.edurelate.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class AllChatsActivity extends BaseActivity {

    public static final String TAG = "AllChatsActivity";
    public static final int SPAN_COUNT = 1;

    ActivityAllChatsBinding binding;
    ToolbarMainBinding tbMainBinding;
    BottomNavigationView bottomNavigation;
    List<Group> groups = new ArrayList<>();
    ChatsAdapter chatsAdapter;
    GridLayoutManager glManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"in on create");
        binding = ActivityAllChatsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        // TODO: Use string.xml value
        setupToolbar("Chats");
        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);

        groups = new ArrayList<>();
        Log.i(TAG,"Number of all groups: " + groups.size());
        chatsAdapter = new ChatsAdapter(AllChatsActivity.this,groups);
        setAdapterInterface();
        glManager = new GridLayoutManager(AllChatsActivity.this,SPAN_COUNT,
                GridLayoutManager.VERTICAL,false);
        binding.rvChats.setAdapter(chatsAdapter);
        binding.rvChats.setLayoutManager(glManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        binding.rvChats.addItemDecoration(itemDecoration);
        queryAllGroups();

        setClickListeners();
    }

    private void queryAllGroups() {
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        query.include(Member.KEY_GROUP+"."+Group.KEY_LATEST_MSG);
        query.whereEqualTo(Member.KEY_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while querying for members: " + e.getMessage(),e);
                    return;
                }
                Log.i(TAG,"Members queried successfully. Size: " + objects.size());
                chatsAdapter.addAll(Member.getGroups(objects));
            }
        });
    }

    private void setClickListeners() {
        Log.i(TAG,"click listeners to be set");

//        setToolbarClickListeners();
        HomeActivity.setBottomNavigationListener(bottomNavigation,AllChatsActivity.this);
    }

    private void setToolbarClickListeners() {
        tbMainBinding.ivBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tbMainBinding.ivLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"logout clicked");
                LoginActivity.logoutUser(AllChatsActivity.this);
            }
        });
    }

    /* --------------------- ADAPTER INTERFACE METHODS --------------------- */
    private void setAdapterInterface() {
        chatsAdapter.setAdapterListener(new ChatsAdapter.ChatsAdapterInterface() {
            @Override
            public void groupClicked(Group group) {
                goGroupDetailsActivity(group);
            }

            @Override
            public void chatClicked(Group group) {
                goChatActivity(group);
            }
        });
    }

    /* --------------------- INTENT METHODS TO ACTIVITIES --------------------- */
    private void goGroupDetailsActivity(Group group) {
        Intent i = new Intent(AllChatsActivity.this,GroupDetailsActivity.class);
        i.putExtra(Group.KEY_GROUP, Parcels.wrap(group));
        this.startActivity(i);
    }

    private void goChatActivity(Group group) {
        Log.i(TAG,"go group : " + group.getObjectId());
        Intent i = new Intent(AllChatsActivity.this,ChatActivity.class);
        i.putExtra(Group.KEY_GROUP, Parcels.wrap(group));
        this.startActivity(i);
    }

}