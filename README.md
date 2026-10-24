#  Proyecto Invernadero IoT - Sistema de Monitoreo Local


##  Problemática Resuelta y Justificación de Arquitectura
El proyecto resuelve la necesidad de monitorear y controlar un entorno agrícola (invernadero) operando bajo una arquitectura IoT Cliente-Servidor local[cite: 1]. Se justifica el uso de un Nodo Estación (Tablet) y un Nodo Sensor (Smartphone) para procesar telemetría en tiempo real sin depender de conectividad a internet, garantizando alta disponibilidad en zonas rurales. Se implementó `EncryptedSharedPreferences` para el manejo seguro de sesión y se cumple con la Ley de Protección de Datos Personales de Chile mediante solicitud de consentimiento explícito, alineándose a estándares de seguridad OT[cite: 1].


##  Especificación del Protocolo de Comunicación Inalámbrica
Se utiliza el estándar Bluetooth RFCOMM (Serial Port Profile) estableciendo la conexión mediante el UUID `00001101-0000-1000-8000-00805F9B34FB`[cite: 1]. El intercambio de datos en vivo es bidireccional[cite: 1]:
* **Emisión del Sensor:** Transmite telemetría en un formato de cadena separada por comas (`temperatura,humedad,luminosidad`).
* **Respuesta de la Estación:** Despacha comandos de texto plano (`VENT:ON`, `LUZ:OFF`) para accionar componentes físicos.
* **Robustez:** Las operaciones de red y sockets se ejecutan de forma asíncrona fuera del hilo principal utilizando Corrutinas (`Dispatchers.IO`) para asegurar la robustez de la aplicación[cite: 1].

##  Instrucciones de Compilación y Ejecución
1. Instalar el archivo `Tablet_Invernadero.apk` en el dispositivo designado como estación y `Telefono_Sensor.apk` en el dispositivo emisor[cite: 1].
2. Vincular ambos dispositivos físicos previamente desde los ajustes de Bluetooth de Android.
3. Ejecutar la aplicación en la estación, iniciar sesión, aceptar los términos de privacidad y permanecer en el panel de control.
4. Ejecutar la aplicación en el sensor, conceder los permisos de Dispositivos Cercanos solicitados en runtime[cite: 1], y presionar "Conectar a la Tablet".


<img width="738" height="1600" alt="WhatsApp Image 2026-10-23 at 22 27 31 (2)" src="https://github.com/user-attachments/assets/a87c505e-c630-4ca3-99ab-1a4a1c1d33be" />
<img width="738" height="1600" alt="WhatsApp Image 2026-10-23 at 22 27 31 (1)" src="https://github.com/user-attachments/assets/7f9134cc-1286-4be9-beff-2ca170e2695d" />
<img width="738" height="1600" alt="WhatsApp Image 2026-10-23 at 22 27 31" src="https://github.com/user-attachments/assets/aeb8f740-f215-4289-9f40-0d433f027c3c" />


<img width="768" height="1280" alt="WhatsApp Image 2026-10-23 at 22 28 35" src="https://github.com/user-attachments/assets/3cf81df5-d202-421c-8fae-e0e1127d7f60" />
<img width="768" height="1280" alt="WhatsApp Image 2026-10-23 at 22 28 34" src="https://github.com/user-attachments/assets/ebc7dced-24e2-4823-a492-138757548b90" />
<img width="768" height="1280" alt="WhatsApp Image 2026-10-23 at 22 28 35 (1)" src="https://github.com/user-attachments/assets/8131d549-fc65-4490-84b6-621adc459b82" />
