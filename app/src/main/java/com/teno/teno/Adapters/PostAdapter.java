package com.teno.teno.Adapters;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import com.google.firebase.database.ValueEventListener;
import com.teno.teno.R;
import com.teno.teno.models.post;


import java.util.List;

import static android.content.ContentValues.TAG;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<post> postList;

    public PostAdapter(Context mContext, List<post> postList) {
        this.mContext = mContext;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int i) {

        post post=postList.get(i);

        Glide.with(mContext)
                .load(post.getImgUrl())
                .placeholder(R.drawable.ic_error)
                .into(holder.post);

        getLikeNumber(post,holder);

        getUsername(post,holder);
        
        getCaption(post,holder);

        holder.liked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLike(post.getPostId(),holder.likeNumber);
                toggleLike(holder);

            }
        });

        holder.notLiked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addLike(post.getPostId(),holder.likeNumber);
                toggleLike(holder);

            }
        });


    }

    private void getCaption(post post, ViewHolder holder) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_post))
                .child(mContext.getString(R.string.field_user))
                .child(post.getPostId())
                .child(mContext.getString(R.string.caption));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.caption.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void toggleLike(ViewHolder holder) {
        if (holder.liked.getVisibility()==View.VISIBLE){
            holder.liked.setVisibility(View.GONE);
            holder.notLiked.setVisibility(View.VISIBLE);

        }else{
            holder.liked.setVisibility(View.VISIBLE);
            holder.notLiked.setVisibility(View.GONE);
        }
    }

    private void getUsername(post post, ViewHolder holder) {
        holder.username.setText("Naman");

    }

    private void addLike(String postId, TextView likeNumber) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_post))
                .child(mContext.getString(R.string.field_user))
                .child(postId)
                .child(mContext.getString(R.string.field_like));

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            int x=Integer.parseInt(snapshot.getValue().toString());
                            ref.setValue(x+1);
                            likeNumber.setText(String.valueOf(x+1));
                        }else{
                            likeNumber.setText(String.valueOf(1));
                            ref.setValue(1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void removeLike(String postId, TextView likeNumber) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_post))
                .child(mContext.getString(R.string.field_user))
                .child(postId)
                .child(mContext.getString(R.string.field_like));

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int x=Integer.parseInt(snapshot.getValue().toString());
                    ref.setValue(x-1);
                    likeNumber.setText(String.valueOf(x-1));

                }else{
                    likeNumber.setText(String.valueOf(0));
                    ref.setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLikeNumber(post post, ViewHolder holder) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_post))
                .child(mContext.getString(R.string.field_user))
                .child(post.getPostId())
                .child(mContext.getString(R.string.field_like));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.likeNumber.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public long getItemId(int position) {
        post post = postList.get(position);
        return post.getImgUrl().hashCode();
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username, caption,likeNumber;
        private ImageView post,liked,notLiked;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            username = itemView.findViewById(R.id.username);
            caption = itemView.findViewById(R.id.caption);
            liked = itemView.findViewById(R.id.like);
            notLiked = itemView.findViewById(R.id.notLiked);
            likeNumber = itemView.findViewById(R.id.likeNumber);
            post = itemView.findViewById(R.id.post);



        }
    }




}
