package com.AquaBalance.monitoring.infrastructure.persistence;

import com.AquaBalance.monitoring.domain.NivelContaminante;
import jakarta.persistence.*;

@Entity
@Table(name = "contaminantes")
public class ContaminanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    private Double carga;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelContaminante nivel;

    @Column(name = "fuente_origen")
    private String fuenteOrigen;

    public ContaminanteEntity() {}

    public ContaminanteEntity(Long id, String nombre, Double carga, NivelContaminante nivel, String fuenteOrigen) {
        this.id = id;
        this.nombre = nombre;
        this.carga = carga;
        this.nivel = nivel;
        this.fuenteOrigen = fuenteOrigen;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getCarga() { return carga; }
    public void setCarga(Double carga) { this.carga = carga; }

    public NivelContaminante getNivel() { return nivel; }
    public void setNivel(NivelContaminante nivel) { this.nivel = nivel; }

    public String getFuenteOrigen() { return fuenteOrigen; }
    public void setFuenteOrigen(String fuenteOrigen) { this.fuenteOrigen = fuenteOrigen; }
}