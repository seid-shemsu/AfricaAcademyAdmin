package com.seid.academyadmin.test;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seid.academyadmin.R;

import java.util.ArrayList;
import java.util.List;

public class QA extends RecyclerView.Adapter<QA.Holder> {
    Context context;
    List<Object> questions;
    List<String> answers;
    List<String> default_answer;
    int code;

    public QA(Context context, List<Object> questions, int code) {
        this.context = context;
        this.questions = questions;
        this.code = code;
        answers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++)
            answers.add("100");
    }

    public QA(Context context, List<Object> questions, List<String> answers, List<String> default_answer, int code) {
        this.context = context;
        this.questions = questions;
        this.answers = answers;
        this.code = code;
        this.default_answer = default_answer;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.question, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, @SuppressLint("RecyclerView") int position) {
        holder.setIsRecyclable(false);
        if (code == 0) {
            Object object = questions.get(position);
            holder.q.setText(object.getQuestion());
            holder.group.removeAllViews();
            int j = object.getChoices().size();
            for (int i = 0; i < j; i++) {
                RadioButton ch = new RadioButton(context);
                ch.setText(object.getChoices().get(i));
                ch.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                ch.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                ch.setId(i);
                ch.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (i == Integer.parseInt(answers.get(position))) {
                    ch.setChecked(true);
                }

                holder.group.addView(ch);
            }
            holder.group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    answers.set(position, String.valueOf(group.getCheckedRadioButtonId()));
                }
            });
        } else if (code == 1) {
            final Object object = questions.get(position);
            holder.q.setText(object.getQuestion());
            int j = object.getChoices().size();
            holder.group.removeAllViews();
            for (int i = 0; i < j; i++) {
                RadioButton ch = new RadioButton(context);
                ch.setText(object.getChoices().get(i));
                ch.setId(i);
                ch.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (i == Integer.parseInt(answers.get(position))) {
                    if (answers.get(position).equalsIgnoreCase(default_answer.get(position))) {
                        ch.setTextColor(context.getResources().getColor(R.color.green));
                        ch.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.circle_correct), null);
                    } else {
                        ch.setTextColor(Color.RED);
                        ch.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.circle_wrong), null);
                    }
                } else
                    ch.setTextColor(Color.BLACK);

                ch.setEnabled(false);
                holder.group.addView(ch);
            }
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView q;
        RadioGroup group;

        Holder(@NonNull View itemView) {
            super(itemView);
            q = itemView.findViewById(R.id.question);
            group = itemView.findViewById(R.id.group);
        }
    }

    public List<String> getAnswer() {
        return answers;
    }
}
