package cl.duoc.ui;

import cl.duoc.controlador.UsuarioControlador;
import cl.duoc.model.Usuario;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
public class UsuarioPanel extends JPanel {
    private final UsuarioControlador usuarioControlador;
    private final JTextField txtNombre = new JTextField(16);
    private final JTextField txtRut = new JTextField(12);
    private final JTextField txtCorreo = new JTextField(16);
    private final JPasswordField txtContrasena = new JPasswordField(12);
    private final JComboBox<String> cmbRol = new JComboBox<>(new String[]{"estudiante", "bibliotecario"});
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RUT", "Correo", "Rol"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public UsuarioPanel(Usuario usuarioActual) {
        super(new BorderLayout(8, 8));
        usuarioControlador = new UsuarioControlador(usuarioActual);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearFormulario(), BorderLayout.SOUTH);
        tabla.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent event) {
                cargarSeleccion();
            }
        });
        cargarUsuarios();
    }

    private JPanel crearFormulario() {
        JPanel campos = new JPanel(new GridLayout(0, 4, 6, 6));
        campos.add(new JLabel("Nombre:"));
        campos.add(txtNombre);
        campos.add(new JLabel("RUT:"));
        campos.add(txtRut);
        campos.add(new JLabel("Correo:"));
        campos.add(txtCorreo);
        campos.add(new JLabel("Contrasena (alta/cambio):"));
        campos.add(txtContrasena);
        campos.add(new JLabel("Rol:"));
        campos.add(cmbRol);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        JButton btnCrear = new JButton("Registrar");
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                crearUsuario();
            }
        });
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                actualizarUsuario();
            }
        });
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                eliminarUsuario();
            }
        });
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                limpiarFormulario();
            }
        });
        botones.add(btnCrear);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel formulario = new JPanel(new BorderLayout(0, 8));
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);
        return formulario;
    }

    private void cargarUsuarios() {
        try {
            modelo.setRowCount(0);
            for (Usuario usuario : usuarioControlador.listar()) {
                modelo.addRow(new Object[]{usuario.getId(), usuario.getNombre(), usuario.getRut(),
                        usuario.getCorreo(), usuario.getRol()});
            }
        } catch (SQLException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        txtNombre.setText(modelo.getValueAt(fila, 1).toString());
        txtRut.setText(modelo.getValueAt(fila, 2).toString());
        txtCorreo.setText(modelo.getValueAt(fila, 3).toString());
        cmbRol.setSelectedItem(modelo.getValueAt(fila, 4).toString());
        txtContrasena.setText("");
    }

    private Usuario usuarioFormulario(int id) {
        return Usuario.crear(id, txtNombre.getText().trim(), txtRut.getText().trim(), txtCorreo.getText().trim(),
            new String(txtContrasena.getPassword()), cmbRol.getSelectedItem().toString());
    }

    private void crearUsuario() {
        try {
            usuarioControlador.crear(usuarioFormulario(0));
            limpiarFormulario();
            cargarUsuarios();
        } catch (SQLException | IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void actualizarUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario para actualizar.");
            return;
        }
        try {
            int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            if (usuarioControlador.actualizar(usuarioFormulario(id))) {
                limpiarFormulario();
                cargarUsuarios();
            }
        } catch (SQLException | IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void eliminarUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario para eliminar.");
            return;
        }
        int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "Eliminar usuario?", "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (usuarioControlador.eliminar(id)) {
                limpiarFormulario();
                cargarUsuarios();
            }
        } catch (SQLException | IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtRut.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        cmbRol.setSelectedItem("estudiante");
        tabla.clearSelection();
    }

    private void mostrarError(Exception exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Usuarios", JOptionPane.ERROR_MESSAGE);
    }
}