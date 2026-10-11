package cl.duoc.model;

/**
 * @author Sergio Salgado
 */
public interface PermisosBiblioteca {
    boolean puedeGestionarCatalogo();

    boolean puedeGestionarEstudiantes();

    boolean puedeGestionarUsuarios();

    boolean puedeGestionarPrestamos();

    boolean puedeGestionarPrestamosDeOtros();

    boolean puedeConsultarReportes();
}