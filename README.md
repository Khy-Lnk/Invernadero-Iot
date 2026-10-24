# 🌿 Proyecto Invernadero IoT - Sistema de Monitoreo Local

<div align="center">
  <img width="250" src="https://github.com/user-attachments/assets/4ea48d92-9b3c-41ad-9c1f-a1f0fed476c2" />
  <img width="250" src="https://github.com/user-attachments/assets/a241f7fc-0ecd-45ce-a488-abfccb58d5ed" />
  <img width="250" src="https://github.com/user-attachments/assets/67548649-36e2-44ed-9036-23fc33cf4b58" />
</div>

## 📌 Problemática Resuelta y Justificación de Arquitectura
El proyecto resuelve la necesidad de monitorear y controlar un entorno agrícola (invernadero) operando bajo una arquitectura IoT Cliente-Servidor local[cite: 1]. Se justifica el uso de un Nodo Estación (Tablet) y un Nodo Sensor (Smartphone) para procesar telemetría en tiempo real sin depender de conectividad a internet, garantizando alta disponibilidad en zonas rurales. Se implementó `EncryptedSharedPreferences` para el manejo seguro de sesión y se cumple con la Ley de Protección de Datos Personales de Chile mediante solicitud de consentimiento explícito, alineándose a estándares de seguridad OT[cite: 1].

<div align="center">
  <img width="230" src="https://github.com/user-attachments/assets/3304b004-0c73-4e76-8da3-8b5dbaa1986f" />
  <img width="230" src="https://github.com/user-attachments/assets/ff9b2b50-5e78-4e5f-8d44-dfcce62baf08" />
  <img width="230" src="https://github.com/user-attachments/assets/e853cf26-6bcd-43eb-adc8-2c27fb0e24e1" />
</div>

## 📡 Especificación del Protocolo de Comunicación Inalámbrica
Se utiliza el estándar Bluetooth RFCOMM (Serial Port Profile) estableciendo la conexión mediante el UUID `00001101-0000-1000-8000-00805F9B34FB`[cite: 1]. El intercambio de datos en vivo es bidireccional[cite: 1]:
* **Emisión del Sensor:** Transmite telemetría en un formato de cadena separada por comas (`temperatura,humedad,luminosidad`).
* **Respuesta de la Estación:** Despacha comandos de texto plano (`VENT:ON`, `LUZ:OFF`) para accionar componentes físicos.
* **Robustez:** Las operaciones de red y sockets se ejecutan de forma asíncrona fuera del hilo principal utilizando Corrutinas (`Dispatchers.IO`) para asegurar la robustez de la aplicación[cite: 1].

## 🚀 Instrucciones de Compilación y Ejecución
1. Instalar el archivo `Tablet_Invernadero.apk` en el dispositivo designado como estación y `Telefono_Sensor.apk` en el dispositivo emisor[cite: 1].
2. Vincular ambos dispositivos físicos previamente desde los ajustes de Bluetooth de Android.
3. Ejecutar la aplicación en la estación, iniciar sesión, aceptar los términos de privacidad y permanecer en el panel de control.
4. Ejecutar la aplicación en el sensor, conceder los permisos de Dispositivos Cercanos solicitados en runtime[cite: 1], y presionar "Conectar a la Tablet".





