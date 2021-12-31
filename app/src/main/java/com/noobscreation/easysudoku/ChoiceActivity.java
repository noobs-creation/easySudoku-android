package com.noobscreation.easysudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class ChoiceActivity extends AppCompatActivity {

    private Button upload_image, enter_manually;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hiding title bar
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //making activity fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_choice);


        //setting up buttons
        upload_image = findViewById(R.id.button_image_upload);
        enter_manually = findViewById(R.id.button_enter_manually);

        upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChoiceActivity.this, UploadActivity.class));

            }
        });

        enter_manually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChoiceActivity.this, ManualActivity.class));

            }
        });

    }
}