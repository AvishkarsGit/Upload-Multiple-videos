package com.example.uploadmultiplevideos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class UploadVideosActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView recyclerView;

    private static final int VIDEO_PICK_CODE=101;
    private static final int STORAGE_REQUEST_CODE=102;
    private String[] storagePermission;

    private ArrayList<String> files,status;

    private Uri videoUri;

    private CustomAdapter adapter;
    private ArrayList<Uri> videosUriList;

    private StorageReference storageReference =
            FirebaseStorage.getInstance().getReference("Videos");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_videos);

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.rvVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //init arrayList
        files=new ArrayList<>();
        status = new ArrayList<>();
        videosUriList = new ArrayList<>();

        //init array
        storagePermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        fab.setOnClickListener(v -> {
            if (checkStoragePermission()){
                pickVideoFromGallery();
            }
            else {
                requestStoragePermission();
            }
        });



    }

    private boolean checkStoragePermission(){
        boolean result =
                ContextCompat.checkSelfPermission(
                        UploadVideosActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED;
        return  result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                UploadVideosActivity.this,storagePermission,STORAGE_REQUEST_CODE
        );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case STORAGE_REQUEST_CODE:
                if (grantResults.length>0)
                {
                    boolean storageAccepted =
                            grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickVideoFromGallery();
                    }
                    else {
                        Toast.makeText(this, "Permission is denied...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void pickVideoFromGallery() {
        Intent i = new Intent();
        i.setType("video/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(Intent.createChooser(i,"Select videos"),VIDEO_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_PICK_CODE && resultCode==RESULT_OK){
            if (data.getClipData()!=null){
                int count =data.getClipData().getItemCount();
                for (int i=0;i<count;i++){
                    final int index = i;
                    videoUri =data.getClipData().getItemAt(i).getUri();
                    videosUriList.add(videoUri);
                    String videoName = getFileName(videoUri);
                    files.add(videoName);
                    status.add("loading");
                    adapter =new CustomAdapter(status,files,UploadVideosActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    StorageReference videoRef = storageReference.child(videoName);
                    videoRef.putFile(videoUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    videoRef.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    DatabaseModel model = new DatabaseModel(videoName,uri.toString());
                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Videos");
                                                    ref.child("video_"+System.currentTimeMillis())
                                                            .setValue(model)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    status.remove(index);
                                                                    status.add(index, "done");
                                                                    adapter.notifyDataSetChanged();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(UploadVideosActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(UploadVideosActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadVideosActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        }
    }

    public String getFileName(Uri filepath) {
        String result = null;
        if (filepath.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(filepath, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = filepath.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}