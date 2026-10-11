package cl.duoc.controlador;

import cl.duoc.dao.UsuarioDao;
import cl.duoc.model.Usuario;

import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Sergio Salgado
 */
public class LoginControlador {
    private final UsuarioDao usuarioDao;

    public LoginControlador() {
        this(new UsuarioDao());
    }

    LoginControlador(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    public Optional<Usuario> autenticar(String rut, String contrasena) throws SQLException {
        if (rut == null || rut.isBlank() || contrasena == null || contrasena.isBlank()) {
            return Optional.empty();
        }

        return usuarioDao.autenticar(rut.trim(), contrasena);
    }
}