package cl.duoc.model;

import java.time.LocalDate;

/**
 * @author Sergio Salgado
 */
public class PrestamoResumen {
    private final int id;
    private final String estudiante;
    private final String libro;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaLimite;
    private final boolean devuelto;

    public PrestamoResumen(int id, String estudiante, String libro, LocalDate fechaPrestamo,
                           LocalDate fechaLimite, boolean devuelto) {
        this.id = id;
        this.estudiante = estudiante;
        this.libro = libro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.devuelto = devuelto;
    }

    public int getId() {
        return id;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public String getLibro() {
        return libro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public boolean isAtrasado() {
        return !devuelto && fechaLimite.isBefore(LocalDate.now());
    }
}