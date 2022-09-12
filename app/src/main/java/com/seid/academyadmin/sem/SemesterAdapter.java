package com.seid.academyadmin.sem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seid.academyadmin.R;
import com.seid.academyadmin.home.HomeFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.ImageViewHolder> {
    private FragmentManager fragmentManager;
    private Context context;
    private List<SemesterObject> semesterObjects;

    public SemesterAdapter(Context context, List<SemesterObject> semesterObjects, FragmentManager fragmentManager) {
        this.context = context;
        this.semesterObjects = semesterObjects;
        this.fragmentManager = fragmentManager;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_semester_item, parent, false);
        return new ImageViewHolder(view, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        final SemesterObject semesterObject = semesterObjects.get(position);
        File img = context.getApplicationContext().getFileStreamPath(semesterObject.getName());
        if (img.exists()) {
            holder.img.setImageBitmap(loadImage(context, semesterObject.getName()));
        } else {
            Picasso.get().load(semesterObject.getImg()).into(holder.img);
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            saveImage(context, Picasso.get().load(semesterObject.getImg()).get(), semesterObject.getName());
                        } catch (IOException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return semesterObjects.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView img;
        FragmentManager fragmentManager;

        private ImageViewHolder(@NonNull View itemView, FragmentManager fragmentManager) {
            super(itemView);
            img = itemView.findViewById(R.id.img);

            this.fragmentManager = fragmentManager;
            img.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Fragment home = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("semester", semesterObjects.get(getAdapterPosition()).getName());
            home.setArguments(bundle);
            semesterObjects.clear();
            fragmentManager.beginTransaction().replace(R.id.fragment_container_view, home).addToBackStack(null).commit();

        }
    }

    private Bitmap loadImage(Context context, String imageName) {
        Bitmap bitmap = null;
        FileInputStream fiStream;
        try {
            fiStream = context.openFileInput(imageName);
            bitmap = BitmapFactory.decodeStream(fiStream);
            fiStream.close();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

    private void saveImage(Context context, Bitmap b, String imageName) {
        FileOutputStream foStream;
        try {
            foStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 80, foStream);
            foStream.close();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
