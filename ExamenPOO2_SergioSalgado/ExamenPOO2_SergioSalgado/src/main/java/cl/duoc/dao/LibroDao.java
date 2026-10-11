package cl.duoc.dao;

import cl.duoc.config.DatabaseConnection;
import cl.duoc.model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class LibroDao {
    public int crear(Libro libro) throws SQLException {
        String sql = "INSERT INTO libros(titulo, autor, isbn, editorial, stock, id_categoria) "
                + "VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(statement, libro);
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el identificador del libro creado.");
    }

    public List<Libro> listar() throws SQLException {
        List<Libro> libros = new ArrayList<>();
        String sql = "SELECT id, titulo, autor, isbn, editorial, stock, id_categoria "
            + "FROM libros ORDER BY id";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                libros.add(new Libro(
                        resultSet.getInt("id"),
                        resultSet.getString("titulo"),
                        resultSet.getString("autor"),
                        resultSet.getString("isbn"),
                        resultSet.getString("editorial"),
                        resultSet.getInt("stock"),
                        resultSet.getInt("id_categoria")
                ));
            }
        }

        return libros;
    }

    public boolean actualizar(Libro libro) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, "
                + "stock = ?, id_categoria = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            asignarParametros(statement, libro);
            statement.setInt(7, libro.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void asignarParametros(PreparedStatement statement, Libro libro) throws SQLException {
        statement.setString(1, libro.getTitulo());
        statement.setString(2, libro.getAutor());
        statement.setString(3, libro.getIsbn());
        statement.setString(4, libro.getEditorial());
        statement.setInt(5, libro.getStock());
        statement.setInt(6, libro.getIdCategoria());
    }
}