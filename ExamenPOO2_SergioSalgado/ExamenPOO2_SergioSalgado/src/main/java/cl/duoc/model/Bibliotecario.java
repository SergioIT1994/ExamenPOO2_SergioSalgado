package cl.duoc.model;

/**
 * @author Sergio Salgado
 */
public class Bibliotecario extends Usuario {
    public Bibliotecario(int id, String nombre, String rut, String correo, String contrasena) {
        super(id, nombre, rut, correo, contrasena);
    }

    @Override
    public String getRol() {
        return "bibliotecario";
    }

    @Override
    public boolean puedeGestionarCatalogo() {
        return true;
    }

    @Override
    public boolean puedeGestionarEstudiantes() {
        return true;
    }

    @Override
    public boolean puedeGestionarUsuarios() {
        return true;
    }

    @Override
    public boolean puedeGestionarPrestamos() {
        return true;
    }

    @Override
    public boolean puedeGestionarPrestamosDeOtros() {
        return true;
    }

    @Override
    public boolean puedeConsultarReportes() {
        return true;
    }
}