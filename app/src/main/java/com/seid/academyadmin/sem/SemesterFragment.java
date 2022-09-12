package com.seid.academyadmin.sem;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.seid.academyadmin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SemesterFragment extends Fragment {
    private List<SemesterObject> semesterObjects = new ArrayList<>();
    private RecyclerView recyclerView;
    private CircularProgressBar progressBar;
    DatabaseReference databaseReference;
    private Uri uri;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setLanguage();
        setHasOptionsMenu(true);
        final View root = inflater.inflate(R.layout.fragment_semester, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = root.findViewById(R.id.progress_bar);
        databaseReference = FirebaseDatabase.getInstance().getReference("new").child("am").child("semesters");
        //((MainActivity) getActivity()).setActionBarTitle(getContext().getResources().getString(R.string.app_name));
        addCourses();
        return root;
    }

    private void addCourses() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                semesterObjects.clear();
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue().toString();
                    String img_url = snapshot.child("img").getValue().toString();
                    semesterObjects.add(new SemesterObject(name, img_url));
                }
                FragmentManager fragmentManager = getFragmentManager();
                SemesterAdapter semesterAdapter = new SemesterAdapter(getContext(), semesterObjects, fragmentManager);
                recyclerView.setAdapter(semesterAdapter);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLanguage() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("lang", Context.MODE_PRIVATE);
        Locale locale = new Locale(sharedPreferences.getString("lang", "am"));
        Configuration configuration = new Configuration();
        Locale.setDefault(locale);
        configuration.locale = locale;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    ImageView image;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.add_semester);
            dialog.show();
            SemesterObject sem = new SemesterObject();
            EditText name = dialog.findViewById(R.id.name);
            image = dialog.findViewById(R.id.image);
            image.setOnClickListener(view -> {
                openFileChooser();
            });
            CircularProgressBar progressBar = dialog.findViewById(R.id.progress_bar);
            Button submit = dialog.findViewById(R.id.submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (check()) {
                        name.setEnabled(false);
                        image.setEnabled(false);
                        dialog.setCancelable(false);
                        progressBar.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.GONE);
                        StorageReference storage = FirebaseStorage.getInstance().getReference()
                                .child("new")
                                .child(Integer.toString(semesterObjects.size() + 1))
                                .child("photos")
                                .child(Integer.toString(semesterObjects.size() + 1));
                        storage.putFile(uri).addOnSuccessListener(taskSnapshot -> storage.getDownloadUrl().addOnSuccessListener(uri -> {
                            sem.setImg(uri.toString());
                            sem.setName(name.getText().toString());
                            FirebaseDatabase.getInstance().getReference()
                                    .child("new")
                                    .child("am")
                                    .child("semesters")
                                    .child(Integer.toString((semesterObjects.size() + 1)))
                                    .setValue(sem);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("new")
                                    .child("am")
                                    .child(name.getText().toString())
                                    .child("courses")
                                    .setValue("");
                            dialog.dismiss();
                        }));
                    }
                }

                private boolean check() {
                    if (name.getText().toString().isEmpty()) {
                        name.setError("Required");
                        return false;
                    }
                    if (uri == null) {
                        Toast.makeText(getContext(), "Attach Image File", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                }
            });
            Log.e("Add", "semester");
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            image.setImageDrawable(getContext().getDrawable(R.drawable.check_white));
        } else {
            Toast.makeText(getContext(), "" + resultCode, Toast.LENGTH_SHORT).show();
        }
    }

}