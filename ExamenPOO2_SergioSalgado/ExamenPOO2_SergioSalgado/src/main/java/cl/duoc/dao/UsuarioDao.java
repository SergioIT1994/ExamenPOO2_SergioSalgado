package cl.duoc.dao;

import cl.duoc.config.DatabaseConnection;
import cl.duoc.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Sergio Salgado
 */
public class UsuarioDao {
    public Optional<Usuario> autenticar(String rut, String contrasena) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, contraseña, rol FROM usuarios WHERE rut = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rut);

            String nombre;
            String correo;
            String rol;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || contrasena == null
                    || !contrasena.equals(resultSet.getString("contraseña"))) {
                    return Optional.empty();
                }

                int id = resultSet.getInt("id");
                nombre = resultSet.getString("nombre");
                correo = resultSet.getString("correo");
                rol = resultSet.getString("rol");
                return Optional.of(Usuario.crear(id, nombre, rut, correo, null, rol));
            }
        }
    }

    public int crear(Usuario usuario) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            return insertar(connection, usuario);
        }
    }

    int insertar(Connection connection, Usuario usuario) throws SQLException {
        validarRol(usuario.getRol());
        String sql = "INSERT INTO usuarios(nombre, rut, correo, contraseña, rol) VALUES(?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getRut());
            statement.setString(3, usuario.getCorreo());
            statement.setString(4, usuario.getContrasena());
            statement.setString(5, usuario.getRol());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el identificador del usuario creado.");
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nombre, rut, correo, rol FROM usuarios ORDER BY id";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                usuarios.add(mapearUsuario(resultSet));
            }
        }

        return usuarios;
    }

    public boolean actualizar(Usuario usuario) throws SQLException {
        validarRol(usuario.getRol());
        boolean cambiarContrasena = usuario.getContrasena() != null && !usuario.getContrasena().isBlank();
        String seleccionar = "SELECT rut, rol FROM usuarios WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                String rutAnterior;
                String rolAnterior;
                try (PreparedStatement statement = connection.prepareStatement(seleccionar)) {
                    statement.setInt(1, usuario.getId());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        rutAnterior = resultSet.getString("rut");
                        rolAnterior = resultSet.getString("rol");
                    }
                }

                String sql = cambiarContrasena
                        ? "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, contraseña = ?, rol = ? WHERE id = ?"
                        : "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, rol = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, usuario.getNombre());
                    statement.setString(2, usuario.getRut());
                    statement.setString(3, usuario.getCorreo());
                    if (cambiarContrasena) {
                        statement.setString(4, usuario.getContrasena());
                        statement.setString(5, usuario.getRol());
                        statement.setInt(6, usuario.getId());
                    } else {
                        statement.setString(4, usuario.getRol());
                        statement.setInt(5, usuario.getId());
                    }
                    statement.executeUpdate();
                }

                if ("estudiante".equals(rolAnterior)) {
                    String actualizarFicha = "UPDATE estudiantes SET nombre = ?, rut = ?, correo = ? WHERE rut = ?";
                    try (PreparedStatement statement = connection.prepareStatement(actualizarFicha)) {
                        statement.setString(1, usuario.getNombre());
                        statement.setString(2, usuario.getRut());
                        statement.setString(3, usuario.getCorreo());
                        statement.setString(4, rutAnterior);
                        statement.executeUpdate();
                    }
                } else if ("estudiante".equals(usuario.getRol())) {
                    String actualizarFicha = "UPDATE estudiantes SET nombre = ?, correo = ? WHERE rut = ?";
                    try (PreparedStatement statement = connection.prepareStatement(actualizarFicha)) {
                        statement.setString(1, usuario.getNombre());
                        statement.setString(2, usuario.getCorreo());
                        statement.setString(3, usuario.getRut());
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Usuario mapearUsuario(ResultSet resultSet) throws SQLException {
        return Usuario.crear(
                resultSet.getInt("id"),
                resultSet.getString("nombre"),
                resultSet.getString("rut"),
                resultSet.getString("correo"),
                null,
                resultSet.getString("rol")
        );
    }

    private void validarRol(String rol) {
        if (!"bibliotecario".equals(rol) && !"estudiante".equals(rol)) {
            throw new IllegalArgumentException("El rol debe ser bibliotecario o estudiante.");
        }
    }
}