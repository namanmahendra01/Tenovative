package com.teno.teno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teno.teno.Adapters.PostAdapter;
import com.teno.teno.models.post;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class posts extends AppCompatActivity {
    private TextView bottom_upload;
    private RecyclerView postRv;
    private PostAdapter postAdapter;
    private ArrayList<post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottom_upload = findViewById(R.id.Upload_Bottom);
        postRv = findViewById(R.id.recyclerPost);

//set up recyclerView
        postRv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(posts.this);
        postRv.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();

        getPost();


        bottom_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(posts.this, upload.class);
                startActivity(i);
            }
        });
    }

    private void getPost() {

        postList.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_post))
                .child(getString(R.string.field_user));

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int x = 0;

                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        x++;
                        postList.add(snapshot1.getValue(post.class));
                        if (x == snapshot.getChildrenCount()) {
                            Collections.reverse(postList);
                            postAdapter = new PostAdapter(posts.this, postList);
                            postAdapter.setHasStableIds(true);
                            postRv.setAdapter(postAdapter);
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}