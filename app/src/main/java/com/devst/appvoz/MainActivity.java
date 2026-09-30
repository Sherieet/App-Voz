package com.devst.appvoz;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // llevo la cuenta de si la imagen se está mostrando o no
    private boolean mostrando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageView = findViewById(R.id.imageView);
        Button animateButton = findViewById(R.id.animateButton);

        // un mismo botón muestra u oculta la imagen con su animación
        animateButton.setOnClickListener(v -> {
            mostrando = !mostrando; // cambio el estado primero
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(AnimationUtils.loadAnimation(this, mostrando ? R.anim.fade_in : R.anim.fade_out));
            animateButton.setText(mostrando ? "Ocultar imagen" : "Mostrar imagen");
        });

        // ===== PARTE  de audio y video va aquí =====
    }
}