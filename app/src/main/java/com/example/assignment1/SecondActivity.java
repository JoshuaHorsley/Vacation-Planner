package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

public class SecondActivity extends ComponentActivity {
    Button goBack = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        goBack = findViewById(R.id.goBack2);
        String msgFromMain = getIntent().getStringExtra("data_from_main_to_2");
        Toast.makeText(this, msgFromMain, Toast.LENGTH_LONG).show();

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendBack = new Intent();
                sendBack.putExtra("data_from_2_to_main", "hello from activity 2");
                setResult(ResultCodes.RESULT_FROM_ACTIVITY_2, sendBack);
                finish();
            }
        });
    }
}
