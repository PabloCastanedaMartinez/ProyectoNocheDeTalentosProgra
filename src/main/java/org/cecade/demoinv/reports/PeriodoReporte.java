package org.cecade.demoinv.reports;

public enum PeriodoReporte {
    DIARIO("Diario"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual"),
    TRIMESTRAL("Trimestral"),
    SEMESTRAL("Semestral"),
    ANUAL("Anual");

    private final String descripcion;

    PeriodoReporte(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

