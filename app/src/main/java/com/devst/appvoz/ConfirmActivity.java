package com.devst.appvoz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Recibe los datos del formulario y devuelve un resultado (OK o cancelado)
public class ConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        TextView txt = findViewById(R.id.txtConfirm);
        Button confirmar = findViewById(R.id.btnConfirmar);
        Button cancelar = findViewById(R.id.btnCancelar);

        String nombre = getIntent().getStringExtra("nombre");
        String correo = getIntent().getStringExtra("correo");

        // validacion: si faltan datos se cancela y se vuelve
        if (nombre == null || correo == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        txt.setText("¿Confirmas los datos?\n\nNombre: " + nombre + "\nCorreo: " + correo);

        confirmar.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("respuesta", "Confirmado: " + nombre + " (" + correo + ")");
            setResult(RESULT_OK, data);   // devuelve el resultado a FormActivity
            finish();
        });

        cancelar.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}