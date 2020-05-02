package com.example.instagram.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.instagram.Model.User;
import com.example.instagram.Model.UserAdapter;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


public class SearchFragment extends Fragment {
    private ListView usersListView;
    private UserAdapter userAdapter;
    private ArrayList<User> users;
    private EditText search_edittext;
    private Context mContext;

    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        usersListView = view.findViewById(R.id.search_listView);
        search_edittext = view.findViewById(R.id.search_editText);

        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), users);
        usersListView.setAdapter(userAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        readAllUsers();

        // TODO : empty seach gives all users for debug purpose
        // begin search on entering any input
        search_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.toString().equals("")) {
//                    users.clear();
//                    userAdapter.notifyDataSetChanged();
//                } else {
//                    searchForUsers(charSequence.toString().toLowerCase().trim());
//                }
                // TODO for debug purpose but after that remove all users seach option
                searchForUsers(charSequence.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        // Inflate the layout for this fragment
        return view;
    }

    /*
        search for current input username
        adapter and user list updated here
     */
    private void searchForUsers(String username) {
        Query query = FirebaseDatabase.getInstance().getReference("Users").
                orderByChild("username").startAt(username).endAt(username + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    // to prevent showing current user in search
//                    if (! user.getId().equals(firebaseUser.getUid())) {
//                        users.add(user);
//                    }
                    users.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readAllUsers() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // if empty search get all users
                if (search_edittext.getText().toString().equals("")) {
                    users.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        // to prevent showing current user in search
//                    if (! user.getId().equals(firebaseUser.getUid())) {
//                        users.add(user);
//                    }
                        users.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
