package cl.duoc.model;

/**
 * @author Sergio Salgado
 */
public class UsuarioEstudiante extends Usuario {
    public UsuarioEstudiante(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo, contrasena);
    }

    @Override
    public String getRol() {
        return "estudiante";
    }

    @Override
    public boolean puedeGestionarCatalogo() {
        return false;
    }

    @Override
    public boolean puedeGestionarEstudiantes() {
        return false;
    }

    @Override
    public boolean puedeGestionarUsuarios() {
        return false;
    }

    @Override
    public boolean puedeGestionarPrestamos() {
        return true;
    }

    @Override
    public boolean puedeGestionarPrestamosDeOtros() {
        return false;
    }

    @Override
    public boolean puedeConsultarReportes() {
        return true;
    }
}