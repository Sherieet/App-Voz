package com.devst.appvoz;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Pantalla de ayuda: se abre con un intent explicito desde MainActivity
public class AyudaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        // boton para volver a la pantalla anterior
        Button volver = findViewById(R.id.btnVolverAyuda);
        volver.setOnClickListener(v -> finish());
    }
}