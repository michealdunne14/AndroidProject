package com.example.michealdunne.androidproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private static final int GALLERY_REQUEST = 1;
    private EditText mPostTitle;
    private EditText mPostData;
    private Uri mImageUri = null;

    private Button mSubmitBtn;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mSelectImage = (ImageButton) findViewById(R.id.imageselect);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostData = (EditText) findViewById(R.id.desField);
        mSubmitBtn = (Button) findViewById(R.id.submitbtn);

        mProgress = new ProgressDialog(this);

        mSelectImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }

            private void startPosting() {

                mProgress.setMessage("Posting");
                final String title_Val = mPostTitle.getText().toString().trim();
                final String des_val = mPostData.getText().toString().trim();

                if(!TextUtils.isEmpty(title_Val) && !TextUtils.isEmpty(des_val) && mImageUri != null);
                mProgress.show();
                StorageReference filepath = mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());

                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        DatabaseReference newPost = mDatabase.push();

                        newPost.child("title").setValue(title_Val);
                        newPost.child("desc").setValue(des_val);
                        newPost.child("image").setValue(downloadUrl.toString());
                        mProgress.dismiss();

                        startActivity(new Intent(PostActivity.this,MainActivity.class));
                    }
                });
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode , int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            mImageUri = data.getData();

            mSelectImage.setImageURI(mImageUri);
        }
    }
}
