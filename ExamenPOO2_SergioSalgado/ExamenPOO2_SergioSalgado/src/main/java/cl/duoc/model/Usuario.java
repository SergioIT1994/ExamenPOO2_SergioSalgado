package cl.duoc.model;

/**
 * @author Sergio Salgado
 */
public abstract class Usuario implements PermisosBiblioteca {
    private int id;
    private String nombre;
    private String rut;
    private String correo;
    private String contrasena;

    protected Usuario(int id, String nombre, String rut, String correo, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public static Usuario crear(int id, String nombre, String rut, String correo,
                                String contrasena, String rol) {
        if ("bibliotecario".equals(rol)) {            
            return new Bibliotecario(id, nombre, rut, correo, contrasena);
        }
        
        if ("estudiante".equals(rol)) {
            return new UsuarioEstudiante(id, nombre, rut, correo, contrasena);
        }
        throw new IllegalArgumentException("El rol debe ser bibliotecario o estudiante.");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public abstract String getRol();
}