package cl.duoc.ui;

import cl.duoc.controlador.EstudianteControlador;
import cl.duoc.model.Estudiante;
import cl.duoc.model.Usuario;

import javax.swing.JButton;
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
public class EstudiantePanel extends JPanel {
    private final EstudianteControlador estudianteControlador;
    private final JTextField txtNombre = new JTextField(16);
    private final JTextField txtRut = new JTextField(12);
    private final JTextField txtCurso = new JTextField(12);
    private final JTextField txtCorreo = new JTextField(16);
    private final JPasswordField txtContrasena = new JPasswordField(12);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RUT", "Curso", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public EstudiantePanel(Usuario usuario) {
        super(new BorderLayout(8, 8));
        estudianteControlador = new EstudianteControlador(usuario);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearFormulario(), BorderLayout.SOUTH);
        tabla.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent event) {
                cargarSeleccion();
            }
        });
        cargarEstudiantes();
    }

    private JPanel crearFormulario() {
        JPanel campos = new JPanel(new GridLayout(0, 4, 6, 6));
        campos.add(new JLabel("Nombre:"));
        campos.add(txtNombre);
        campos.add(new JLabel("RUT:"));
        campos.add(txtRut);
        campos.add(new JLabel("Curso:"));
        campos.add(txtCurso);
        campos.add(new JLabel("Correo:"));
        campos.add(txtCorreo);
        campos.add(new JLabel("Cambiar contraseña:"));
        campos.add(txtContrasena);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        JButton btnCrear = new JButton("Registrar");
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                crearEstudiante();
            }
        });
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                actualizarEstudiante();
            }
        });
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                eliminarEstudiante();
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

    private void cargarEstudiantes() {
        try {
            modelo.setRowCount(0);
            for (Estudiante estudiante : estudianteControlador.listar()) {
                modelo.addRow(new Object[]{estudiante.getId(), estudiante.getNombre(), estudiante.getRut(),
                        estudiante.getCurso(), estudiante.getCorreo()});
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
        txtCurso.setText(modelo.getValueAt(fila, 3).toString());
        txtCorreo.setText(modelo.getValueAt(fila, 4).toString());
        txtContrasena.setText("");
    }

    private Estudiante estudianteFormulario(int id) {
        return new Estudiante(id, txtNombre.getText().trim(), txtRut.getText().trim(),
                txtCurso.getText().trim(), txtCorreo.getText().trim());
    }

    private void crearEstudiante() {
        try {
            estudianteControlador.crear(estudianteFormulario(0), new String(txtContrasena.getPassword()));
            limpiarFormulario();
            cargarEstudiantes();
        } catch (SQLException | IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void actualizarEstudiante() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante para actualizar.");
            return;
        }

        try {
            int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            String contrasena = new String(txtContrasena.getPassword());
            if (estudianteControlador.actualizar(estudianteFormulario(id), contrasena)) {
                limpiarFormulario();
                cargarEstudiantes();
            }
        } catch (SQLException | IllegalArgumentException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void eliminarEstudiante() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante para eliminar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Eliminar la ficha y su cuenta de acceso? Los prestamos asociados lo pueden impedir.",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            if (estudianteControlador.eliminar(id)) {
                limpiarFormulario();
                cargarEstudiantes();
            }
        } catch (SQLException | SecurityException exception) {
            mostrarError(exception);
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        tabla.clearSelection();
    }

    private void mostrarError(Exception exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}