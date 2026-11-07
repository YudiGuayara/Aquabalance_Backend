package com.AquaBalance.network;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Estación de comunicaciones asociada a un recurso hídrico (sensor + gateway). */
@Entity
@Table(name = "estaciones_red")
public class EstacionRedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurso_id", nullable = false, unique = true)
    private Long recursoId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo_recurso", length = 30)
    private String tipoRecurso;

    @Column(nullable = false, length = 45)
    private String ip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProtocoloRed protocolo;

    @Column(nullable = false)

    private Integer puerto;

    private Integer latenciaMs;
    private Integer senalDbm;
    private Integer bateriaPct;

    @Column(name = "ultima_comunicacion")
    private LocalDateTime ultimaComunicacion;

    @Column(name = "paquetes_recibidos", nullable = false)
    private Long paquetesRecibidos = 0L;

    @Column(name = "paquetes_perdidos", nullable = false)
    private Long paquetesPerdidos = 0L;

    public EstacionRedEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecursoId() { return recursoId; }
    public void setRecursoId(Long recursoId) { this.recursoId = recursoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(String tipoRecurso) { this.tipoRecurso = tipoRecurso; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }


    public ProtocoloRed getProtocolo() { return protocolo; }
    public void setProtocolo(ProtocoloRed protocolo) { this.protocolo = protocolo; }

    public Integer getPuerto() { return puerto; }
    public void setPuerto(Integer puerto) { this.puerto = puerto; }

    public Integer getLatenciaMs() { return latenciaMs; }
    public void setLatenciaMs(Integer latenciaMs) { this.latenciaMs = latenciaMs; }

    public Integer getSenalDbm() { return senalDbm; }
    public void setSenalDbm(Integer senalDbm) { this.senalDbm = senalDbm; }

    public Integer getBateriaPct() { return bateriaPct; }
    public void setBateriaPct(Integer bateriaPct) { this.bateriaPct = bateriaPct; }

    public LocalDateTime getUltimaComunicacion() { return ultimaComunicacion; }
    public void setUltimaComunicacion(LocalDateTime ultimaComunicacion) { this.ultimaComunicacion = ultimaComunicacion; }

    public Long getPaquetesRecibidos() { return paquetesRecibidos; }
    public void setPaquetesRecibidos(Long paquetesRecibidos) { this.paquetesRecibidos = paquetesRecibidos; }

    public Long getPaquetesPerdidos() { return paquetesPerdidos; }
    public void setPaquetesPerdidos(Long paquetesPerdidos) { this.paquetesPerdidos = paquetesPerdidos; }
}