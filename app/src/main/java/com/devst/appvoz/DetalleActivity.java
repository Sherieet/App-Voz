package com.devst.appvoz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Recibe datos (uri de imagen y un texto) mediante un intent explicito
public class DetalleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        ImageView imagen = findViewById(R.id.imgDetalle);
        TextView texto = findViewById(R.id.txtDetalle);
        Button volver = findViewById(R.id.btnVolverDetalle);
        volver.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        Uri uri = intent.getData();                       // la imagen viene en el data del intent
        String origen = intent.getStringExtra("origen");  // extra de texto

        // validacion: si no llegaron datos no se rompe la app
        if (uri == null) {
            Toast.makeText(this, "No llego ninguna imagen", Toast.LENGTH_SHORT).show();
            texto.setText("Sin imagen para mostrar");
            return;
        }

        try {
            imagen.setImageURI(uri);
            texto.setText("Origen: " + (origen != null ? origen : "desconocido"));
        } catch (SecurityException e) {
            // pasa si el sistema ya no deja leer esa uri
            Toast.makeText(this, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}