package cl.duoc.dao;

import cl.duoc.config.DatabaseConnection;
import cl.duoc.model.LibroPopular;
import cl.duoc.model.PrestamoResumen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class ReporteDao {
    public List<LibroPopular> librosMasPrestados() throws SQLException {
        String sql = "SELECT l.titulo, l.autor, COUNT(p.id) AS cantidad_prestamos "
                + "FROM libros l LEFT JOIN prestamos p ON p.id_libro = l.id "
                + "GROUP BY l.id, l.titulo, l.autor ORDER BY cantidad_prestamos DESC, l.titulo";
        List<LibroPopular> reporte = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                reporte.add(new LibroPopular(resultSet.getString("titulo"), resultSet.getString("autor"),
                        resultSet.getLong("cantidad_prestamos")));
            }
        }

        return reporte;
    }

    public List<PrestamoResumen> historialPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT p.id, e.nombre AS estudiante, l.titulo AS libro, p.fecha_prestamo, "
                + "p.fecha_devolucion AS fecha_limite, p.devuelto "
                + "FROM prestamos p JOIN estudiantes e ON e.id = p.id_estudiante "
                + "JOIN libros l ON l.id = p.id_libro WHERE p.id_estudiante = ? "
                + "ORDER BY p.fecha_prestamo DESC, p.id DESC";
        List<PrestamoResumen> historial = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEstudiante);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    historial.add(mapearPrestamo(resultSet));
                }
            }
        }

        return historial;
    }

    public List<PrestamoResumen> librosActualmentePrestados() throws SQLException {
        String sql = "SELECT p.id, e.nombre AS estudiante, l.titulo AS libro, p.fecha_prestamo, "
                + "p.fecha_devolucion AS fecha_limite, p.devuelto "
                + "FROM prestamos p JOIN estudiantes e ON e.id = p.id_estudiante "
                + "JOIN libros l ON l.id = p.id_libro WHERE p.devuelto = FALSE "
                + "ORDER BY p.fecha_devolucion, e.nombre, l.titulo";
        List<PrestamoResumen> prestamos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                prestamos.add(mapearPrestamo(resultSet));
            }
        }

        return prestamos;
    }

    private PrestamoResumen mapearPrestamo(ResultSet resultSet) throws SQLException {
        return new PrestamoResumen(resultSet.getInt("id"), resultSet.getString("estudiante"),
                resultSet.getString("libro"), resultSet.getDate("fecha_prestamo").toLocalDate(),
                resultSet.getDate("fecha_limite").toLocalDate(), resultSet.getBoolean("devuelto"));
    }
}