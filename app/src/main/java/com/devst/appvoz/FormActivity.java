package com.devst.appvoz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

// Formulario con validaciones. Envia datos a ConfirmActivity y espera una respuesta
public class FormActivity extends AppCompatActivity {

    private EditText etNombre, etCorreo;
    private TextView txtResultado;
    private ActivityResultLauncher<Intent> lanzadorConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        txtResultado = findViewById(R.id.txtResultado);
        Button enviar = findViewById(R.id.btnEnviarForm);
        Button volver = findViewById(R.id.btnVolverForm);

        // aqui llega la respuesta de ConfirmActivity
        lanzadorConfirm = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String respuesta = result.getData().getStringExtra("respuesta");
                        txtResultado.setText(respuesta != null ? respuesta : "Sin respuesta");
                    } else {
                        txtResultado.setText("Envio cancelado");
                    }
                });

        enviar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();

            // validaciones de los campos
            if (nombre.isEmpty()) {
                etNombre.setError("Escribe tu nombre");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.setError("Correo no valido");
                Toast.makeText(this, "Revisa el correo", Toast.LENGTH_SHORT).show();
                return;
            }

            // intent explicito con extras
            Intent i = new Intent(this, ConfirmActivity.class);
            i.putExtra("nombre", nombre);
            i.putExtra("correo", correo);
            lanzadorConfirm.launch(i);
        });

        volver.setOnClickListener(v -> finish());
    }
}