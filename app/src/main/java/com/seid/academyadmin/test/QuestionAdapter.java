package com.seid.academyadmin.test;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.seid.academyadmin.R;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends ArrayAdapter<String>{
   // String[] questions;
    Context context;
    List<String> answer = new ArrayList<>();
    RadioButton tr, fl;
    int type;
    int[] answerCheck = new int[100];
    String[] answered = new String[100];
    LinearLayout linearLayout;
    ArrayList<String> questions;

    public QuestionAdapter(Context context, ArrayList<String> questions, int type){
        super(context, R.layout.singli_question);
        this.questions = questions;
        this.context = context;
        this.type = type;
        for (int i=0; i<questions.size(); i++)
            answer.add("-");
    }
    public QuestionAdapter(Context context, String[] answered, ArrayList<String> questions, int[] answerCheck, int type){
        super(context, R.layout.singli_question);
        this.questions = questions;
        this.context = context;
        this.answered = answered;
        this.type = type;
        this.answerCheck = answerCheck;
        for (int i=0; i<questions.size(); i++)
            answer.add("-");
    }

    public List<String> getAnswer() {
        return answer;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.singli_question, parent, false);
        //Toast.makeText(context, answer.size() + "", Toast.LENGTH_SHORT).show();
        TextView question = view.findViewById(R.id.question);
        question.setText(questions.get(position));
        tr = view.findViewById(R.id.choice_1);
        fl = view.findViewById(R.id.choice_2);
//        linearLayout = view.findViewById(R.id.homeLinear);
        if (type == 0){
            if (answer.get(position).equalsIgnoreCase("true"))
                tr.setChecked(true);
            else if (answer.get(position).equals("false"))
                fl.setChecked(true);
        }
        if (type == 1){
            tr.setTextColor(Color.BLACK);
            fl.setTextColor(Color.BLACK);
            if (answered[position] == "true")
                tr.setChecked(true);
            else if (answered[position] == "false")
                fl.setChecked(true);
            if (answerCheck[position] == 0){
                if (tr.isChecked()){
                    tr.setTextColor(Color.RED);
                    //tr.setBackgroundResource(R.drawable.wrong_bg);
                    tr.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, view.getResources().getDrawable(R.drawable.circle_wrong), null);
                }
                else{
                    fl.setTextColor(Color.RED);
                    //fl.setBackgroundResource(R.drawable.wrong_bg);
                    fl.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, view.getResources().getDrawable(R.drawable.circle_wrong), null);
                }


            }
            else if (answerCheck[position] == 1 ){
                if (tr.isChecked()){
                    tr.setTextColor(context.getResources().getColor(R.color.green));
                    //tr.setBackgroundResource(R.drawable.correct_bg);
                    tr.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, view.getResources().getDrawable(R.drawable.circle_correct), null);
                }
                else{
                    fl.setTextColor(context.getResources().getColor(R.color.green));
                    fl.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, view.getResources().getDrawable(R.drawable.circle_correct), null);
                    //fl.setBackgroundResource(R.drawable.correct_bg);
                }
            }
        }

        tr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    answer.set(position, "true");
                }
            }
        });
        fl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    answer.set(position, "false");
            }
        });
        return view;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return questions.get(position);
    }

    @Override
    public int getCount() {
        return questions.size();
    }
}
