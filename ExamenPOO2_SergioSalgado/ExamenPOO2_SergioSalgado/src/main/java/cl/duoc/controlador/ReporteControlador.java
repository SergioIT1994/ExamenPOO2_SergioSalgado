package cl.duoc.controlador;

import cl.duoc.dao.EstudianteDao;
import cl.duoc.dao.ReporteDao;
import cl.duoc.model.LibroPopular;
import cl.duoc.model.PrestamoResumen;
import cl.duoc.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class ReporteControlador {
    private final Usuario usuario;
    private final EstudianteDao estudianteDao;
    private final ReporteDao reporteDao;

    public ReporteControlador(Usuario usuario) {
        this(usuario, new EstudianteDao(), new ReporteDao());
    }

    ReporteControlador(Usuario usuario, EstudianteDao estudianteDao, ReporteDao reporteDao) {
        this.usuario = usuario;
        this.estudianteDao = estudianteDao;
        this.reporteDao = reporteDao;
    }

    public List<LibroPopular> librosMasPrestados() throws SQLException {
        validarConsulta();
        return reporteDao.librosMasPrestados();
    }

    public List<PrestamoResumen> historialPorEstudiante(int idEstudianteSolicitado) throws SQLException {
        validarConsulta();
        int idEstudiante = usuario.puedeGestionarPrestamosDeOtros()
            ? idEstudianteSolicitado
            : obtenerIdPropio();
        if (idEstudiante <= 0) {
            throw new IllegalArgumentException("Selecciona un ID de estudiante valido.");
        }
        return reporteDao.historialPorEstudiante(idEstudiante);
    }

    public List<PrestamoResumen> librosActualmentePrestados() throws SQLException {
        validarConsulta();
        return reporteDao.librosActualmentePrestados();
    }

    private int obtenerIdPropio() throws SQLException {
        Integer id = estudianteDao.buscarIdPorRut(usuario.getRut());
        if (id == null) {
            throw new IllegalStateException("La cuenta no esta vinculada a una ficha de estudiante.");
        }
        return id;
    }

    private void validarConsulta() {
        if (usuario == null || !usuario.puedeConsultarReportes()) {
            throw new SecurityException("El usuario no tiene permiso para consultar reportes.");
        }
    }
}