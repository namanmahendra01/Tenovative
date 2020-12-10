package com.teno.teno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.teno.teno.UtilityClasses.FirebaseMethods;
import com.teno.teno.UtilityClasses.ImageManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teno.teno.UtilityClasses.FilePaths;
import com.teno.teno.UtilityClasses.Permissions;

import java.util.HashMap;


public class upload extends AppCompatActivity {
    private static final int VERIFY_PERMISSION_REQUEST = 1;
    private static final String TAG ="upload" ;
    boolean isKitKat;
    private int imageCount;

    private Button selectBtn;
    private TextView postImageBtn,bottom_post;
    private EditText caption;
    private ImageView postImage;
    private final Context mContext=upload.this;
    private String imgURL;


    //firebase
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        initWidgets();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_post))
                .child(getString(R.string.field_user))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        imageCount= (int) snapshot.getChildrenCount();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: " + checkPermissionArray(Permissions.PERMISSIONS));
                if (checkPermissionArray(Permissions.PERMISSIONS)) {
                    isKitKat = true;
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                } else verifyPermission(Permissions.PERMISSIONS);
            }
        });
        bottom_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, posts.class);
                startActivity(i);
            }
        });

        postImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cap = caption.getText().toString();
                if (imgURL != null && !imgURL.equals("")) {
                    uploadNewPhoto(cap, imgURL);
                } else {
                    Toast.makeText(mContext, "Please Select Image First.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @SuppressLint("DefaultLocale")
    private void uploadNewPhoto(String caption, String imgURL) {
        FilePaths filepaths = new FilePaths();
        String user_id = "1234";
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/post" + (imageCount + 1));
        String imgUrl2 = mFirebaseMethods.compressImage(imgURL);
        Bitmap bm = ImageManager.getBitmap(imgUrl2);
        byte[] bytes;
        bytes = ImageManager.getBytesFromBitmap(bm, 100);
        UploadTask uploadTask;
        uploadTask = storageReference.putBytes(bytes);
        Log.d(TAG, "uploadNewPhoto: uploadTask" + uploadTask);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Toast.makeText(mContext, "Photo Upload success", Toast.LENGTH_SHORT).show();
                addPhotoToDatabase(caption, uri.toString());
            });
            mContext.startActivity(new Intent(mContext, posts.class));
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Photo Upload Failed");
            Toast.makeText(mContext, "Photo Upload failed", Toast.LENGTH_SHORT).show();
            mContext.startActivity(new Intent(mContext, posts.class));
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            ProgressDialog.show(mContext, "", "Uploading... - " + String.format("%.0f", progress) + "%", true);
            Log.d(TAG, "onProgress: upload progress" + progress + "% done");
        });
    }


    private void addPhotoToDatabase(String caption, String url) {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference();
        String key=ref.push().getKey();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("imgUrl",url);
        hashMap.put("cap",caption);
        hashMap.put("isVerified",false);
        hashMap.put("like",0);
        hashMap.put("postId",key);


      ref.child(getString(R.string.dbname_Verification))
              .child(getString(R.string.dbname_post))
              .child(getString(R.string.field_user))
              .child(key)
              .setValue(hashMap);

    }

    private void initWidgets() {
        selectBtn=findViewById(R.id.selectImage);
        postImageBtn=findViewById(R.id.postBtn);
        caption=findViewById(R.id.caption);
        postImage=findViewById(R.id.post);
        bottom_post=findViewById(R.id.Post_Bottom);
        mFirebaseMethods = new FirebaseMethods(upload.this);


    }
    public void verifyPermission(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, VERIFY_PERMISSION_REQUEST);
    }
    public boolean checkPermissionArray(String[] permissions) {
        for (String check : permissions) if (!checkPermissions(check)) return false;
        return true;
    }

    public boolean checkPermissions(String permission) {
        int permissionRequest = ActivityCompat.checkSelfPermission(this, permission);
        return permissionRequest == PackageManager.PERMISSION_GRANTED;
    }
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getPathFromUri(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type))
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                else if ("video".equals(type))
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                else if ("audio".equals(type))
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) return uri.getPath();
        return null;
    }


    @TargetApi(19)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String imgPath = "";
        if (data != null && data.getData() != null && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                imgPath = getPathFromUri(mContext, uri);
                Log.d(TAG, "onActivityResult: path: " + imgPath);
                Log.d(TAG, "onActivityResult: uri: " + uri);
                setImage(imgPath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setImage(String imgPath) {
        Log.d(TAG, "setImage next " + imgPath);
        imgURL = imgPath;
        String mAppend = "file:/";
        Glide.with(getApplicationContext())
                .load(imgURL)
                .placeholder(R.drawable.ic_error)
                .into(postImage);
    }




}