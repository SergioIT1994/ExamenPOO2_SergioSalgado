package cl.duoc.controlador;

import cl.duoc.dao.LibroDao;
import cl.duoc.model.Libro;
import cl.duoc.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Sergio Salgado
 */
public class BibliotecaControlador {
    private final LibroDao libroDao;

    public BibliotecaControlador() {
        this(new LibroDao());
    }

    BibliotecaControlador(LibroDao libroDao) {
        this.libroDao = libroDao;
    }

    public List<Libro> listarLibros() throws SQLException {
        return libroDao.listar();
    }

    public int registrarLibro(Usuario usuario, Libro libro) throws SQLException {
        validarBibliotecario(usuario);
        validarLibro(libro);
        return libroDao.crear(libro);
    }

    public boolean actualizarLibro(Usuario usuario, Libro libro) throws SQLException {
        validarBibliotecario(usuario);
        validarLibro(libro);
        return libroDao.actualizar(libro);
    }

    public boolean eliminarLibro(Usuario usuario, int id) throws SQLException {
        validarBibliotecario(usuario);
        return libroDao.eliminar(id);
    }

    private void validarBibliotecario(Usuario usuario) {
        if (usuario == null || !usuario.puedeGestionarCatalogo()) {
            throw new SecurityException("Solo el bibliotecario puede gestionar libros.");
        }
    }

    private void validarLibro(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank()
                || libro.getAutor() == null || libro.getAutor().isBlank()
                || libro.getIsbn() == null || libro.getIsbn().isBlank()
                || libro.getEditorial() == null || libro.getEditorial().isBlank()) {
            throw new IllegalArgumentException("Completa titulo, autor, ISBN y editorial.");
        }
        if (libro.getStock() < 0 || libro.getIdCategoria() <= 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo y la categoria es obligatoria.");
        }
    }
}