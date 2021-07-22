package com.codepath.edurelate.models;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Parcel(analyze = Group.class)
@ParseClassName("Group")
public class Group extends ParseObject {

    public static final String TAG = "GroupModel";
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_IS_FRIEND_GROUP = "isFriendGroup";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_CHAT = "chat";
    public static final String KEY_GROUP = "group";
    public static final String KEY_MEMBERS = "members";
    public static final String KEY_GROUP_PIC = "groupPic";
    public static final String KEY_MESSAGES = "messages";
    public static final String KEY_LATEST_MSG = "latestMsg";
    public static final String KEY_LATEST_MSG_DATE = "latestMsgDate";
    public static final String KEY_INVITEES = "invitees";
    public static final String KEY_JOIN_REQUESTERS = "joinRequesters";
    public static final int OPEN_GROUP_CODE = 0;
    public static final int CLOSED_GROUP_CODE = 1;
    public static final String KEY_GROUP_ACCESS = "groupAccess";

    public static Group newNonFriendGroup(String groupName,int groupAccess) throws ParseException {
        Group group = new Group();
        group.setGroupName(groupName);
        group.setNewMembers();
        group.put(KEY_MESSAGES,new ArrayList<>());
        group.put(KEY_GROUP_ACCESS,groupAccess);
        group.setOwner(ParseUser.getCurrentUser());
        group.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error saving group: " + e.getMessage(), e);
                    return;
                }
                group.setLatestMsgDate(group.getCreatedAt());
                Log.i(TAG,"Group successfully created!");
            }
        });
        User.addNewGroup(group,ParseUser.getCurrentUser());
        return group;
    }

    public String getGroupName() {
        if (has(Group.KEY_GROUP_NAME)) {
            return getString(KEY_GROUP_NAME);
        }
        return "no name";
    }

    public boolean getIsFriendGroup() {
        return getOwner().getObjectId().equals(User.edurelateBot.getObjectId());
    }

    public ParseUser getOwner() {
        return getParseUser(KEY_OWNER);
    }

    public List<Message> getMessages() {
        return getList(KEY_MESSAGES);
    }

    public Chat getChat() {
        return (Chat) get(KEY_CHAT);
    }

    public List<ParseUser> getMembers() {
        return getList(KEY_MEMBERS);
    }

    public ParseFile getGroupPic() throws ParseException {
        return fetchIfNeeded().getParseFile(KEY_GROUP_PIC);
    }

    public Message getLatestMsg() throws ParseException {
        return (Message) get(KEY_LATEST_MSG);
    }

    public Date getLatestMsgDate() throws ParseException {
        return getDate(KEY_LATEST_MSG_DATE);
    }

    public List<ParseUser> getInvitees() {
        return getList(KEY_INVITEES);
    }

    public void setGroupName(String groupName) {
        put(KEY_GROUP_NAME,groupName);
    }

    public void setIsFriendGroup(boolean isFriendGroup) {
        put(KEY_IS_FRIEND_GROUP,isFriendGroup);
    }

    public void setOwner(ParseUser owner) {
        put(KEY_OWNER,owner);
    }

    private void setLatestMsgDate(Date createdAt) {
        put(KEY_LATEST_MSG_DATE, createdAt);
    }

    public int getGroupAccess() {
        return getInt(KEY_GROUP_ACCESS);
    }

    public void addMember(ParseUser member) {
        addUnique(KEY_MEMBERS,member);
        // TODO: relocate this if needed
        saveInBackground();
    }

    private void addMessage(Message message) {
        add(KEY_MESSAGES,message);
    }

    public void setNewMembers() {
        List<Member> members = new ArrayList<>();
        put(KEY_MEMBERS,members);
    }

    public void setGroupPic(ParseFile groupPic) {
        put(KEY_GROUP_PIC,groupPic);
    }

