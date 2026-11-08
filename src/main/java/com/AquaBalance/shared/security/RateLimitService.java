package com.AquaBalance.shared.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de intentos en memoria (ventana deslizante).
 * Sirve para frenar fuerza bruta en el login y el abuso de los endpoints que envian correos.
 * Nota: al estar en memoria funciona por instancia; si algun dia hay varias instancias del backend,
 * habria que moverlo a Redis o a la base de datos.
 */
@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, Deque<Instant>> intentos = new ConcurrentHashMap<>();

    /** Registra un intento y devuelve true si todavia esta dentro del limite. */
    public boolean permitir(String clave, int maximo, Duration ventana) {
        Instant ahora = Instant.now();
        Deque<Instant> cola = intentos.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (cola) {
            while (!cola.isEmpty() && cola.peekFirst().isBefore(ahora.minus(ventana))) {
                cola.pollFirst();
            }
            if (cola.size() >= maximo) {
                return false;
            }
            cola.addLast(ahora);
            return true;
        }
    }

    /** Consulta sin registrar un intento nuevo. */
    public boolean bloqueado(String clave, int maximo, Duration ventana) {
        Deque<Instant> cola = intentos.get(clave);
        if (cola == null) return false;
        Instant limite = Instant.now().minus(ventana);
        synchronized (cola) {
            while (!cola.isEmpty() && cola.peekFirst().isBefore(limite)) {
                cola.pollFirst();
            }
            return cola.size() >= maximo;
        }
    }

    public void limpiar(String clave) {
        intentos.remove(clave);
    }
}
