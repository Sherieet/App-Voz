package com.devst.appvoz;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    // aqui voy guardando si la imagen se ve o no
    private boolean mostrando = false;

    private ImageView imageView;

    // uri de la foto que se va a tomar con la camara
    private Uri uriFoto;
    // uri de la imagen que se ve en pantalla (camara o galeria)
    private Uri uriImagenActual;
    private String origenImagen = "";

    // launchers para recibir resultados
    private ActivityResultLauncher<Intent> lanzadorCamara;
    private ActivityResultLauncher<Intent> lanzadorGaleria;
    private ActivityResultLauncher<String> lanzadorPermisoCamara;

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
        // por eso se revisa la version
        String galeria = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        imageView = findViewById(R.id.imageView);
        Button animateButton = findViewById(R.id.animateButton);

        registrarLaunchers();

        // ---------- boton mostrar / ocultar imagen (ya existia) ----------
        animateButton.setOnClickListener(v -> {
            // si no tiene el permiso de galeria, no permite ver la imagen
            if (ContextCompat.checkSelfPermission(this, galeria) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, galeria)) {
                    // denegado una vez: todavia se puede volver a pedir
                    Toast.makeText(this, "Necesito el permiso para mostrar la imagen", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{galeria}, 1);
                } else {
                    // primera vez o denegado 2 veces: se pide / se manda a configuracion
                    if (primeraVezPermiso(galeria)) {
                        ActivityCompat.requestPermissions(this, new String[]{galeria}, 1);
                    } else {
                        Toast.makeText(this, "Activalo en Configuracion", Toast.LENGTH_LONG).show();
                        lanzarImplicito(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getPackageName())));
                    }
                }
                return;
            }

            // se muestra u oculta la imagen con los fade
            mostrando = !mostrando;
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(AnimationUtils.loadAnimation(this, mostrando ? R.anim.fade_in : R.anim.fade_out));
            animateButton.setText(mostrando ? "Ocultar imagen" : "Mostrar imagen");
        });

        // ================= INTENTS IMPLICITOS =================

        // 1) Camara: MediaStore.ACTION_IMAGE_CAPTURE
        findViewById(R.id.btnCamara).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                // pide el permiso y cuando responda se abre la camara
                lanzadorPermisoCamara.launch(Manifest.permission.CAMERA);
            }
        });

        // 2) Galeria: ACTION_GET_CONTENT con image/*
        findViewById(R.id.btnGaleria).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            try {
                lanzadorGaleria.launch(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No hay app de galeria", Toast.LENGTH_SHORT).show();
            }
        });

        // 3) Pagina web: ACTION_VIEW con https://
        findViewById(R.id.btnWeb).setOnClickListener(v ->
                lanzarImplicito(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.santotomas.cl"))));

        // 4) Correo: ACTION_SENDTO con mailto: (asunto y cuerpo prellenados)
        findViewById(R.id.btnCorreo).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SENDTO);
            i.setData(Uri.parse("mailto:"));   // solo apps de correo
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"docente@correo.cl"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Prototipo 2 - AppVoz");
            i.putExtra(Intent.EXTRA_TEXT, "Hola, les comparto el avance de mi app.");
            lanzarImplicito(i);
        });

        // 5) Configuracion Wi-Fi: Settings.ACTION_WIFI_SETTINGS
        findViewById(R.id.btnWifi).setOnClickListener(v ->
                lanzarImplicito(new Intent(Settings.ACTION_WIFI_SETTINGS)));

        // ================= INTENTS EXPLICITOS =================

        // 1) MainActivity -> DetalleActivity (con extras)
        findViewById(R.id.btnDetalle).setOnClickListener(v -> {
            // validacion: tiene que haber una imagen elegida o tomada
            if (uriImagenActual == null) {
                Toast.makeText(this, "Primero toma o elige una foto", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, DetalleActivity.class);
            i.setData(uriImagenActual);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.putExtra("origen", origenImagen);
            startActivity(i);
        });

        // 2) MainActivity -> FormActivity (que luego va a ConfirmActivity con resultado)
        findViewById(R.id.btnForm).setOnClickListener(v ->
                startActivity(new Intent(this, FormActivity.class)));

        // 3) MainActivity -> AyudaActivity
        findViewById(R.id.btnAyuda).setOnClickListener(v ->
                startActivity(new Intent(this, AyudaActivity.class)));
    }

    // registra los launchers que reciben el resultado de camara, galeria y permisos
    private void registrarLaunchers() {

        // resultado del permiso de camara
        lanzadorPermisoCamara = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), concedido -> {
                    if (concedido) {
                        abrirCamara();
                    } else {
                        Toast.makeText(this, "Sin permiso de camara no se puede tomar la foto",
                                Toast.LENGTH_LONG).show();
                    }
                });

        // resultado de la camara
        lanzadorCamara = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && uriFoto != null) {
                        origenImagen = "Camara";
                        cargarImagen(uriFoto);
                    } else {
                        // el usuario cancelo: se borra el registro vacio de la galeria
                        if (uriFoto != null) getContentResolver().delete(uriFoto, null, null);
                        Toast.makeText(this, "Foto cancelada", Toast.LENGTH_SHORT).show();
                    }
                });

        // resultado de la galeria
        lanzadorGaleria = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null
                            && result.getData().getData() != null) {
                        origenImagen = "Galeria";
                        cargarImagen(result.getData().getData());
                    } else {
                        Toast.makeText(this, "No se eligio ninguna imagen", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // prepara un archivo en la galeria y abre la camara para guardar ahi la foto
    private void abrirCamara() {
        ContentValues valores = new ContentValues();
        valores.put(MediaStore.Images.Media.DISPLAY_NAME, "AppVoz_" + System.currentTimeMillis() + ".jpg");
        valores.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= 29) {
            valores.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AppVoz");
        }

        try {
            uriFoto = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores);
        } catch (Exception e) {
            uriFoto = null;
        }

        // validacion: si no se pudo crear el archivo, no se abre la camara
        if (uriFoto == null) {
            Toast.makeText(this, "No se pudo preparar la foto", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto);
        try {
            lanzadorCamara.launch(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay app de camara", Toast.LENGTH_SHORT).show();
        }
    }

    // carga la imagen en un hilo aparte para no congelar la pantalla
    private void cargarImagen(Uri uri) {
        new Thread(() -> {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                BitmapFactory.Options opciones = new BitmapFactory.Options();
                opciones.inSampleSize = 4;   // reduce el tamano para no gastar memoria
                Bitmap bmp = BitmapFactory.decodeStream(in, null, opciones);

                // volver al hilo principal para tocar la interfaz
                runOnUiThread(() -> {
                    if (bmp == null) {
                        Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show();
                    } else {
                        imageView.setImageBitmap(bmp);
                        imageView.setVisibility(View.VISIBLE);
                        uriImagenActual = uri;
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // lanza un intent implicito y avisa si ninguna app puede atenderlo
    private void lanzarImplicito(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay una app para esta accion", Toast.LENGTH_SHORT).show();
        }
    }

    // true si es la primera vez que se pide el permiso (todavia no se ha rechazado nunca)
    private boolean primeraVezPermiso(String permiso) {
        return !getSharedPreferences("permisos", MODE_PRIVATE).getBoolean(permiso, false);
    }

    // aqui veo que respondio el usuario con los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // se anota que ya se pidio, para distinguir "primera vez" de "denegado para siempre"
        for (String p : permissions) {
            getSharedPreferences("permisos", MODE_PRIVATE).edit().putBoolean(p, true).apply();
        }

        boolean todoOk = grantResults.length > 0;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) todoOk = false;
        }
        Toast.makeText(this, todoOk ? "Permisos listos" : "Falta algun permiso", Toast.LENGTH_SHORT).show();
    }
}