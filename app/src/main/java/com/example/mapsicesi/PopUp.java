package com.example.mapsicesi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.UUID;

public class PopUp extends AppCompatActivity {

    String coordenada = new String();
    String direccion = new String();
    double latitude;
    double longitude;
    String user = new String();
    Button agregar;
    TextView coord_tv;
    TextView dir_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        setContentView(R.layout.activity_pop_up);

        coordenada = extras.getString("coordenadas");
        direccion = extras.getString("direccion");
        latitude = extras.getDouble("latitud");
        longitude = extras.getDouble("longitud");
        user = extras.getString("user");

        DisplayMetrics medidasVentana = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(medidasVentana);

        coord_tv = findViewById(R.id.coor_tv);
        coord_tv.setText(coordenada);

        dir_tv = findViewById(R.id.dir_tv);
        dir_tv.setText(direccion);

        agregar = findViewById(R.id.addHole_btn);


        //Botton Agregar hueco

        agregar.setOnClickListener(
                (v) -> {
                    Pothole pothole = new Pothole(UUID.randomUUID().toString(), latitude, longitude);

                    Gson gson = new Gson();
                    String json = gson.toJson(pothole);
                    HTTPSWebUtilDomi https = new HTTPSWebUtilDomi();

                    new Thread(
                            () -> {
                                https.PUTrequest("https://reto1apps-542cb.firebaseio.com/potholes/"+pothole.getId()+".json", json);
                            }
                    ).start();
                }
        );





        int ancho = medidasVentana.widthPixels;
        int alto = medidasVentana.heightPixels;

        getWindow().setLayout((int) (ancho*0.85), (int) (alto*0.5));

    }
}