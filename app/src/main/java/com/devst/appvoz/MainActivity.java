package com.devst.appvoz;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // aqui voy guardando si la imagen se ve o no
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

        // android 13 cambio el nombre del permiso de galeria,
        // por se revisa la version
        String galeria = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        // apenas abro la app se pide microfono y galeria
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO, galeria}, 1);

        ImageView imageView = findViewById(R.id.imageView);
        Button animateButton = findViewById(R.id.animateButton);

        animateButton.setOnClickListener(v -> {
            // si no tiene el permiso de galeria, no permite ver la imagen
            if (ContextCompat.checkSelfPermission(this, galeria) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, galeria)) {
                    // si fue denegado una sola vez, todavia se puede volver a pedir desde aqui
                    Toast.makeText(this, "Necesito el permiso para mostrar la imagen", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{galeria}, 1);
                } else {
                    // si lo denegaron 2 veces ya no deja preguntar, asi que lo manda a configuracion para que lo active
                    Toast.makeText(this, "Activalo en Configuracion", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName())));
                }
                return;
            }

            // se muestra u oculta la imagen con los fade
            mostrando = !mostrando;
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(AnimationUtils.loadAnimation(this, mostrando ? R.anim.fade_in : R.anim.fade_out));
            animateButton.setText(mostrando ? "Ocultar imagen" : "Mostrar imagen");
        });

        //aqui deberia ir la parte del audio y video
    }

    // aqui veo que respondio el usuario con los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean todoOk = true;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) todoOk = false;
        }
        Toast.makeText(this, todoOk ? "Permisos listos" : "Falta algun permiso", Toast.LENGTH_SHORT).show();
    }
}