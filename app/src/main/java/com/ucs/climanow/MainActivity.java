package com.ucs.climanow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText Cidade;
    TextView cidadeAtual;
    TextView resultado;
    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private final String appid = "7e9025a1ea4781fe23b2b9b28faaf71d";

    private Location location;
    private LocationManager locationManager;

    private Address endereco;

    DecimalFormat df = new DecimalFormat("#.##");


    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        double latitude = 0.0;
        double longitude = 0.0;
        Cidade = findViewById(R.id.Cidade);
        cidadeAtual = findViewById(R.id.cidadeAtual);
        resultado = findViewById(R.id.resultado);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }else{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }


        if(location != null){

            longitude = location.getLongitude();
            latitude = location.getLatitude();

            Toast.makeText(getApplicationContext(),"Latitude e Longitude obtidos com sucesso!", Toast.LENGTH_SHORT).show();

        }

        try {
            endereco = buscarEndereco(latitude, longitude);
            cidadeAtual.setText("Você está em " + endereco.getLocality());
        } catch (IOException e) {
            Log.i("GPS",e.getMessage());
        }


    }

    public Address buscarEndereco(double latitude, double longitude) throws IOException{

        Geocoder geocoder;
        Address address = null;
        List<Address> addressList;

        geocoder = new Geocoder(getApplicationContext());

        addressList = geocoder.getFromLocation(latitude,longitude,1);

        if(addressList.size() > 0){
            address = addressList.get(0);
        }

        return address;
    }

    public void setAtual(View view){
        Cidade.setText(endereco.getLocality());
   }

    public void getDados(View view) {

        String tempUrl = "";
        String cidade = Cidade.getText().toString().trim();
        if (cidade.equals("")) {
            resultado.setText("Você deve escolher uma cidade");
        }else{
            tempUrl = url + "?q=" + cidade + "&appid=" +appid;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                   String output = "";
                   try {
                       JSONObject jsonResponse = new JSONObject(response);
                       JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                       JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                       String descricao = jsonObjectWeather.getString("description");
                       JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                       double temperatura = jsonObjectMain.getDouble("temp") -273.15;
                       double sensacaoTermica = jsonObjectMain.getDouble("feels_like" ) -273.15;
                       float pressao = jsonObjectMain.getInt("pressure");
                       int humidade = jsonObjectMain.getInt("humidity");
                       JSONObject jsonObjectVento = jsonResponse.getJSONObject("wind");
                       String vento = jsonObjectVento.getString("speed");
                       JSONObject jsonObjectNuvem = jsonResponse.getJSONObject("clouds");
                       String nuvens = jsonObjectNuvem.getString("all");
                       JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                       String nomePais = jsonObjectSys.getString("country");
                       String nomeCidade = jsonResponse.getString("name");
                       resultado.setTextColor(Color.rgb(250,240,230));
                       output += "Clima atual de " + nomeCidade + " (" + nomePais + ")"
                               + "\n Temperatura: " + df.format(temperatura) + " °C"
                               + "\n Sensação Térmica: " + df.format(sensacaoTermica) + " °C"
                               + "\n Humidade: " + humidade + "%"
                               + "\n Descrição: " + descricao
                               + "\n Vento: " + vento + "m/s (Metros por segundo)"
                               + "\n Nuvens: " + nuvens + "%"
                               + "\n Pressão Atmosférica: " + pressao + " hPa";
                       resultado.setText(output);


                   }catch (JSONException e){
                       e.printStackTrace();

                   }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }

    }
}