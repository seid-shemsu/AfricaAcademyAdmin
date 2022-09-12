package com.seid.academyadmin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.Holder> {

    private FragmentManager fragmentManager;
    Context context;
    List<String> titles, icons, numbers;
    String name;
    String course_code, semester;

    public TitleAdapter(Context context, List<String> titles, List<String> icons, List<String> numbers, FragmentManager fragmentManager, String name, String course_code, String semester) {
        this.context = context;
        this.titles = titles;
        this.fragmentManager = fragmentManager;
        this.name = name;
        this.course_code = course_code;
        this.icons = icons;
        this.numbers = numbers;
        this.semester = semester;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.title, parent, false);
        return new Holder(view, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        //I have changed passed with lesson sharedPreference
        holder.title.setText(titles.get(position));
        //number
        SharedPreferences passed = context.getSharedPreferences("passed", Context.MODE_PRIVATE);
        SharedPreferences lesson = context.getSharedPreferences("lessons", Context.MODE_PRIVATE);
        if (passed.getBoolean(course_code + (position), false) && !passed.getBoolean(course_code + (position + 1), false)) {
            holder.number.setBackground(context.getResources().getDrawable(R.drawable.number_bg));
            holder.number.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.number.setTextColor(context.getResources().getColor(R.color.bunny));
            holder.number.setBackground(null);
        }
        holder.number.setText(numbers.get(position));
        //icon
        if (position != 0) {
            if (passed.getBoolean(icons.get(position - 1), false)) {
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.unlocked));
            } else
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.locked));
        } else {
            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.unlocked));
        }
        if (passed.getBoolean(icons.get(position), false))
            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_correct));

    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, number;
        ImageView icon;
        RelativeLayout linear;
        FragmentManager fragmentManager;

        public Holder(@NonNull View itemView, FragmentManager fragmentManager) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            linear = itemView.findViewById(R.id.linear);
            number = itemView.findViewById(R.id.number);
            icon = itemView.findViewById(R.id.icon);
            this.fragmentManager = fragmentManager;
            title.setOnClickListener(this);
            linear.setOnClickListener(this);
            number.setOnClickListener(this);
            icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SharedPreferences passed = context.getSharedPreferences("passed", Context.MODE_PRIVATE);
            SharedPreferences lesson = context.getSharedPreferences("lessons", Context.MODE_PRIVATE);
            if (passed.getBoolean(course_code + (getAdapterPosition()), false)) {
                Fragment detail = new Detail();
                Bundle bundle = new Bundle();
                bundle.putString("semester", semester);
                bundle.putString("part_number", Integer.toString(getAdapterPosition() + 1));
                bundle.putString("course_name", name);
                bundle.putString("title", titles.get(getAdapterPosition()));
                bundle.putString("course_code", course_code);
                detail.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.fragment_container_view, detail).addToBackStack(null).commit();
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.take_prev_lesson), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
