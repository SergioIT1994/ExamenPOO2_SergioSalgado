package cl.duoc.ui;

import cl.duoc.controlador.BibliotecaControlador;
import cl.duoc.model.Libro;
import cl.duoc.model.Usuario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * @author Sergio Salgado
 */
public class BibliotecaFrame extends JFrame {
    private final BibliotecaControlador bibliotecaControlador = new BibliotecaControlador();
    private final Usuario usuario;
    private final JTextField txtTitulo = new JTextField(12);
    private final JTextField txtAutor = new JTextField(12);
    private final JTextField txtIsbn = new JTextField(12);
    private final JTextField txtEditorial = new JTextField(12);
    private final JTextField txtStock = new JTextField(5);
    private final JTextField txtCategoria = new JTextField(5);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Titulo", "Autor", "ISBN", "Editorial", "Stock", "Categoria"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public BibliotecaFrame(Usuario usuario) {
        super("Biblioteca escolar");
        this.usuario = usuario;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);

        JLabel lblUsuario = new JLabel(usuario.getNombre() + " - " + usuario.getRol());
        JButton btnActualizar = new JButton("Actualizar catalogo");
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                cargarLibros();
            }
        });
        JButton btnCerrarSesion = new JButton("Cerrar sesion");
        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        JPanel encabezado = new JPanel(new FlowLayout(FlowLayout.LEADING));
        encabezado.add(lblUsuario);
        encabezado.add(btnCerrarSesion);

        JPanel panelCatalogo = new JPanel(new BorderLayout(8, 8));
        panelCatalogo.add(btnActualizar, BorderLayout.NORTH);
        panelCatalogo.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(encabezado, BorderLayout.NORTH);
        JTabbedPane pestanas = new JTabbedPane();
        if (usuario.puedeGestionarCatalogo()) {
            panelCatalogo.add(crearFormulario(), BorderLayout.SOUTH);
            pestanas.addTab("Estudiantes", new EstudiantePanel(usuario));
            pestanas.addTab("Usuarios", new UsuarioPanel(usuario));
            tabla.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                @Override
                public void valueChanged(javax.swing.event.ListSelectionEvent event) {
                    cargarSeleccion();
                }
            });
        }
        pestanas.addTab("Catalogo", panelCatalogo);
        pestanas.addTab("Prestamos", new PrestamoPanel(usuario));
        pestanas.addTab("Reportes", new ReportePanel(usuario));
        add(pestanas, BorderLayout.CENTER);
        cargarLibros();
    }

    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new GridLayout(3, 4, 6, 6));
        formulario.add(new JLabel("Titulo:"));
        formulario.add(txtTitulo);
        formulario.add(new JLabel("Autor:"));
        formulario.add(txtAutor);
        formulario.add(new JLabel("ISBN:"));
        formulario.add(txtIsbn);
        formulario.add(new JLabel("Editorial:"));
        formulario.add(txtEditorial);
        formulario.add(new JLabel("Stock:"));
        formulario.add(txtStock);
        formulario.add(new JLabel("ID categoria:"));
        formulario.add(txtCategoria);

        JButton btnCrear = new JButton("Registrar");
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                crearLibro();
            }
        });
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                actualizarLibro();
            }
        });
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                eliminarLibro();
            }
        });
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                limpiarFormulario();
            }
        });
        formulario.add(btnCrear);
        formulario.add(btnActualizar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);
        return formulario;
    }

    private void cargarLibros() {
        try {
            modelo.setRowCount(0);
            for (Libro libro : bibliotecaControlador.listarLibros()) {
                modelo.addRow(new Object[]{libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getIsbn(),
                        libro.getEditorial(), libro.getStock(), libro.getIdCategoria()});
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Error al cargar el catalogo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }

        txtTitulo.setText(modelo.getValueAt(fila, 1).toString());
        txtAutor.setText(modelo.getValueAt(fila, 2).toString());
        txtIsbn.setText(modelo.getValueAt(fila, 3).toString());
        txtEditorial.setText(modelo.getValueAt(fila, 4).toString());
        txtStock.setText(modelo.getValueAt(fila, 5).toString());
        txtCategoria.setText(modelo.getValueAt(fila, 6).toString());
    }

    private Libro crearLibroDesdeFormulario(int id) {
        return new Libro(id, txtTitulo.getText().trim(), txtAutor.getText().trim(), txtIsbn.getText().trim(),
                txtEditorial.getText().trim(), Integer.parseInt(txtStock.getText().trim()),
                Integer.parseInt(txtCategoria.getText().trim()));
    }

    private void crearLibro() {
        try {
            bibliotecaControlador.registrarLibro(usuario, crearLibroDesdeFormulario(0));
            limpiarFormulario();
            cargarLibros();
        } catch (SQLException | IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void actualizarLibro() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro para actualizar.");
            return;
        }

        try {
            int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            if (bibliotecaControlador.actualizarLibro(usuario, crearLibroDesdeFormulario(id))) {
                limpiarFormulario();
                cargarLibros();
            }
        } catch (SQLException | IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void eliminarLibro() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro para eliminar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this, "Eliminar el libro seleccionado?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            if (bibliotecaControlador.eliminarLibro(usuario, id)) {
                limpiarFormulario();
                cargarLibros();
            }
        } catch (SQLException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void limpiarFormulario() {
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        txtStock.setText("");
        txtCategoria.setText("");
        tabla.clearSelection();
    }

    private void mostrarError(Exception exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}