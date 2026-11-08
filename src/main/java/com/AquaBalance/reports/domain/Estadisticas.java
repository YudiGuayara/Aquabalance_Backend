package com.AquaBalance.reports.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Estadisticas {

    private Long totalMediciones;
    private Double promedioPh;
    private Double promedioTemperatura;
    private Double phMinimo;
    private Double phMaximo;
    private Double temperaturaMinima;
    private Double temperaturaMaxima;
    private Map<String, Long> medicionesPorContaminante;
    private List<PuntoTemporal> evolucionPh;
    private List<PuntoTemporal> evolucionTemperatura;
    private Long totalAlertas;
    private Map<String, Long> alertasPorNivel;
    private List<AlertaResumen> alertas;
    private Long totalEventos;
    private Map<String, Long> eventosPorMagnitud;
    private List<EventoResumen> eventos;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // ── Clases internas ──────────────────────────────────────

    public static class PuntoTemporal {
        private final String fecha;
        private final Double valor;
        public PuntoTemporal(String fecha, Double valor) {
            this.fecha = fecha;
            this.valor = valor;
        }
        public String getFecha() { return fecha; }
        public Double getValor() { return valor; }
    }

    public static class AlertaResumen {
        private final Long id;
        private final String nivel;
        private final String mensaje;
        private final LocalDateTime fecha;
        public AlertaResumen(Long id, String nivel, String mensaje, LocalDateTime fecha) {
            this.id = id; this.nivel = nivel;
            this.mensaje = mensaje; this.fecha = fecha;
        }
        public Long getId() { return id; }
        public String getNivel() { return nivel; }
        public String getMensaje() { return mensaje; }
        public LocalDateTime getFecha() { return fecha; }
    }

    public static class EventoResumen {
        private final Long id;
        private final String descripcion;
        private final String magnitud;
        private final LocalDateTime fecha;
        public EventoResumen(Long id, String descripcion, String magnitud, LocalDateTime fecha) {
            this.id = id; this.descripcion = descripcion;
            this.magnitud = magnitud; this.fecha = fecha;
        }
        public Long getId() { return id; }
        public String getDescripcion() { return descripcion; }
        public String getMagnitud() { return magnitud; }
        public LocalDateTime getFecha() { return fecha; }
    }

    // ── Getters y Setters ────────────────────────────────────

    public Long getTotalMediciones() { return totalMediciones; }
    public void setTotalMediciones(Long v) { this.totalMediciones = v; }
    public Double getPromedioPh() { return promedioPh; }
    public void setPromedioPh(Double v) { this.promedioPh = v; }
    public Double getPromedioTemperatura() { return promedioTemperatura; }
    public void setPromedioTemperatura(Double v) { this.promedioTemperatura = v; }
    public Double getPhMinimo() { return phMinimo; }
    public void setPhMinimo(Double v) { this.phMinimo = v; }
    public Double getPhMaximo() { return phMaximo; }
    public void setPhMaximo(Double v) { this.phMaximo = v; }
    public Double getTemperaturaMinima() { return temperaturaMinima; }
    public void setTemperaturaMinima(Double v) { this.temperaturaMinima = v; }
    public Double getTemperaturaMaxima() { return temperaturaMaxima; }
    public void setTemperaturaMaxima(Double v) { this.temperaturaMaxima = v; }
    public Map<String, Long> getMedicionesPorContaminante() { return medicionesPorContaminante; }
    public void setMedicionesPorContaminante(Map<String, Long> v) { this.medicionesPorContaminante = v; }
    public List<PuntoTemporal> getEvolucionPh() { return evolucionPh; }
    public void setEvolucionPh(List<PuntoTemporal> v) { this.evolucionPh = v; }
    public List<PuntoTemporal> getEvolucionTemperatura() { return evolucionTemperatura; }
    public void setEvolucionTemperatura(List<PuntoTemporal> v) { this.evolucionTemperatura = v; }
    public Long getTotalAlertas() { return totalAlertas; }
    public void setTotalAlertas(Long v) { this.totalAlertas = v; }
    public Map<String, Long> getAlertasPorNivel() { return alertasPorNivel; }
    public void setAlertasPorNivel(Map<String, Long> v) { this.alertasPorNivel = v; }
    public List<AlertaResumen> getAlertas() { return alertas; }
    public void setAlertas(List<AlertaResumen> v) { this.alertas = v; }
    public Long getTotalEventos() { return totalEventos; }
    public void setTotalEventos(Long v) { this.totalEventos = v; }
    public Map<String, Long> getEventosPorMagnitud() { return eventosPorMagnitud; }
    public void setEventosPorMagnitud(Map<String, Long> v) { this.eventosPorMagnitud = v; }
    public List<EventoResumen> getEventos() { return eventos; }
    public void setEventos(List<EventoResumen> v) { this.eventos = v; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime v) { this.fechaInicio = v; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime v) { this.fechaFin = v; }
}