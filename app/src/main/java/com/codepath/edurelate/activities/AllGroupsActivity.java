package com.codepath.edurelate.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.codepath.edurelate.BaseActivity;
import com.codepath.edurelate.R;
import com.codepath.edurelate.adapters.GroupsAdapter;
import com.codepath.edurelate.databinding.ActivityAllGroupsBinding;
import com.codepath.edurelate.databinding.ToolbarMainBinding;
import com.codepath.edurelate.fragments.NewGroupDialogFragment;
import com.codepath.edurelate.fragments.SearchGroupsFragment;
import com.codepath.edurelate.models.Category;
import com.codepath.edurelate.models.Group;
import com.codepath.edurelate.models.Request;
import com.codepath.edurelate.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllGroupsActivity extends BaseActivity {

    public static final String TAG = "AllGroupsActivity";
    public static final int SPAN_COUNT = 1;
    public static final String GROUPS_BY_NAME = "groupsByName";
    public static final String GROUPS_BY_RANK = "groupsByRank";
    public static final String REQUEST_IDS = "requestIds";

    ActivityAllGroupsBinding binding;
    ToolbarMainBinding tbMainBinding;
    BottomNavigationView bottomNavigation;
    List<Group> groups = new ArrayList<>();
    List<Group> groupsByRank = new ArrayList<>();
    List<Double> ranks = new ArrayList<>();
    List<String> requestIds;
    GroupsAdapter groupsAdapter;
    GridLayoutManager glManager;
    ArrayAdapter<String> sortAdapter;
    String[] sortOptions = new String[]{"Name (A-Z)","Name (Z-A)", "Recommended"};
    GroupsAdapter byRankAdapter;
    boolean groupsIsReversed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG,"in on create");
        binding = ActivityAllGroupsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setupToolbar("All Groups");
        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        groups = new ArrayList<>();
        requestIds = new ArrayList<>();
        queryCurrUserRequests();
        groupsAdapter = new GroupsAdapter(AllGroupsActivity.this,groups,requestIds);
        byRankAdapter = new GroupsAdapter(this,groupsByRank,requestIds);
        setAdapterInterface();
        glManager = new GridLayoutManager(AllGroupsActivity.this,SPAN_COUNT,
                GridLayoutManager.VERTICAL,false);
        binding.rvGroups.setAdapter(groupsAdapter);
        binding.rvGroups.setLayoutManager(glManager);
        queryAllGroups();
        setupSortSpinner();
        setClickListeners();
    }

    /* ------------------- SPINNERS ------------------ */
    private void setupSortSpinner() {
        sortAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,sortOptions);
        binding.spSortBy.setAdapter(sortAdapter);
        binding.spSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortItemSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void sortItemSelected(int position) {
        Log.i(TAG,"Sort item selected at " + position);
        Log.i(TAG,"Position is new");
        if (position == 0) {
            binding.rvGroups.setAdapter(groupsAdapter);
            if (groupsIsReversed) {
                Log.i(TAG,"Groups is reversed");
                Collections.reverse(groups);
                groupsIsReversed = false;
                groupsAdapter.notifyDataSetChanged();
            }
            return;
        }
        if (position == 1) {
            binding.rvGroups.setAdapter(groupsAdapter);
            if (!groupsIsReversed) {
                Log.i(TAG,"Groups is not reversed");
                Collections.reverse(groups);
                groupsIsReversed = true;
                groupsAdapter.notifyDataSetChanged();
            }
            return;
        }
        if (position == 2) {
            binding.rvGroups.setAdapter(byRankAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);
        menu.findItem(R.id.action_search).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            switchToSearch();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setClickListeners() {
        Log.i(TAG,"click listeners to be set");
        HomeActivity.setBottomNavigationListener(bottomNavigation,AllGroupsActivity.this);
        binding.tvActNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewGroupDialog();
            }
        });
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
                LoginActivity.logoutUser(AllGroupsActivity.this);
            }
        });
    }

    private void showNewGroupDialog() {
        FragmentManager fm = getSupportFragmentManager();
        NewGroupDialogFragment newGroupDialogFragment = NewGroupDialogFragment.newInstance("New Group");
        newGroupDialogFragment.show(fm, "fragment_new_group");
    }

    private void queryAllGroups() {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.include(Group.KEY_OWNER);
        query.include(Group.KEY_CATEGORIES);
        query.whereNotEqualTo(Group.KEY_OWNER,User.edurelateBot);
        query.whereNotContainedIn(Group.KEY_OBJECT_ID,User.getCurrGroupIds());
        query.orderByAscending(Group.KEY_GROUP_NAME_LOWER);
        query.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while querying groups: " + e.getMessage(),e);
                    return;
                }
                Log.i(TAG, "Groups successfully queried. Size: " + objects.size());
                groupsAdapter.clear();
                groupsAdapter.addAll(objects);
                Log.i(TAG, "Groups adapter is updated");
                rankGroups(objects);
            }
        });
    }

    private void rankGroups(List<Group> groups) {
        List<Integer> userInterests = User.getInterests(ParseUser.getCurrentUser());
        Log.i(TAG,"Mean: " + User.meanGroupSize);
        Log.i(TAG,"StdDev: " + User.groupSizeStdDev);
        Log.i(TAG,"Pdf at mean: " + pdf(User.meanGroupSize,User.meanGroupSize,User.groupSizeStdDev));
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            List<Category> categories = group.getCategories();
            double interestRank = 0;
            double currGroupsRank = 0;
            double sizeRank = 0;
            if (User.groupSizeStdDev != 0) {
                sizeRank = pdf(group.getSize(),User.meanGroupSize,User.groupSizeStdDev);
            }
            if (Double.isNaN(sizeRank)) {
                sizeRank = 0;
            }
            Log.i(TAG,"Size rank: " + sizeRank + " with group size: " + group.getSize());
            for (int j = 0; j < categories.size(); j++) {
                int code = categories.get(j).getCode();
                if (userInterests.contains(code)) {
                    interestRank += 1.0/userInterests.size();
                    Log.i(TAG,"User has interest " + categories.get(j).getTitle() + " with rank: " + interestRank);
                }
                if (User.groupStatsMap.containsKey(code)) {
                    currGroupsRank += 1.0 * User.groupStatsMap.get(code)/User.categorySum;
                    Log.i(TAG,"User's groups have category " + categories.get(j).getTitle() + " with rank: " + currGroupsRank);
                }
            }
            double rank = interestRank * 10 + currGroupsRank * 8 + sizeRank * 3;
            addGroup(group,rank);
        }
    }

    public static double pdf(double x, double mu, double sigma) {
        double z = (x - mu) / sigma;
        return Math.exp(-z*z / 2);
    }

    private void addGroup(Group group, double rank) {
        int pos = findNewPos(rank);
        Log.i(TAG,"Group with rank: " + rank + " added at pos: " + pos);
        groupsByRank.add(pos,group);
        ranks.add(pos,rank);
    }

    private int findNewPos(double rank) {
        if (ranks.size() == 0) {
            return 0;
        }
        int start = 0;
        int end = ranks.size();
        while (start < end) {
            int mid = start + (end - start) / 2;
            if (ranks.get(mid) == rank) {
                return mid + 1;
            }
            if (ranks.get(mid) < rank) {
                if (mid - 1 > 0) {
                    if (ranks.get(mid-1) >= rank) {
                        return mid;
                    }
                    end = mid - 1;
                    continue;
                }
                return 0;
            }
            if (mid+1 < ranks.size()) {
                if (ranks.get(mid+1) <= rank) {
                    return mid+1;
                }
                start = mid + 1;
                continue;
            }
            return ranks.size();
        }
        return ranks.size();
    }

    private void queryCurrUserRequests() {
        ParseQuery<Request> query = ParseQuery.getQuery(Request.class);
        query.include(Request.KEY_TO_GROUP);
        query.whereEqualTo(Request.KEY_CREATOR,ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Request>() {
            @Override
            public void done(List<Request> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while querying request: " + e.getMessage(),e);
                    return;
                }
                Log.i(TAG,"Request successfully queried");
                requestIds.addAll(Request.getGroupIds(objects));
            }
        });
    }

    /* -------------------------- SEARCH -------------------------------- */
    private void switchToSearch() {
        binding.rlWholePage.setVisibility(View.GONE);
        binding.flContainer.setVisibility(View.VISIBLE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SearchGroupsFragment fragment = SearchGroupsFragment.newInstance(groups,groupsByRank,requestIds);
        fragment.setFragListener(new SearchGroupsFragment.SearchFragInterface() {
            @Override
            public void fragmentClosed() {
                binding.rlWholePage.setVisibility(View.VISIBLE);
                binding.flContainer.setVisibility(View.GONE);
            }

            @Override
            public void joinGroup(Group group) {

            }

            @Override
            public void goToGroup(Group group) {
                goGroupDetailsActivity(group);
            }
        });
        ft.replace(R.id.flContainer, fragment);
        ft.commit();
    }

    /* ------------------- interface methods ------------------ */
    private void setAdapterInterface() {
        groupsAdapter.setAdapterListener(new GroupsAdapter.GroupsAdapterInterface() {
            @Override
            public void groupClicked(Group group,View groupPic) {
                goGroupDetailsActivity(group);
            }

            @Override
            public void ownerClicked(ParseUser owner) {
                goProfileActivity(owner);
            }

            @Override
            public void joinGroup(Group group) {
                Log.i(TAG,"Send a request to join group: " + group);
                User.sendGroupRequest(group);
            }
        });
        byRankAdapter.setAdapterListener(new GroupsAdapter.GroupsAdapterInterface() {
            @Override
            public void groupClicked(Group group,View groupPic) {
                goGroupDetailsActivity(group);
            }

            @Override
            public void ownerClicked(ParseUser owner) {
                goProfileActivity(owner);
            }

            @Override
            public void joinGroup(Group group) {
                Log.i(TAG,"Send a request to join group: " + group);
                User.sendGroupRequest(group);
            }
        });
    }

    /* --------------------- intent methods to activities --------------------- */
    private void goProfileActivity(ParseUser user) {
        Intent i = new Intent(AllGroupsActivity.this, ProfileActivity.class);
        i.putExtra(User.KEY_USER, Parcels.wrap(user));
        this.startActivity(i);
    }

    private void goGroupDetailsActivity(Group group) {
        Intent i = new Intent(AllGroupsActivity.this, GroupDetailsActivity.class);
        i.putExtra(Group.KEY_GROUP,Parcels.wrap(group));
        this.startActivity(i);
    }
}