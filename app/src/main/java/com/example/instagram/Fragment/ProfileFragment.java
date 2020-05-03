package com.example.instagram.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.Model.PhotoInProfileAdapter;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class ProfileFragment extends Fragment {

    private TextView posts_tv, followers_tv, following_tv, username_tv, fullname_tv, bio_tv;
    private ImageView profileImage_iv, options_iv;
    private Button edit_btn;
    private ImageButton photos_ibtn, saved_ibtn;

    RecyclerView photosRecyclerView;
    PhotoInProfileAdapter profilePhotosadapter;
    ArrayList<Post> postsList;

    private FirebaseUser firebaseUser;

    private String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        if (prefs.contains("profileId")) {
            // open other profile
            profileId = prefs.getString("profileId", "none");
        } else {
            // open my profile
            profileId = firebaseUser.getUid();
        }


        posts_tv = view.findViewById(R.id.posts_textview);
        followers_tv = view.findViewById(R.id.followers_textview);
        following_tv = view.findViewById(R.id.following_textview);
        username_tv = view.findViewById(R.id.username_textview);
        fullname_tv = view.findViewById(R.id.fullname_textview);
        bio_tv = view.findViewById(R.id.bio_textview);
        profileImage_iv = view.findViewById(R.id.profileImage_imageview);
        options_iv = view.findViewById(R.id.options_imageview);
        edit_btn = view.findViewById(R.id.editProfile_btn);
        photos_ibtn = view.findViewById(R.id.myPhotos_imageBtn);
        saved_ibtn = view.findViewById(R.id.saved_imageBtn);


        photosRecyclerView = view.findViewById(R.id.photos_recycleview);
        photosRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        photosRecyclerView.setLayoutManager(linearLayoutManager);
        postsList = new ArrayList<>();
        profilePhotosadapter = new PhotoInProfileAdapter(getContext(), postsList);
        photosRecyclerView.setAdapter(profilePhotosadapter);


        // update bars info
        updateProfileInfo();
        updateFollowersFollowingNo();
        updatePostsNo();
        // read photos in profile
        readProfilePhotos();

        if (profileId.equals(firebaseUser.getUid())) {
            // my profile
            edit_btn.setText("Edit Profile");
        } else {
            // other profile
            setFollowButtonText();
            saved_ibtn.setVisibility(View.GONE);
        }



        // if my profile then edit ,, if other user then follow or unfollow
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_btn.getText().equals("Edit Profile")) {
                    // edit my profile
                } else if (edit_btn.getText().equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(profileId).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(profileId).child("Followers")
                            .child(firebaseUser.getUid()).setValue(true);
                } else if (edit_btn.getText().equals("Following")) {
                    // already friends, then un-friend
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(profileId).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(profileId).child("Followers")
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });



        // Inflate the layout for this fragment
        return view;
    }

    // update profile info depending on preset profile id (my profile or other)
    private void updateProfileInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getContext()).load(user.getImageUrl()).into(profileImage_iv);
                username_tv.setText(user.getUsername());
                fullname_tv.setText(user.getFullName());
                bio_tv.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // setting text on button (follow or following) in case of other profile
    private void setFollowButtonText() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    edit_btn.setText("Following");
                } else {
                    edit_btn.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update number of followers and following
    private void updateFollowersFollowingNo() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(profileId).child("Followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers_tv.setText("" + dataSnapshot.getChildrenCount());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance()
                .getReference("Follow").child(profileId).child("Following");

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following_tv.setText("" + dataSnapshot.getChildrenCount());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update number of posts
    private void updatePostsNo() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts").child(profileId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts_tv.setText("" + dataSnapshot.getChildrenCount());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readProfilePhotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(profileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    postsList.add(post);
                }
                // here we read from single user ,, no need to sort by time (already sorted)
                // in home fragment we sorted them by time as from many users sorted for each user
                // so we needed to sort the whole result by time
                Collections.reverse(postsList);
                profilePhotosadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
