package cl.duoc.controlador;

import cl.duoc.dao.EstudianteDao;
import cl.duoc.model.Estudiante;
import cl.duoc.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class EstudianteControlador {
    private final Usuario usuario;
    private final EstudianteDao estudianteDao;

    public EstudianteControlador(Usuario usuario) {
        this(usuario, new EstudianteDao());
    }

    EstudianteControlador(Usuario usuario, EstudianteDao estudianteDao) {
        this.usuario = usuario;
        this.estudianteDao = estudianteDao;
    }

    public List<Estudiante> listar() throws SQLException {
        validarBibliotecario();
        return estudianteDao.listar();
    }

    public int crear(Estudiante estudiante, String contrasena) throws SQLException {
        validarBibliotecario();
        validarEstudiante(estudiante);
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contrasena de acceso es obligatoria.");
        }
        return estudianteDao.crear(estudiante, contrasena);
    }

    public boolean actualizar(Estudiante estudiante, String nuevaContrasena) throws SQLException {
        validarBibliotecario();
        validarEstudiante(estudiante);
        return estudianteDao.actualizar(estudiante, nuevaContrasena);
    }

    public boolean eliminar(int id) throws SQLException {
        validarBibliotecario();
        return estudianteDao.eliminar(id);
    }

    private void validarBibliotecario() {
        if (usuario == null || !usuario.puedeGestionarEstudiantes()) {
            throw new SecurityException("Solo el bibliotecario puede gestionar estudiantes.");
        }
    }

    private void validarEstudiante(Estudiante estudiante) {
        if (estudiante.getNombre() == null || estudiante.getNombre().isBlank()
                || estudiante.getRut() == null || estudiante.getRut().isBlank()
                || estudiante.getCurso() == null || estudiante.getCurso().isBlank()
                || estudiante.getCorreo() == null || estudiante.getCorreo().isBlank()) {
            throw new IllegalArgumentException("Completa nombre, RUT, curso y correo del estudiante.");
        }
        if (estudiante.getRut().length() > 12 || estudiante.getCurso().length() > 20
                || estudiante.getNombre().length() > 100 || estudiante.getCorreo().length() > 100) {
            throw new IllegalArgumentException("Uno o mas campos exceden el largo permitido.");
        }
    }
}