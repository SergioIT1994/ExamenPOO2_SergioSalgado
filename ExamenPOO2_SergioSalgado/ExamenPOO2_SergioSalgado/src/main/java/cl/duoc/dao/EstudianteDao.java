package cl.duoc.dao;

import cl.duoc.config.DatabaseConnection;
import cl.duoc.model.Estudiante;
import cl.duoc.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class EstudianteDao {
    private final UsuarioDao usuarioDao = new UsuarioDao();

    public int crear(Estudiante estudiante, String contrasena) throws SQLException {
        String sql = "INSERT INTO estudiantes(nombre, rut, curso, correo) VALUES(?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer idUsuario = obtenerIdUsuario(connection, estudiante.getRut());
                if (idUsuario == null) {
                        usuarioDao.insertar(connection, Usuario.crear(0, estudiante.getNombre(),
                            estudiante.getRut(), estudiante.getCorreo(), contrasena, "estudiante"));
                } else {
                    String actualizarPerfil = "UPDATE usuarios SET nombre = ?, correo = ? WHERE id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(actualizarPerfil)) {
                        statement.setString(1, estudiante.getNombre());
                        statement.setString(2, estudiante.getCorreo());
                        statement.setInt(3, idUsuario);
                        statement.executeUpdate();
                    }
                }

                int idEstudiante;
                try (PreparedStatement statement = connection.prepareStatement(
                        sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    asignarParametros(statement, estudiante);
                    statement.executeUpdate();
                    try (ResultSet resultSet = statement.getGeneratedKeys()) {
                        if (!resultSet.next()) {
                            throw new SQLException("No se pudo obtener el identificador del estudiante.");
                        }
                        idEstudiante = resultSet.getInt(1);
                    }
                }

                connection.commit();
                return idEstudiante;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Estudiante> listar() throws SQLException {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes ORDER BY id";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                estudiantes.add(mapearEstudiante(resultSet));
            }
        }

        return estudiantes;
    }

    public Integer buscarIdPorRut(String rut) throws SQLException {
        String sql = "SELECT id FROM estudiantes WHERE rut = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rut);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return null;
            }
        }
    }

    public boolean actualizar(Estudiante estudiante, String nuevaContrasena) throws SQLException {
        String actualizarEstudiante = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                String rutAnterior = obtenerRutAnterior(connection, estudiante.getId());
                if (rutAnterior == null) {
                    connection.rollback();
                    return false;
                }
                Integer idUsuario = obtenerIdUsuario(connection, rutAnterior);

                try (PreparedStatement statement = connection.prepareStatement(actualizarEstudiante)) {
                    asignarParametros(statement, estudiante);
                    statement.setInt(5, estudiante.getId());
                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                if (idUsuario != null) {
                    actualizarUsuario(connection, idUsuario, estudiante, nuevaContrasena);
                } else if (nuevaContrasena != null && !nuevaContrasena.isBlank()) {
                        usuarioDao.insertar(connection, Usuario.crear(0, estudiante.getNombre(), estudiante.getRut(),
                            estudiante.getCorreo(), nuevaContrasena, "estudiante"));
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
        String seleccionar = "SELECT rut FROM estudiantes WHERE id = ?";
        String eliminarEstudiante = "DELETE FROM estudiantes WHERE id = ?";
        String eliminarUsuario = "DELETE FROM usuarios WHERE rut = ? AND rol = 'estudiante'";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                String rut;
                try (PreparedStatement statement = connection.prepareStatement(seleccionar)) {
                    statement.setInt(1, id);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        rut = resultSet.getString("rut");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(eliminarEstudiante)) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(eliminarUsuario)) {
                    statement.setString(1, rut);
                    statement.executeUpdate();
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

    private String obtenerRutAnterior(Connection connection, int id) throws SQLException {
        String sql = "SELECT rut FROM estudiantes WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("rut");
                }
                return null;
            }
        }
    }

    private Integer obtenerIdUsuario(Connection connection, String rut) throws SQLException {
        String sql = "SELECT id FROM usuarios WHERE rut = ? AND rol = 'estudiante'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rut);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return null;
            }
        }
    }

    private void actualizarUsuario(Connection connection, int idUsuario, Estudiante estudiante,
                                   String nuevaContrasena) throws SQLException {
        boolean cambiarContrasena = nuevaContrasena != null && !nuevaContrasena.isBlank();
        String sql = cambiarContrasena
                ? "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, contraseña = ? WHERE id = ?"
                : "UPDATE usuarios SET nombre = ?, rut = ?, correo = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estudiante.getNombre());
            statement.setString(2, estudiante.getRut());
            statement.setString(3, estudiante.getCorreo());
            if (cambiarContrasena) {
                statement.setString(4, nuevaContrasena);
                statement.setInt(5, idUsuario);
            } else {
                statement.setInt(4, idUsuario);
            }
            statement.executeUpdate();
        }
    }

    private Estudiante mapearEstudiante(ResultSet resultSet) throws SQLException {
        return new Estudiante(resultSet.getInt("id"), resultSet.getString("nombre"),
                resultSet.getString("rut"), resultSet.getString("curso"), resultSet.getString("correo"));
    }

    private void asignarParametros(PreparedStatement statement, Estudiante estudiante) throws SQLException {
        statement.setString(1, estudiante.getNombre());
        statement.setString(2, estudiante.getRut());
        statement.setString(3, estudiante.getCurso());
        statement.setString(4, estudiante.getCorreo());
    }
}