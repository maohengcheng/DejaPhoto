package cse110.group6.dejaphoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendsList extends AppCompatActivity {
    public static final String FRIENDS_LIST_REFERENCE = "Friends";

    EditText friendEntry;
    DatabaseReference mDatabaseRef;
    FriendArrayAdapter friendAdapter;
    ArrayList<String> friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        getSupportActionBar().setTitle("Friends List");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(user.getUid() + "/" + FRIENDS_LIST_REFERENCE);

        // Create listener to get friend list from database
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            boolean firstInstance = true;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!firstInstance)
                    return;

                Object friends = dataSnapshot.getValue();
                if(friends == null)
                    friendList = new ArrayList<String>();
                else
                    friendList = (ArrayList<String>) friends;

                ListView friendListView = (ListView) findViewById(R.id.friendsListView);
                friendAdapter = new FriendArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, friendList);
                friendListView.setAdapter(friendAdapter);

                firstInstance = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    void addFriend(View v) {
        if(friendList == null) {
            Toast.makeText(this, "Unable to complete action at this time", Toast.LENGTH_SHORT).show();
            return;
        }

        String friend = friendEntry.getText().toString();

        if(friendList.contains(friend))
            Toast.makeText(this, "You're already friends with this person!", Toast.LENGTH_SHORT).show();
        else {
            friendList.add(friend);
            mDatabaseRef.setValue(friendList);
            friendAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Added " + friend + " to your friends list!", Toast.LENGTH_SHORT).show();
        }
    }

    void removeFriend(View v) {
        if(friendList == null) {
            Toast.makeText(this, "Unable to complete action at this time", Toast.LENGTH_SHORT).show();
            return;
        }

        String friend = friendEntry.getText().toString();

        if(friendList.contains(friend)) {
            friendList.remove(friend);
            mDatabaseRef.setValue(friendList);
            friendAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Removed " + friend + " from your friends list", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "You're not friends with this person", Toast.LENGTH_SHORT).show();
    }

    private class FriendArrayAdapter extends ArrayAdapter<String> {
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public FriendArrayAdapter(Context context, int resourceId, List<String> friends) {
            super(context, resourceId, friends);
            for (int i = 0; i < friends.size(); i++) {
                mIdMap.put(friends.get(i), i);
            }
        }
    }
}
