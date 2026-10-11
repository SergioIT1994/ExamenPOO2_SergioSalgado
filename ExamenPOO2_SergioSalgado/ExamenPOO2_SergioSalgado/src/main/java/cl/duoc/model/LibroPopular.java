package cl.duoc.model;

/**
 * @author Sergio Salgado
 */
public class LibroPopular {
    private final String titulo;
    private final String autor;
    private final long cantidadPrestamos;

    public LibroPopular(String titulo, String autor, long cantidadPrestamos) {
        this.titulo = titulo;
        this.autor = autor;
        this.cantidadPrestamos = cantidadPrestamos;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public long getCantidadPrestamos() {
        return cantidadPrestamos;
    }
}