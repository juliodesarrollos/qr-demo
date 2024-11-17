package com.example.qrdemo;

import static android.os.Build.VERSION.SDK_INT;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import net.glxn.qrgen.android.QRCode;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private static final int ALTURA_CODIGO = 500, ANCHURA_CODIGO = 500;
    private EditText etTextoParaCodigo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etTextoParaCodigo = findViewById(R.id.etTextoParaCodigo);
        final ImageView imagenCodigo = findViewById(R.id.ivCodigoGenerado);
        Button btnGenerar = findViewById(R.id.btnGenerar),
                btnGuardar = findViewById(R.id.btnGuardar);
        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrar_teclado(v);
                String texto = obtenerTextoParaCodigo();
                if (texto.isEmpty()) return;
                Bitmap bitmap = QRCode.from(texto).withSize(ANCHURA_CODIGO, ALTURA_CODIGO).bitmap();
                imagenCodigo.setImageBitmap(bitmap);
            }
        });
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrar_teclado(v);
                String texto = obtenerTextoParaCodigo();
                if (texto.isEmpty()) return;
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        verificarYPedirPermisos();
                    } else {
                        // Crear stream del código QR
                        ByteArrayOutputStream byteArrayOutputStream = QRCode.from(texto).withSize(ANCHURA_CODIGO, ALTURA_CODIGO).stream();
                        // E intentar guardar
                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/codigo.png");
                            byteArrayOutputStream.writeTo(fos);
                            Toast.makeText(MainActivity.this, "Código guardado", Toast.LENGTH_SHORT).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void verificarYPedirPermisos() {
        if(SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Snackbar.make(findViewById(android.R.id.content), "Permission needed!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    startActivity(intent);
                                }
                            }
                        })
                        .show();
            }
        }
    }

    private void cerrar_teclado (View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private String obtenerTextoParaCodigo() {
        etTextoParaCodigo.setError(null);
        String posibleTexto = etTextoParaCodigo.getText().toString();
        if (posibleTexto.isEmpty()) {
            etTextoParaCodigo.setError("Escribe el texto del código QR");
            etTextoParaCodigo.requestFocus();
        }
        return posibleTexto;
    }

}