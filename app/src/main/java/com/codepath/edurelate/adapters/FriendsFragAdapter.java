package com.codepath.edurelate.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.edurelate.models.Group;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FriendsFragAdapter extends RecyclerView.Adapter<FriendsFragAdapter.ViewHolder> {

    public static final String TAG = "FriendsFragAdapter";

    Context context;
    List<Group> friends;

    public FriendsFragAdapter(Context context, List<Group> friends) {
        this.context = context;
        this.friends = friends;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FriendsFragAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }
    }
}
