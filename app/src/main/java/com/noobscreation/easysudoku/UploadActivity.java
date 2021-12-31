package com.noobscreation.easysudoku;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.noobscreation.easysudoku.Retrofit.IUploadAPI;
import com.noobscreation.easysudoku.Retrofit.RetrofitClient;
import com.noobscreation.easysudoku.Utils.Common;
import com.noobscreation.easysudoku.Utils.IUploadCallbacks;
import com.noobscreation.easysudoku.Utils.ProgressRequestBody;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UploadActivity extends AppCompatActivity implements IUploadCallbacks {

    private static final int PICK_FILE_REQUEST = 1;
    IUploadAPI mService;
    Button btn_upload, button_back;
    ImageView imageView;
    Uri selectedFileUri;
    ProgressDialog dialog;

    private IUploadAPI getAPIUpload(){
        return RetrofitClient.getClient().create(IUploadAPI.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(UploadActivity.this, "permissions accepted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(UploadActivity.this, "Accept permissions", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        mService = getAPIUpload();

        btn_upload = findViewById(R.id.btn_upload);
        imageView = findViewById(R.id.imageView);
        button_back = findViewById(R.id.button_back);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadActivity.this, ChoiceActivity.class));
                finish();
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == PICK_FILE_REQUEST){
                if (data != null){
                    selectedFileUri = data.getData();
                    if (selectedFileUri != null && !selectedFileUri.getPath().isEmpty()){
                        imageView.setImageURI(selectedFileUri);
                    }else {
                        Toast.makeText(this, "can't find file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void chooseFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void uploadFile(){
        if (selectedFileUri != null){
            dialog = new ProgressDialog(UploadActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Uploading...");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();

            File file = null;
            try {
                file = new File(Common.getFilePath(this, selectedFileUri));

            }catch (URISyntaxException e){
                e.printStackTrace();
            }
            if (file != null){
                final ProgressRequestBody requestBody = new ProgressRequestBody(file, this);
                final MultipartBody.Part body = MultipartBody.Part.createFormData("image",file.getName(), requestBody);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mService.uploadFile(body)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        dialog.dismiss();
                                        String image_processed_link = new StringBuilder("http://10.0.2.2:5000/"+response.body().replace("\"","")).toString();
                                        Picasso.get().load(image_processed_link).into(imageView);
                                        Toast.makeText(UploadActivity.this, "detected!!", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        dialog.dismiss();
                                        Toast.makeText(UploadActivity.this, "failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).start();
            }else {
                Toast.makeText(this, "file is null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProgressUpdate(int percent) {

    }
}