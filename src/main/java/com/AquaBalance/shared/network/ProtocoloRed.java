package com.AquaBalance.network;

/** Protocolo de comunicación que usa la estación para enviar sus datos. */
public enum ProtocoloRed {
    HTTP,     // sensor con conexión a Internet (wifi / ethernet), puerto 443
    MQTT,     // telemetría ligera para IoT, puerto 1883
    LORAWAN   // red de largo alcance y bajo consumo, para zonas rurales (puerto 1700)
}