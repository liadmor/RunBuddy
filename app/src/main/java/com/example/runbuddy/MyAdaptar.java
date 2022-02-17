package com.example.runbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

public class MyAdaptar extends RecyclerView.Adapter<MyAdaptar.MyViewHolder> {
    JSONArray activities;
    Context context;

    public MyAdaptar(Context ct, JSONArray activities){
        context = ct;
        this.activities = activities;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        try {
            holder.starterName.setText(activities.getJSONObject(position).getString("starterUser"));
            holder.distance.setText(activities.getJSONObject(position).getString("distance"));
            holder.dateTime.setText(activities.getJSONObject(position).getString("date") + " " + activities.getJSONObject(position).getString("time"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return activities.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView starterName;
        TextView distance;
        TextView dateTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            starterName = itemView.findViewById(R.id.starterName);
            distance = itemView.findViewById(R.id.distanceShow);
            dateTime = itemView.findViewById(R.id.dateTimeShow);
        }
    }
}