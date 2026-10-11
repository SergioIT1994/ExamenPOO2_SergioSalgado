package cl.duoc.controlador;

import cl.duoc.dao.EstudianteDao;
import cl.duoc.dao.PrestamoDao;
import cl.duoc.model.Prestamo;
import cl.duoc.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class PrestamoControlador {
    private final Usuario usuario;
    private final EstudianteDao estudianteDao;
    private final PrestamoDao prestamoDao;

    public PrestamoControlador(Usuario usuario) {
        this(usuario, new EstudianteDao(), new PrestamoDao());
    }

    PrestamoControlador(Usuario usuario, EstudianteDao estudianteDao, PrestamoDao prestamoDao) {
        this.usuario = usuario;
        this.estudianteDao = estudianteDao;
        this.prestamoDao = prestamoDao;
    }

    public int registrar(int idEstudiante, int idLibro) throws SQLException {
        validarUsuario();
        validarRolPermitido();
        int estudianteAutorizado = usuario.puedeGestionarPrestamosDeOtros()
            ? idEstudiante
            : buscarIdEstudiantePropio();
        if (estudianteAutorizado <= 0 || idLibro <= 0) {
            throw new IllegalArgumentException("Selecciona un estudiante y un libro validos.");
        }
        return prestamoDao.registrar(estudianteAutorizado, idLibro);
    }

    public boolean devolver(int idPrestamo) throws SQLException {
        validarUsuario();
        validarRolPermitido();
        Integer idEstudiantePermitido = usuario.puedeGestionarPrestamosDeOtros()
            ? null
            : buscarIdEstudiantePropio();
        return prestamoDao.devolver(idPrestamo, idEstudiantePermitido);
    }

    public List<Prestamo> listar() throws SQLException {
        validarUsuario();
        validarRolPermitido();
        if (usuario.puedeGestionarPrestamosDeOtros()) {
            return prestamoDao.listarTodos();
        }
        return prestamoDao.listarPorEstudiante(buscarIdEstudiantePropio());
    }

    private int buscarIdEstudiantePropio() throws SQLException {
        Integer id = estudianteDao.buscarIdPorRut(usuario.getRut());
        if (id == null) {
            throw new IllegalStateException("La cuenta no esta vinculada a una ficha de estudiante.");
        }
        return id;
    }

    private void validarUsuario() {
        if (usuario == null) {
            throw new SecurityException("Debes iniciar sesion para gestionar prestamos.");
        }
    }

    private void validarRolPermitido() {
        if (!usuario.puedeGestionarPrestamos()) {
            throw new SecurityException("El rol no tiene acceso a prestamos.");
        }
    }
}