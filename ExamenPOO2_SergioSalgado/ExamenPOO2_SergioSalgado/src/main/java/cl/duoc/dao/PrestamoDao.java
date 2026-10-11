package cl.duoc.dao;

import cl.duoc.config.DatabaseConnection;
import cl.duoc.model.Prestamo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class PrestamoDao {
    private static final Object BLOQUEO_STOCK = new Object();
    private static final int PLAZO_PRESTAMO_DIAS = 7;

    public int registrar(int idEstudiante, int idLibro) throws SQLException {
        synchronized (BLOQUEO_STOCK) {
            String actualizarStock = "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";
            String insertarPrestamo = "INSERT INTO prestamos "
                    + "(id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                    + "VALUES(?, ?, ?, ?, FALSE)";

            try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
                connection.setAutoCommit(false);
                try {
                    try (PreparedStatement statement = connection.prepareStatement(actualizarStock)) {
                        statement.setInt(1, idLibro);
                        if (statement.executeUpdate() == 0) {
                            throw new SQLException("El libro no existe o no tiene unidades disponibles.");
                        }
                    }

                    LocalDate fechaPrestamo = LocalDate.now();
                    LocalDate fechaLimite = fechaPrestamo.plusDays(PLAZO_PRESTAMO_DIAS);
                    int idPrestamo;
                    try (PreparedStatement statement = connection.prepareStatement(
                            insertarPrestamo, Statement.RETURN_GENERATED_KEYS)) {
                        statement.setInt(1, idEstudiante);
                        statement.setInt(2, idLibro);
                        statement.setDate(3, java.sql.Date.valueOf(fechaPrestamo));
                        statement.setDate(4, java.sql.Date.valueOf(fechaLimite));
                        statement.executeUpdate();
                        try (ResultSet resultSet = statement.getGeneratedKeys()) {
                            if (!resultSet.next()) {
                                throw new SQLException("No se pudo obtener el identificador del prestamo.");
                            }
                            idPrestamo = resultSet.getInt(1);
                        }
                    }

                    connection.commit();
                    return idPrestamo;
                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        }
    }

    public boolean devolver(int idPrestamo, Integer idEstudiantePermitido) throws SQLException {
        synchronized (BLOQUEO_STOCK) {
            String seleccionar = "SELECT id_libro FROM prestamos WHERE id = ? AND devuelto = FALSE";
            if (idEstudiantePermitido != null) {
                seleccionar += " AND id_estudiante = ?";
            }
            seleccionar += " FOR UPDATE";

            try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
                connection.setAutoCommit(false);
                try {
                    int idLibro;
                    try (PreparedStatement statement = connection.prepareStatement(seleccionar)) {
                        statement.setInt(1, idPrestamo);
                        if (idEstudiantePermitido != null) {
                            statement.setInt(2, idEstudiantePermitido);
                        }
                        try (ResultSet resultSet = statement.executeQuery()) {
                            if (!resultSet.next()) {
                                connection.rollback();
                                return false;
                            }
                            idLibro = resultSet.getInt("id_libro");
                        }
                    }

                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE")) {
                        statement.setInt(1, idPrestamo);
                        if (statement.executeUpdate() == 0) {
                            connection.rollback();
                            return false;
                        }
                    }

                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE libros SET stock = stock + 1 WHERE id = ?")) {
                        statement.setInt(1, idLibro);
                        if (statement.executeUpdate() == 0) {
                            throw new SQLException("No se encontro el libro asociado al prestamo.");
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
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT id, id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto "
                + "FROM prestamos WHERE id_estudiante = ? ORDER BY fecha_prestamo DESC, id DESC";
        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEstudiante);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prestamos.add(mapear(resultSet));
                }
            }
        }

        return prestamos;
    }

    public List<Prestamo> listarTodos() throws SQLException {
        String sql = "SELECT id, id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto "
                + "FROM prestamos ORDER BY fecha_prestamo DESC, id DESC";
        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                prestamos.add(mapear(resultSet));
            }
        }

        return prestamos;
    }

    private Prestamo mapear(ResultSet resultSet) throws SQLException {
        return new Prestamo(resultSet.getInt("id"), resultSet.getInt("id_estudiante"),
                resultSet.getInt("id_libro"), resultSet.getDate("fecha_prestamo").toLocalDate(),
                resultSet.getDate("fecha_devolucion").toLocalDate(), resultSet.getBoolean("devuelto"));
    }
}