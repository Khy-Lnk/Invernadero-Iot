package com.example.invernaderopi

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Lógica de Permisos en Runtime (Requisito CE 2.1.3)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permisosAprobados = permissions.entries.all { it.value }
        if (!permisosAprobados) {
            Toast.makeText(this, "Aviso: Sin permisos, la conexión local con el nodo estará limitada.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Disparar la solicitud de permisos al abrir la app
        solicitarPermisosBluetooth()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            guardarDatosCifrados(email)
                            mostrarPopUpLegal()
                        } else {
                            Toast.makeText(baseContext, "Error: Verifica tus credenciales", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor llena los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun solicitarPermisosBluetooth() {
        val permisos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        requestPermissionLauncher.launch(permisos)
    }

    // Lógica de Persistencia Cifrada (Requisito CE 2.1.3)
    private fun guardarDatosCifrados(email: String) {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            this,
            "datos_seguros",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit().putString("usuario_registrado", email).apply()
    }

    private fun mostrarPopUpLegal() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("📜 Términos y Privacidad")
        builder.setMessage("Para cumplir con la nueva Ley de Protección de Datos Personales, solicitamos tu autorización. ¿Aceptas que esta app procese tus datos de forma segura?")
        builder.setCancelable(false)

        builder.setPositiveButton("Acepto") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, InvernaderoActivity::class.java)
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton("Rechazo") { dialog, _ ->
            dialog.dismiss()
            auth.signOut()
            Toast.makeText(baseContext, "Debes aceptar para ingresar al panel", Toast.LENGTH_LONG).show()
        }

        builder.show()
    }
}