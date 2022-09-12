package com.seid.academyadmin;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.seid.academyadmin.test.Quiz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class Detail extends Fragment {


    public Detail() {
        // Required empty public constructor
    }

    private SharedPreferences has_quiz;
    private SharedPreferences passed;
    private SharedPreferences.Editor lesson_editor, quiz_editor, editor_passed;
    private TextView audio;
    private Button quiz;
    private String language, course_name, part_number, course_code, semester;
    private String youtube_link, audio_link, pdf_link;
    //music player elements
    private LinearLayout linearLayout;
    private TextView startTimeField;
    private TextView endTimeField;
    private MediaPlayer mediaPlayer;
    private Handler myHandler = new Handler();
    private SeekBar seekbar;
    private ImageButton playButton;
    public static int oneTimeOnly = 0;
    private List<List<String>> choices = new ArrayList<>();
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setLanguage();
        setHasOptionsMenu(true);
        root = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView youtube = root.findViewById(R.id.youtube);
        audio = root.findViewById(R.id.audio);
        TextView pdf = root.findViewById(R.id.pdf);
        quiz = root.findViewById(R.id.quiz);
        SharedPreferences lang = getContext().getSharedPreferences("lang", Context.MODE_PRIVATE);
        language = lang.getString("lang", "am");
        semester = getArguments().getString("semester");
        course_name = getArguments().getString("course_name");
        course_code = getArguments().getString("course_code");
        part_number = getArguments().getString("part_number");
        SharedPreferences lessons = getContext().getSharedPreferences("lessons", Context.MODE_PRIVATE);
        has_quiz = getContext().getSharedPreferences("has_quiz", Context.MODE_PRIVATE);
        passed = getContext().getSharedPreferences("passed", Context.MODE_PRIVATE);
        editor_passed = passed.edit();
        lesson_editor = lessons.edit();
        quiz_editor = has_quiz.edit();
        /*if (part_number.equalsIgnoreCase("1"))
            lesson_editor.putBoolean(course_code + part_number, false).apply();*/
        //changed due to change in policy
        if (lessons.getBoolean(semester + course_code + part_number, false))
            quiz.setVisibility(View.VISIBLE);
        //((MainActivity) getActivity()).setActionBarTitle(getArguments().getString("title"));
        getLinks();
        youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lesson_editor.putBoolean(semester + course_code + (Integer.parseInt(part_number)), true).apply();
                //editor_passed.putBoolean(course_code + part_number, true).apply();
                try {
                    mediaPlayer.pause();
                } catch (Exception e) {
                }
                setProgress();
                //startActivity(new Intent(getContext(), VideoPlayManager.class).putExtra("link", youtube_link).putExtra("title", course_name + part_number));
                //quiz button set to invisible
                quiz.setVisibility(View.VISIBLE);
            }
        });
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        download();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    download();
                }
            }
        });
        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        downloadPdf();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    downloadPdf();
                }
            }
        });
        quiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Quiz.class)
                        .putExtra("course_code", course_code)
                        .putExtra("semester", semester)
                        .putExtra("quiz", part_number));

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();


            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        linearLayout = root.findViewById(R.id.musicplayer);
        startTimeField = root.findViewById(R.id.starttime);
        endTimeField = root.findViewById(R.id.endtime);
        seekbar = root.findViewById(R.id.seekbar1);
        playButton = root.findViewById(R.id.pause_play_btn);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playButton.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    playButton.setImageResource(R.drawable.pause);
                }
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////end
        return root;
    }

    String img_url;
    String name;
    String part;
    int p = 0;

    private void setProgress() {
        final SharedPreferences user = getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String phone = user.getString("phone", "");
        final SharedPreferences userProgress = getContext().getSharedPreferences(phone + semester + course_code, Context.MODE_PRIVATE);
        DatabaseReference course = FirebaseDatabase.getInstance().getReference("new").child(language).child(semester).child("courses").child(course_code);
        final DatabaseReference progress = FirebaseDatabase.getInstance().getReference("users").child(phone).child("progress").child(semester).child(course_code);
        course.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                img_url = dataSnapshot.child("image").getValue().toString();
                name = dataSnapshot.child("name").getValue().toString();
                part = dataSnapshot.child("parts").getValue().toString();
                p = (int) ((double) Integer.parseInt(part_number) / (double) Integer.parseInt(part) * 100);
                if (p > Integer.parseInt(userProgress.getString("progress", "0"))) {
                    progress.child("course_name").setValue(name);
                    progress.child("progress").setValue(p);
                    progress.child("img_url").setValue(img_url);
                    progress.child("lesson").setValue(Integer.parseInt(part_number) + 1);
                    userProgress.edit().putString("progress", p + "").apply();
                    userProgress.edit().putString("lesson", (Integer.parseInt(part_number) + 1) + "").apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void downloadPdf() {
        try {
            lesson_editor.putBoolean(semester + course_code + (Integer.parseInt(part_number)), true).apply();
            //editor_passed.putBoolean(course_code + part_number, true).apply();
            String root = Environment.getExternalStorageDirectory().toString();
            File dir = new File(root + "/africa/" + course_code + "/pdfs");
            if (!dir.exists())
                dir.mkdirs();
            final File file = new File(dir, course_code + part_number + ".pdf");
            if (file.isFile() && file.length() > 0) {
                /*startActivity(new Intent(getContext(), Reader.class)
                        .putExtra("file", file.toString()));
                //setProgress();
                quiz.setVisibility(View.VISIBLE);*/
            }
            /*else {
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.wait);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(pdf_link);
                    if (!file.exists())
                        file.createNewFile();
                    storageReference.getFile(file)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    dialog.dismiss();
                                    startActivity(new Intent(getContext(), Reader.class).putExtra("file", file.toString()));
                                    //setProgress();
                                    quiz.setVisibility(View.VISIBLE);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    if (e.getMessage().contains("location"))
                                        Toast.makeText(getContext(), getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();

                                }
                            });
                }
                catch (Exception e) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    Toast.makeText(getContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                }
            }*/
        } catch (Exception e) {
            Toast.makeText(getContext(), "ERROR 103\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void getLinks() {
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("new").child("am").child(semester).child(course_name).child(part_number);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        youtube_link = dataSnapshot.child("youtube").getValue().toString();
                        audio_link = dataSnapshot.child("music").getValue().toString();
                        pdf_link = dataSnapshot.child("pdf").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "ERROR 102\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    TextView percent;

    private void download() {
        //final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), course_code + part_number + ".ogg");
        /*try {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            String root = Environment.getExternalStorageDirectory().toString();
            File dir = new File(root + "/africa/" + course_code + "/audios");
            dir.mkdirs();
            final File file = new File(dir, course_code + part_number + ".ogg");
            if (file.isFile() && file.length() > 0) {
                play();
            } else {
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.confirmation);
                final Button download = dialog.findViewById(R.id.download);
                final Button cancel = dialog.findViewById(R.id.cancel);
                percent = dialog.findViewById(R.id.percent);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        download.setVisibility(View.GONE);
                        percent.setVisibility(View.VISIBLE);
                        cancel.setVisibility(View.GONE);
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference(audio_link);
                        try {
                            file.createNewFile();
                            storageReference.getFile(file)
                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            dialog.dismiss();
                                            play();
                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(final FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            percent.setText(getResources().getString(R.string.downloading) + "...\n" + 100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount() + "%");
                                        }

                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            if (e.getMessage().contains("location"))
                                                Toast.makeText(getContext(), getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } catch (Exception e) {
                            dialog.dismiss();
                            Toast.makeText(getContext(), getString(R.string.try_again) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "ERROR 101\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }*/
    }


    private void play() {

        lesson_editor.putBoolean(semester + course_code + (Integer.parseInt(part_number)), true);
        lesson_editor.commit();
        //setProgress();
        linearLayout.setVisibility(View.VISIBLE);
        playButton.setImageResource(R.drawable.pause);
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File dir = new File(root + "/africa/" + course_code + "/audios");
            final File file = new File(dir, course_code + part_number + ".ogg");
            mediaPlayer.setDataSource(file.toString());
            mediaPlayer.prepare();
        } catch (final Exception e) {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
       /* MusicManager.stop();
        MusicManager.Player(mediaPlayer);*/
        int finalTime = mediaPlayer.getDuration();
        int startTime = mediaPlayer.getCurrentPosition();
        if (oneTimeOnly == 0) {
            seekbar.setMax(finalTime);
            oneTimeOnly = 0;
        }
        endTimeField.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) finalTime)))
        );
        startTimeField.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) startTime)))
        );
        seekbar.setProgress(startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
        quiz.setVisibility(View.VISIBLE);
    }

    private Runnable UpdateSongTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        public void run() {
            int startTime = mediaPlayer.getCurrentPosition();
            startTimeField.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    linearLayout.setVisibility(View.GONE);
                }
            });
            seekbar.setProgress(startTime);
            myHandler.postDelayed(this, 1000);
        }
    };

    private void setLanguage() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("lang", Context.MODE_PRIVATE);
        Locale locale = new Locale(sharedPreferences.getString("lang", "am"));
        Configuration configuration = new Configuration();
        Locale.setDefault(locale);
        configuration.locale = locale;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    Context context;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            Log.e("Add", "semester : " + semester + "\n course : " + course_name + "\n part : " + part_number);
        }
        return super.onOptionsItemSelected(item);
    }
}
