package com.example.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

public class MainActivity extends ComponentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    Button goTo2Button = null;
    Button goTo3Button = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goTo3Button = findViewById(R.id.goTo3Button);
        goTo2Button = findViewById(R.id.goTo2Button);

        ActivityResultLauncher<Intent> getContent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode() == ResultCodes.RESULT_FROM_ACTIVITY_2) {
                            Intent receivedData = o.getData();
                            String dataReceived = null;
                            if (receivedData != null) {
                                dataReceived = receivedData.getStringExtra("data_from_2_to_main");
                                Log.d(TAG, "Data received: " + dataReceived);
                                Toast.makeText(MainActivity.this, dataReceived, Toast.LENGTH_LONG).show();
                            }
                        } else if(o.getResultCode() == ResultCodes.RESULT_FROM_ACTIVITY_3) {
                            Intent receivedData = o.getData();
                            String dataReceived = null;
                            if (receivedData != null) {
                                dataReceived = receivedData.getStringExtra("data_from_3_to_main");
                                Log.d(TAG, "Data received: " + dataReceived);
                                Toast.makeText(MainActivity.this, dataReceived, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });



        goTo2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("data_from_main_to_2", "Hello from Main");
//                startActivity(intent);
                getContent.launch(intent);
            }
        });


        goTo3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                intent.putExtra("data_from_main_to_3", "Hello from Main");
//                startActivity(intent);
                getContent.launch(intent);
            }
        });

    }
}
