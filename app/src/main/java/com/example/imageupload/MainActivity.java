package com.example.imageupload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Http.ApiClient;
import Http.ApiInterface;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
    ApiInterface apiInterface = ApiClient.getBaseClient().create(ApiInterface.class);
    String img1 = "image";
    Boolean imageSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pickImage = findViewById(R.id.pickImage);
        Button uploadImage = findViewById(R.id.uploadImage);

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissionREAD_EXTERNAL_STORAGE(getBaseContext()) == true) {

                    pickImageFromGallery();
                }

            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                MultipartBody.Part pic = null;

                if (!imageSelected) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setMessage("Select an image to upload");
                    builder1.setCancelable(true);
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else {
                    if (imagesEncodedList.size() > 0) {
                        for (int i = 0; i < imagesEncodedList.size(); i++) {
                            File file = new File(imagesEncodedList.get(i));
                            Log.d("val1", file.toString());
                            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                            pic = MultipartBody.Part.createFormData(img1 + i, file.getName(), requestBody);

                            Log.d("val1", pic.toString());


                            Call<Images> call = apiInterface.uploadData(pic);

                            call.enqueue(new Callback<Images>() {
                                @Override
                                public void onResponse(Call<Images> call, Response<Images> response) {
                                    Log.d("val1", "maybe");
                                    if (response.isSuccessful()) {
                                        Images addEventResponse = response.body();
                                        Log.d("val1", "response multiple image: " + addEventResponse);
                                        imageSelected = false;
                                    }
                                }

                                @Override
                                public void onFailure(Call<Images> call, Throwable t) {
                                    Log.d("val1", t.toString());
                                }
                            });
                        }
                        imageEncoded = null;
                        imagesEncodedList.clear();

                    }

                    if (imageEncoded != null) {

                        File file = new File(imageEncoded);
                        Log.d("val1", file.toString());
                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                        pic = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

                        Log.d("val1", pic.toString());

                        Call<Images> call = apiInterface.uploadData(pic);

                        call.enqueue(new Callback<Images>() {
                            @Override
                            public void onResponse(Call<Images> call, Response<Images> response) {
                                Log.d("val1", "maybe");
                                if (response.isSuccessful()) {
                                    Images addEventResponse = response.body();
                                    Log.d("val1", "response single image: " + addEventResponse);
                                    imageSelected = false;
                                }
                            }

                            @Override
                            public void onFailure(Call<Images> call, Throwable t) {
                                Log.d("val1", t.toString());
                            }
                        });

                    }

                }
            }

        });

    }

    //Picking image
    public void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {

                mArrayUri.clear();
                // Getting Image from data
                //String[] filePathColumn = {MediaStore.Images.Media.DATA};
                imagesEncodedList = new ArrayList<String>();

                if (data.getData() != null) {

                    Uri mImageUri = data.getData();

                    // Get the cursor
                   /* Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    assert cursor != null;
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);

                    cursor.close();

                    mArrayUri.add(mImageUri);
                    Log.d("val1", "" + mArrayUri.get(0));
                    Log.d("val1", "" + imageEncoded);*/

                    String fileId = DocumentsContract.getDocumentId(mImageUri);
                    // Split at colon, use second item in the array
                    String id = fileId.split(":")[1];
                    String[] column = {MediaStore.Images.Media.DATA};
                    String selector = MediaStore.Images.Media._ID + "=?";
                    Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, selector, new String[]{id}, null);
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        imageEncoded = cursor.getString(columnIndex);
                    }
                    cursor.close();
                    mArrayUri.add(mImageUri);
                    Log.d("val2", "Selected Single Image" + mArrayUri.size());

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();

                        mArrayUri.clear();

                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();

                            mArrayUri.add(uri);

                            // Get the cursor
                           /* Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            Log.d("val1","8");
                            cursor.moveToFirst();
                            Log.d("val1","9");
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            Log.d("val1",""+cursor.getString(columnIndex));
                            imageEncoded  = cursor.getString(columnIndex);
                            Log.d("val1",imageEncoded);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();*/

                            String fileId = DocumentsContract.getDocumentId(uri);
                            // Split at colon, use second item in the array
                            String id = fileId.split(":")[1];
                            String[] column = {MediaStore.Images.Media.DATA};
                            String selector = MediaStore.Images.Media._ID + "=?";
                            Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    column, selector, new String[]{id}, null);
                            int columnIndex = cursor.getColumnIndex(column[0]);
                            if (cursor.moveToFirst()) {
                                imageEncoded = cursor.getString(columnIndex);
                                imagesEncodedList.add(imageEncoded);
                            }
                            cursor.close();

                        }
                        Log.d("val1", "Selected Multiple Images" + mArrayUri.size());
                        Log.d("val1", "Selected Multiple Images" + imagesEncodedList.size());
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
            imageSelected = true;
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong " + e.toString(), Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    //Checks for permission
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    // If permission not available
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // After permission granted
                    pickImageFromGallery();

                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

}

