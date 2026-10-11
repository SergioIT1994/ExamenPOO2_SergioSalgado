package cl.duoc.controlador;

import cl.duoc.dao.UsuarioDao;
import cl.duoc.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class UsuarioControlador {
    private final Usuario usuarioActual;
    private final UsuarioDao usuarioDao;

    public UsuarioControlador(Usuario usuarioActual) {
        this(usuarioActual, new UsuarioDao());
    }

    UsuarioControlador(Usuario usuarioActual, UsuarioDao usuarioDao) {
        this.usuarioActual = usuarioActual;
        this.usuarioDao = usuarioDao;
    }

    public List<Usuario> listar() throws SQLException {
        validarBibliotecario();
        return usuarioDao.listar();
    }

    public int crear(Usuario usuario) throws SQLException {
        validarBibliotecario();
        validarUsuario(usuario, true);
        return usuarioDao.crear(usuario);
    }

    public boolean actualizar(Usuario usuario) throws SQLException {
        validarBibliotecario();
        validarUsuario(usuario, false);
        return usuarioDao.actualizar(usuario);
    }

    public boolean eliminar(int id) throws SQLException {
        validarBibliotecario();
        if (usuarioActual.getId() == id) {
            throw new IllegalArgumentException("No puedes eliminar la cuenta con la que iniciaste sesion.");
        }
        return usuarioDao.eliminar(id);
    }

    private void validarBibliotecario() {
        if (usuarioActual == null || !usuarioActual.puedeGestionarUsuarios()) {
            throw new SecurityException("Solo el bibliotecario puede gestionar usuarios.");
        }
    }

    private void validarUsuario(Usuario usuario, boolean requiereContrasena) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getRut() == null || usuario.getRut().isBlank()
                || usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            throw new IllegalArgumentException("Completa nombre, RUT y correo.");
        }
        if (usuario.getNombre().length() > 100 || usuario.getRut().length() > 12
                || usuario.getCorreo().length() > 100) {
            throw new IllegalArgumentException("Uno o mas campos exceden el largo permitido.");
        }
        if (requiereContrasena && (usuario.getContrasena() == null || usuario.getContrasena().isBlank())) {
            throw new IllegalArgumentException("La contrasena es obligatoria para crear una cuenta.");
        }
        if (!"bibliotecario".equals(usuario.getRol()) && !"estudiante".equals(usuario.getRol())) {
            throw new IllegalArgumentException("Selecciona un rol valido.");
        }
    }
}