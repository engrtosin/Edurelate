package com.codepath.edurelate.models;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class User {

    public static final String TAG = "UserModel";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_USER_PIC = "userPic";
    public static final String KEY_USER = "user";
    public static final String KEY_OBJECT_ID = "objectId";
    public static final String BOT_OBJECT_ID = "kJmCehSRZm";
    public static final String KEY_EMAIL = "email";
    public static final String GOOGLE_PASSWORD = "googlePassword";
    public static final String KEY_INTERESTS = "interests";

    public static ParseUser currentUser;
    public static GoogleSignInAccount googleAccount;
    public static ParseUser edurelateBot;
    public static List<Group> currUserGroups = new ArrayList<>();
    public static HashMap<Integer,Integer> groupStatsMap = new HashMap<>();
    public static List<Member> currUserMemberships = new ArrayList<>();
    public static int categorySum = 0;
    public static double meanGroupSize = 0;
    public static double groupSizeStdDev = 0;

    /* ------------------- GET METHODS -------------------------- */
    public static String getFirstName(ParseUser user) {
        String firstName = user.getString(KEY_FIRST_NAME);
        return firstName;
    }

    public static String getLastName(ParseUser user) {
        String lastName = user.getString(KEY_LAST_NAME);
        return lastName;
    }

    public static String getFullName(ParseUser user) {
        String firstName = null;
        String lastName = null;
        firstName = user.getString(KEY_FIRST_NAME);
        lastName = user.getString(KEY_LAST_NAME);
        return firstName + " " + lastName;
    }

    /* ------------------- SET METHODS -------------------------- */
    public static void setFirstName(ParseUser user, String firstName) {
        user.put(KEY_FIRST_NAME,firstName);
    }

    public static void setLastName(ParseUser user, String lastName) {
        user.put(KEY_LAST_NAME,lastName);
    }

    public static void setUserPic(ParseUser user, ParseFile parseFile) {
        user.put(KEY_USER_PIC,parseFile);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error updating user " + user.getObjectId() + " pic");
                    return;
                }
            }
        });
    }

    /* ------------------- CURRENT USER METHODS -------------------------- */
    public static void clearCurrUserData() {
        currUserGroups = null;
        currUserMemberships = null;
        groupStatsMap = new HashMap<>();
        categorySum = 0;
    }

    public static List<String> getCurrGroupIds() {
        List<String> groupIds = new ArrayList<>();
        for (int i = 0; i < currUserGroups.size(); i++) {
            groupIds.add(currUserGroups.get(i).getObjectId());
        }
        return groupIds;
    }

    public static void sendGroupRequest(Group group) {
        ParseUser currUser = ParseUser.getCurrentUser();
        Request request = Request.newRequest(currUser,group);
        String txtToOwner = User.getFullName(currUser) + " wants to join your group: " + group.getGroupName();
        Notification toOwner = Notification.newNotification(group.getOwner(),Notification.REQUEST_RECEIVED_CODE,txtToOwner,request);
        String txtToUser = "You sent a request to join " + group.getGroupName();
        Notification toUser = Notification.newNotification(currUser,Notification.REQUEST_SENT_CODE,txtToUser,request);
    }

    public static void leaveGroup(Group group) {
        ParseUser currUser = ParseUser.getCurrentUser();
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        query.whereEqualTo(Member.KEY_USER,currUser);
        query.whereContainedIn(Member.KEY_GROUP, Arrays.asList(group));
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while getting corresponding member object: " + e.getMessage(),e);
                    return;
                }
                if (objects.size() > 0) {
                    Log.i(TAG,"Corresponding member for curr user and group (" + group.getObjectId() + ") found.");
                    objects.get(0).deleteInBackground();
                    deleteGroupForCurrUser(group);
                }
            }
        });
        String txtToOwner = User.getFullName(currUser) + " left your group: " + group.getGroupName();
        Notification toOwner = Notification.newNotification(group.getOwner(),Notification.MEMBER_LEFT_CODE,txtToOwner);
        String txtToUser = "You left " + group.getGroupName();
        Notification toUser = Notification.newNotification(currUser,Notification.YOU_LEFT_GROUP_CODE,txtToUser);
        group.memberLeft();
    }

    private static void deleteGroupForCurrUser(Group group) {
        if (currUserGroups.contains(group)) {
            currUserGroups.remove(group);
            return;
        }
        for (int i = 0; i < currUserGroups.size(); i++) {
            Log.i(TAG,"is group same as this in currUserGroups: " + i + ", " + currUserGroups.get(i).getObjectId().equals(group.getObjectId()));
            if (currUserGroups.get(i).getObjectId().equals(group.getObjectId())) {
                currUserGroups.remove(currUserGroups.get(i));
                return;
            }
        }
    }

    private static void deleteMembershipForCurrUser(Member member) {
        if (currUserMemberships.contains(member)) {
            currUserMemberships.remove(member);
            return;
        }
        for (int i = 0; i < currUserMemberships.size(); i++) {
            Log.i(TAG,"is group same as this in currUserMemberships: " + i + ", " + currUserMemberships.get(i).getObjectId().equals(member.getObjectId()));
            if (currUserMemberships.get(i).getObjectId().equals(member.getObjectId())) {
                currUserMemberships.remove(currUserMemberships.get(i));
                return;
            }
        }
    }

    /* ------------------- OTHER METHODS -------------------------- */
    public static boolean compareUsers(ParseUser user, ParseUser otherUser) {
        return user.getObjectId().equals(otherUser.getObjectId());
    }

    public static ParseFile getUserPic(ParseUser friend) {
        return friend.getParseFile(KEY_USER_PIC);
    }

    public static List<Integer> getInterests(ParseUser user) {
        List<Integer> interests = new ArrayList<>();
        try {
            interests = user.fetchIfNeeded().getList(KEY_INTERESTS);
        } catch (ParseException e) {
            Log.e(TAG,"Error while getting interests: " + e.getMessage(),e);
        }
        return interests;
    }

    public static void addNewInterests(ParseUser user, List<Integer> newInterests) {
        user.addAll(KEY_INTERESTS,newInterests);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while saving interests: " + e.getMessage(),e);
                    return;
                }
            }
        });
    }

    public static void updateCurrUserGroups(List<Group> extraGroups) {
        int oldGroupLength = currUserGroups.size();
        int newGroupLength = extraGroups.size();
        double newTotalSize = 0;
        for (int i = 0; i < extraGroups.size(); i++) {
            Group group = extraGroups.get(i);
            List<Category> categories = extraGroups.get(i).getCategories();
            for (int j = 0; j < categories.size(); j++) {
                int code = categories.get(j).getCode();
                categorySum += 1;
                newTotalSize += group.getSize();
                Log.i(TAG,"Group with size: " + group.getSize());
                if (!groupStatsMap.containsKey(code)) {
                    groupStatsMap.put(code,1);
                    continue;
                }
                int currMapSize = groupStatsMap.get(code);
                groupStatsMap.put(code,currMapSize+1);
            }
        }
        meanGroupSize = (meanGroupSize*oldGroupLength + newTotalSize)/(oldGroupLength+newGroupLength);
        currUserGroups.addAll(extraGroups);
        Runnable r = new Runnable() {
            public void run() {
                updateStdDev();
                Log.i(TAG,"Mean: " + meanGroupSize);
                Log.i(TAG,"Standard dev: " + groupSizeStdDev);
            }
        };
        Thread t = new Thread(r);
        t.start();
        Log.i(TAG,"Map size: " + groupStatsMap.size());
        Log.i(TAG,"Category sum: " + categorySum);
    }

    private static void updateStdDev() {
        double variance = 0;
        for (int i = 0; i < currUserGroups.size(); i++) {
            variance += Math.pow(currUserGroups.get(i).getSize() - meanGroupSize,2);
        }
        variance /= currUserGroups.size();
        groupSizeStdDev = Math.pow(variance,0.5);
    }

    public static String getEmail(ParseUser user) {
        return user.getEmail();
    }
}
