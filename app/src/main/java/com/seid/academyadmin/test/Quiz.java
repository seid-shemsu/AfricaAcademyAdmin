package com.seid.academyadmin.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.seid.academyadmin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Quiz extends AppCompatActivity {
    RecyclerView recyclerView;
    Button submit;
    CircularProgressBar progressBar;
    List<String> default_answer = new ArrayList<>();
    List<Object> objects = new ArrayList<>();
    QA qa;

    Snackbar snackbar;
    String course_code, quiz, semester;
    int res = 0;
    private SharedPreferences passed;
    private SharedPreferences.Editor editor;
    private TextView no;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLanguage();
        quiz = getIntent().getExtras().getString("quiz");
        setContentView(R.layout.activity_quiz);
        setTitle(quiz);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.recycler);
        no = findViewById(R.id.no);
        recyclerView.setHasFixedSize(true);
        dialog = new Dialog(Quiz.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        submit = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progress_bar);
        semester = getIntent().getExtras().getString("semester");
        course_code = getIntent().getExtras().getString("course_code");

        String part = semester + "_" + course_code +  "_" + quiz;
        getQ(part);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qa.getAnswer().contains("100")) {
                    Toast.makeText(Quiz.this, R.string.attemt_all_question, Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < qa.getAnswer().size(); i++) {
                        if (qa.getAnswer().get(i).equalsIgnoreCase(default_answer.get(i)))
                            res++;
                    }
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    //code for final exam
                }
            }
        });
    }


    private void getQ(String part) {
        final DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("tests").child(part);
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                objects.clear();
                default_answer.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String question = snapshot.child("question").getValue().toString();
                    String answer = snapshot.child("answer").getValue().toString();
                    default_answer.add(answer);
                    List<String> choices = new ArrayList<>();
                    for (DataSnapshot snapshot1 : snapshot.child("choices").getChildren()) {
                        choices.add(snapshot1.getValue().toString());
                    }
                    objects.add(new Object(question, answer, choices));
                }
                progressBar.setVisibility(View.GONE);

                if (objects.size() == 0) {
                    no.setVisibility(View.VISIBLE);
                    getSharedPreferences("passed", MODE_PRIVATE).edit().putBoolean(course_code + quiz, true).apply();
                } else {
                    qa = new QA(Quiz.this, objects, 0);
                    recyclerView.setAdapter(qa);
                    submit.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private boolean connectionCheck() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
        int connected = 0;
        for (NetworkInfo networkInfo : info) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                connected = 1;
            }
        }
        return connected != 0;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    private void setLanguage() {
        SharedPreferences sharedPreferences = getSharedPreferences("lang", MODE_PRIVATE);
        Locale locale = new Locale(sharedPreferences.getString("lang", "am"));
        Configuration configuration = new Configuration();
        Locale.setDefault(locale);
        configuration.locale = locale;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }
}