//    public void setNewChat() {
//        this.chat = new Chat();
//        setChatFields();
//        chat.saveInBackground();
//        put(KEY_CHAT,this.chat);
//    }

    /* ------------- GROUP HELPER METHODS ------------------- */
    public void sendInvite(ParseUser user) {
        Invite invite = Invite.newGroupInvite(user,this);
        add(KEY_INVITEES,user);
        String txtToOwner = "You invited " + User.getFullName(user) + " to " + getGroupName();
        Notification toOwner = Notification.newInstance(Notification.INVITER_CODE,txtToOwner,invite);
        String txtToUser = User.getFullName(getOwner()) + " invited you to " + getGroupName();
        Notification toUser = Notification.newInstance(Notification.INVITEE_CODE,txtToUser,invite);
        saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG,"Error while adding user to invitees: " + e.getMessage(),e);
                }
                Log.i(TAG, "user added to invitees.");
            }
        });
    }

    public Message sendNewMessage(String body, Message replyTo) {
        Message message = new Message();
        message.put(Message.KEY_BODY,body);
        message.put(Message.KEY_RECIPIENT,this);
        message.put(Message.KEY_USERS_LIKING_THIS, new ArrayList<>());
        message.put(Message.KEY_SENDER,ParseUser.getCurrentUser());
        if (replyTo != null) {
            message.put(Message.KEY_REPLY_TO,replyTo);
        }
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error saving new message: " + e.getMessage(),e);
                    return;
                }
                Log.i(TAG,"Message successfully saved.");
            }
        });
        put(KEY_LATEST_MSG,message);
        addMessage(message);
        saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error while adding message to group: " + e.getMessage(),e);
                }
                Log.i(TAG,"message successfully added to group");
            }
        });
        return message;
    }

    public Member getMember(ParseUser user) {
        final Member[] member = new Member[1];
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        query.whereEqualTo(Member.KEY_USER,user);
        query.whereEqualTo(Member.KEY_GROUP,this);
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error finding member: " + e.getMessage(),e);
                }
                if (objects.size() > 0) {
                    member[0] = objects.get(0);
                }
            }
        });
        return member[0];
    }

    public Member removeInvitee(ParseUser user) {
        final Member[] member = new Member[1];
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        query.whereEqualTo(Member.KEY_USER,user);
        query.whereEqualTo(Member.KEY_GROUP,this);
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error finding member: " + e.getMessage(),e);
                }
                if (objects.size() > 0) {
                    member[0] = objects.get(0);
                }
            }
        });
        removeAll(KEY_INVITEES, Arrays.asList(member[0]));
        return member[0];
    }

    public Member removeJoinRequester(ParseUser user) {
        final Member[] member = new Member[1];
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        query.whereEqualTo(Member.KEY_USER,user);
        query.whereEqualTo(Member.KEY_GROUP,this);
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error finding member: " + e.getMessage(),e);
                }
                if (objects.size() > 0) {
                    member[0] = objects.get(0);
                }
            }
        });
        removeAll(KEY_JOIN_REQUESTERS, Arrays.asList(member[0]));
        return member[0];
    }

    public Invite getInvite(ParseUser user) {
        final Invite[] invite = new Invite[1];
        ParseQuery<Invite> query = ParseQuery.getQuery(Invite.class);
        Log.i(TAG,"group: " + this.getObjectId());
        query.whereEqualTo(Invite.KEY_TO_JOIN_GROUP,this);
        Log.i(TAG,"user: " + user.getObjectId());
        query.whereEqualTo(Invite.KEY_NEW_MEMBER,user);
        query.findInBackground(new FindCallback<Invite>() {
            @Override
            public void done(List<Invite> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG,"Error finding member: " + e.getMessage(),e);
                }
                if (objects.size() > 0) {
                    Log.i(TAG,"results for " + user.getObjectId() + ": " + objects.size());
                    invite[0] = objects.get(0);
                }
            }
        });
        return invite[0];
    }

    public boolean isMember(ParseUser user) {
        List<ParseUser> members = getMembers();
        for (int i = 0; i < members.size(); i++) {
            if (User.compareUsers(members.get(i),user)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInvited(ParseUser user) {
        List<ParseUser> invitees = getInvitees();
        for (int i = 0; i < invitees.size(); i++) {
            if (User.compareUsers(invitees.get(i),user)) {
                return true;
            }
        }
        return false;
    }

    /* ------------- CHAT METHODS ------------------- */
//    public void setChatFields() {
//        this.chat.put(Chat.KEY_ISGROUPCHAT,true);
//        List<Message> messages = new ArrayList<>();
//        this.chat.put(Chat.KEY_MESSAGES,messages);
//    }
}
