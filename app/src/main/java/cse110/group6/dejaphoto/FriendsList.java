package cse110.group6.dejaphoto;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashSet;


public class FriendsList extends AppCompatActivity implements View.OnClickListener {

    EditText friendEntry;
    HashSet<String> friendSet;
    SharedPreferences friendFile;
    public static final String FRIENDS_LIST_PREFERENCES = "friends_list";
    public static final String FRIENDS_LIST_KEY = "friends";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        getSupportActionBar().setTitle("Friends List");

        friendEntry = (EditText) findViewById(R.id.friendsListEntry);
        friendEntry.setOnClickListener(this);

        friendFile = getSharedPreferences(FRIENDS_LIST_PREFERENCES ,0);
        friendSet = (HashSet) friendFile.getStringSet(FRIENDS_LIST_KEY, new HashSet<String>());
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = friendFile.edit();
        editor.putStringSet(FRIENDS_LIST_KEY, friendSet);
        editor.commit();

        super.onStop();
    }

    @Override
    public void onClick(View v) {
        friendEntry.setText("");
    }

    void addFriend(View v) {
        String friend = friendEntry.getText().toString();

        if(friendSet.contains(friend))
            Toast.makeText(this, "You're already friends with this person!", Toast.LENGTH_SHORT).show();
        else {
            friendSet.add(friend);
            Toast.makeText(this, "Added " + friend + " to your friends list!", Toast.LENGTH_SHORT).show();
        }
    }

    void removeFriend(View v) {
        String friend = friendEntry.getText().toString();

        if(friendSet.contains(friend)) {
            friendSet.remove(friend);
            Toast.makeText(this, "Removed " + friend + " from your friends list", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "You're not friends with this person", Toast.LENGTH_SHORT).show();
    }
}
