package com.example.invernaderopi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class InvernaderoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var serverSocket: BluetoothServerSocket? = null
    private var outputStream: OutputStream? = null

    // UUID estándar para conexión serial Bluetooth (RFCOMM)
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invernadero)

        auth = FirebaseAuth.getInstance()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val switchVentilador = findViewById<Switch>(R.id.switchVentilador)
        val switchLuz = findViewById<Switch>(R.id.switchLuz)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Iniciar el servidor Bluetooth usando Corrutinas (Fuera del hilo principal)
        iniciarServidorBluetooth()

        // Controles de Actuadores (Envían datos de vuelta al nodo sensor)
        switchVentilador.setOnClickListener {
            val estado = if (switchVentilador.isChecked) "VENT:ON" else "VENT:OFF"
            enviarComandoBluetooth(estado)
        }

        switchLuz.setOnClickListener {
            val estado = if (switchLuz.isChecked) "LUZ:ON" else "LUZ:OFF"
            enviarComandoBluetooth(estado)
        }

        btnLogout.setOnClickListener {
            cerrarSocketsSeguro()
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun iniciarServidorBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Aviso: Permisos Bluetooth limitados", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("EstacionInvernadero", MY_UUID)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InvernaderoActivity, "📡 Estación esperando conexión del sensor...", Toast.LENGTH_LONG).show()
                }

                var shouldLoop = true
                while (shouldLoop) {
                    val socket: BluetoothSocket? = try {
                        serverSocket?.accept()
                    } catch (e: Exception) {
                        shouldLoop = false
                        null
                    }

                    socket?.also {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@InvernaderoActivity, "✅ ¡Nodo Sensor Conectado!", Toast.LENGTH_SHORT).show()
                        }
                        leerDatosDelNodo(it)
                        serverSocket?.close()
                        shouldLoop = false
                    }
                }
            } catch (e: Exception) {
                Log.e("Bluetooth", "Error del servidor", e)
            }
        }
    }

    private suspend fun leerDatosDelNodo(socket: BluetoothSocket) {
        val inputStream: InputStream = socket.inputStream
        outputStream = socket.outputStream // Guardamos el flujo para enviar comandos
        val buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {
                bytes = inputStream.read(buffer)
                val mensajeRecibido = String(buffer, 0, bytes).trim()

                // Protocolo de llegada: "temperatura,humedad,luminosidad"
                val datos = mensajeRecibido.split(",")
                if (datos.size >= 3) {
                    withContext(Dispatchers.Main) {
                        actualizarInterfaz(datos[0], datos[1], datos[2])
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InvernaderoActivity, "❌ Conexión perdida con el sensor", Toast.LENGTH_SHORT).show()
                }
                break
            }
        }
    }

    private fun enviarComandoBluetooth(comando: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                outputStream?.write(comando.toByteArray())
            } catch (e: Exception) {
                Log.e("Bluetooth", "Error enviando comando", e)
            }
        }
    }

    private fun actualizarInterfaz(temp: String, hum: String, lum: String) {
        val tvTemperatura = findViewById<TextView>(R.id.tvTemperatura)
        val tvHumedad = findViewById<TextView>(R.id.tvHumedad)
        val tvLuminosidad = findViewById<TextView>(R.id.tvLuminosidad)
        val tvAlerta = findViewById<TextView>(R.id.tvAlerta)

        tvTemperatura.text = "🌡️ Temperatura: $temp °C"
        tvHumedad.text = "💧 Humedad: $hum %"
        tvLuminosidad.text = "☀️ Luminosidad: $lum %"

        val tempValue = temp.toDoubleOrNull() ?: 0.0
        if (tempValue >= 30.0) {
            if (tvAlerta.visibility == View.GONE) {
                tvAlerta.visibility = View.VISIBLE
                val anim = AlphaAnimation(0.2f, 1.0f).apply {
                    duration = 400
                    repeatMode = Animation.REVERSE
                    repeatCount = Animation.INFINITE
                }
                tvAlerta.startAnimation(anim)
            }
        } else {
            tvAlerta.visibility = View.GONE
            tvAlerta.clearAnimation()
        }
    }

    private fun cerrarSocketsSeguro() {
        try {
            serverSocket?.close()
            outputStream?.close()
        } catch (e: Exception) {
            Log.e("Bluetooth", "Error al cerrar", e)
        }
    }
}