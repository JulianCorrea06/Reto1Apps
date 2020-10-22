package com.example.mapsicesi;

import com.google.gson.Gson;

import java.util.Map;

public class LocationWorker extends Thread {


    private MapsActivity ref;
    private boolean isAlive;


    public LocationWorker(MapsActivity ref){
        this.ref = ref;

    }

    public void run(){
        HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();
        Gson gson = new Gson();

        while(isAlive){

            delay(10000);
            if(ref.getCurrentPosition() != null){

                utilDomi.PUTrequest("https://reto1apps-542cb.firebaseio.com/Usuarios"+ref.getUser()+"/location.json", gson.toJson(ref.getCurrentPosition()));
            }

        }
    }

    public void delay(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void finish() {
    }
}
