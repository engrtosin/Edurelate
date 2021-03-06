package com.codepath.edurelate.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.Collections;
import java.util.List;

@Parcel(analyze = Message.class)
@ParseClassName("Message")
public class Message extends ParseObject {

    public static final String TAG = "MessageModel";
    public static final String KEY_BODY = "body";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_RECIPIENT = "recipient";
    public static final String KEY_REPLY_TO = "replyTo";
    public static final String KEY_USERS_LIKING_THIS = "usersLikingThis";
    public static final String KEY_GROUP = "group";
    public static final int TRUNCATED_BODY_LENGTH = 40;
    public static final int MESSAGE_OUTGOING = 789;
    public static final int MESSAGE_INCOMING = 321;

    public ParseUser sender;
    public String body;
    public Message replyTo;
    public Group recipient;

    /* ------------------------ GET METHODS ------------------------ */
    public String getBody(boolean isFull) {
        String body = getString(KEY_BODY);
        if (body.length() < TRUNCATED_BODY_LENGTH) {
            return body;
        }
        if (!isFull) {
            return body.substring(0,TRUNCATED_BODY_LENGTH) + "...";
        }
        return body;
    }
    
    public ParseUser getSender() {
        return getParseUser(KEY_SENDER);
    }

    public ParseUser getRecipient() {
        return getParseUser(KEY_RECIPIENT);
    }

    public Group getGroup() {
        return (Group) getParseObject(KEY_GROUP);
    }

    public Message getReplyTo() {
        return (Message) getParseObject(KEY_REPLY_TO);
    }

    public List<ParseUser> getUsersLikingThis() {
        return getList(KEY_USERS_LIKING_THIS);
    }

    public int getLikeCount() {
        return getUsersLikingThis().size();
    }

    /* ------------------------ OTHER METHODS ------------------------ */
    public void unlikeThis(ParseUser user) {
        removeAll(KEY_USERS_LIKING_THIS, Collections.singleton(user));
    }

    public void likeThis(ParseUser user) {
        add(KEY_USERS_LIKING_THIS,user);
    }
}