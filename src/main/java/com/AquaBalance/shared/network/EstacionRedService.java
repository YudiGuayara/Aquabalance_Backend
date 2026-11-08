package com.AquaBalance.network;

import com.AquaBalance.monitoring.infrastructure.persistence.JpaRecursoRepository;
import com.AquaBalance.monitoring.infrastructure.persistence.RecursoEntity;
import com.AquaBalance.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EstacionRedService {

    // Si una estación no transmite en este tiempo se considera fuera de línea
    static final long SEGUNDOS_FUERA_DE_LINEA = 45;
    // Umbrales para marcar la conexión como degradada
    static final int LATENCIA_DEGRADADA_MS = 800;
    static final int SENAL_DEGRADADA_DBM = -100;
    static final int BATERIA_BAJA_PCT = 15;

    private final EstacionRedRepository estacionRepo;
    private final JpaRecursoRepository recursoRepo;

    public EstacionRedService(EstacionRedRepository estacionRepo,
                              JpaRecursoRepository recursoRepo) {
        this.estacionRepo = estacionRepo;
        this.recursoRepo = recursoRepo;

    }

    @Transactional
    public List<EstacionRedResponse> listar() {
        sincronizarConRecursos();
        LocalDateTime ahora = LocalDateTime.now();
        return estacionRepo.findAll().stream()
                .sorted(Comparator.comparing(EstacionRedEntity::getRecursoId))
                .map(e -> aRespuesta(e, ahora))
                .toList();
    }

    @Transactional
    public EstacionRedResponse registrarTelemetria(Long id, TelemetriaRequest datos) {
        EstacionRedEntity estacion = buscar(id);

        if (datos.latenciaMs() != null) estacion.setLatenciaMs(datos.latenciaMs());
        if (datos.senalDbm() != null) estacion.setSenalDbm(datos.senalDbm());
        if (datos.bateriaPct() != null) estacion.setBateriaPct(datos.bateriaPct());

        estacion.setUltimaComunicacion(LocalDateTime.now());
        estacion.setPaquetesRecibidos(estacion.getPaquetesRecibidos() + 1);

        return aRespuesta(estacionRepo.save(estacion), LocalDateTime.now());
    }

    @Transactional
    public EstacionRedResponse registrarPerdida(Long id) {
        EstacionRedEntity estacion = buscar(id);
        estacion.setPaquetesPerdidos(estacion.getPaquetesPerdidos() + 1);
        return aRespuesta(estacionRepo.save(estacion), LocalDateTime.now());
    }


    /** Estado de la conexión según la última comunicación y la calidad del enlace. */
    static EstadoEstacion calcularEstado(LocalDateTime ultimaComunicacion,
                                         Integer latenciaMs,
                                         Integer senalDbm,
                                         Integer bateriaPct,
                                         LocalDateTime ahora) {
        if (ultimaComunicacion == null) {
            return EstadoEstacion.SIN_DATOS;
        }

        long segundos = Duration.between(ultimaComunicacion, ahora).getSeconds();
        if (segundos > SEGUNDOS_FUERA_DE_LINEA) {
            return EstadoEstacion.FUERA_DE_LINEA;
        }

        boolean latenciaAlta = latenciaMs != null && latenciaMs > LATENCIA_DEGRADADA_MS;
        boolean senalDebil = senalDbm != null && senalDbm < SENAL_DEGRADADA_DBM;
        boolean bateriaBaja = bateriaPct != null && bateriaPct < BATERIA_BAJA_PCT;

        return (latenciaAlta || senalDebil || bateriaBaja)
                ? EstadoEstacion.DEGRADADA
                : EstadoEstacion.EN_LINEA;
    }

    private EstacionRedEntity buscar(Long id) {
        return estacionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estación no encontrada: " + id));
    }

    /** Crea una estación (simulada) por cada recurso hídrico que aún no tenga una. */
    private void sincronizarConRecursos() {

        Set<Long> conEstacion = new HashSet<>();
        estacionRepo.findAll().forEach(e -> conEstacion.add(e.getRecursoId()));

        for (RecursoEntity recurso : recursoRepo.findAll()) {
            if (conEstacion.contains(recurso.getId())) continue;

            ProtocoloRed protocolo = protocoloPorTipo(recurso);

            EstacionRedEntity nueva = new EstacionRedEntity();
            nueva.setRecursoId(recurso.getId());
            nueva.setNombre("Estación " + recurso.getNombre());
            nueva.setTipoRecurso(recurso.getTipo().name());
            nueva.setIp(String.format("10.20.%d.10", recurso.getId() % 250)); // IP privada simulada
            nueva.setProtocolo(protocolo);
            nueva.setPuerto(puertoPorProtocolo(protocolo));
            estacionRepo.save(nueva);
        }
    }

    private ProtocoloRed protocoloPorTipo(RecursoEntity recurso) {
        return switch (recurso.getTipo()) {
            case Rio, Lago -> ProtocoloRed.LORAWAN;   // zonas remotas
            case Embalse, Acuifero -> ProtocoloRed.MQTT;
            case Planta -> ProtocoloRed.HTTP;         // instalación con red cableada
        };
    }

    private int puertoPorProtocolo(ProtocoloRed protocolo) {
        return switch (protocolo) {
            case HTTP -> 443;
            case MQTT -> 1883;
            case LORAWAN -> 1700;

        };
    }

    private EstacionRedResponse aRespuesta(EstacionRedEntity e, LocalDateTime ahora) {
        Long segundos = e.getUltimaComunicacion() == null
                ? null
                : Duration.between(e.getUltimaComunicacion(), ahora).getSeconds();

        long total = e.getPaquetesRecibidos() + e.getPaquetesPerdidos();
        double perdida = total == 0
                ? 0.0
                : Math.round(e.getPaquetesPerdidos() * 1000.0 / total) / 10.0;

        return new EstacionRedResponse(
                e.getId(),
                e.getRecursoId(),
                e.getNombre(),
                e.getTipoRecurso(),
                e.getIp(),
                e.getProtocolo(),
                e.getPuerto(),
                calcularEstado(e.getUltimaComunicacion(), e.getLatenciaMs(),
                        e.getSenalDbm(), e.getBateriaPct(), ahora),
                e.getLatenciaMs(),
                e.getSenalDbm(),
                e.getBateriaPct(),
                segundos,
                e.getPaquetesRecibidos(),
                e.getPaquetesPerdidos(),
                perdida
        );
    }

}