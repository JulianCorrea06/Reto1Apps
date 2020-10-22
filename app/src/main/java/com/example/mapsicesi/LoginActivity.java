package com.example.mapsicesi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText edt_username;
    private Button btn_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edt_username = findViewById(R.id.edt_username);
        btn_login = findViewById(R.id.btn_login);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 1
        );


        btn_login.setOnClickListener(
            (v) -> {
                String user = edt_username.getText().toString();
                Intent i = new Intent(this,MapsActivity.class );
                i.putExtra("user", user);
                startActivity(i);
            }
        );
    }
}