package cl.duoc.ui;

import cl.duoc.controlador.PrestamoControlador;
import cl.duoc.model.Prestamo;
import cl.duoc.model.Usuario;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Sergio Salgado
 */
public class PrestamoPanel extends JPanel {
    private final PrestamoControlador prestamoControlador;
    private final JTextField txtIdEstudiante = new JTextField(6);
    private final JTextField txtIdLibro = new JTextField(6);
    private final JButton btnRegistrar = new JButton("Registrar prestamo");
    private final JButton btnDevolver = new JButton("Registrar devolucion");
    private final JButton btnActualizar = new JButton("Actualizar historial");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Estudiante", "Libro", "Fecha prestamo", "Fecha limite", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public PrestamoPanel(Usuario usuario) {
        super(new BorderLayout(8, 8));
        prestamoControlador = new PrestamoControlador(usuario);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEADING));
        if (usuario.puedeGestionarPrestamosDeOtros()) {
            formulario.add(new JLabel("ID estudiante:"));
            formulario.add(txtIdEstudiante);
        }
        formulario.add(new JLabel("ID libro:"));
        formulario.add(txtIdLibro);
        formulario.add(btnRegistrar);
        formulario.add(btnDevolver);
        formulario.add(btnActualizar);

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                registrarPrestamo();
            }
        });
        btnDevolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                registrarDevolucion();
            }
        });
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                cargarPrestamos();
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent event) {
                completarLibroSeleccionado();
            }
        });
        cargarPrestamos();
    }

    private void registrarPrestamo() {
        try {
            int idLibro = Integer.parseInt(txtIdLibro.getText().trim());
            final int idEstudiante;
            if (txtIdEstudiante.getText().isBlank()) {
                idEstudiante = 0;
            } else {
                idEstudiante = Integer.parseInt(txtIdEstudiante.getText().trim());
            }
            ejecutarEnSegundoPlano(new OperacionPrestamo() {
                @Override
                public boolean ejecutar() throws Exception {
                    return prestamoControlador.registrar(idEstudiante, idLibro) > 0;
                }
            }, "Prestamo registrado.");
        } catch (NumberFormatException exception) {
            mostrarError("Ingresa un ID de libro y, si eres bibliotecario, un ID de estudiante valido.");
        }
    }

    private void registrarDevolucion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarError("Selecciona un prestamo para registrar la devolucion.");
            return;
        }
        int idPrestamo = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
        ejecutarEnSegundoPlano(new OperacionPrestamo() {
            @Override
            public boolean ejecutar() throws Exception {
                return prestamoControlador.devolver(idPrestamo);
            }
        }, "Devolucion registrada.");
    }

    private void cargarPrestamos() {
        bloquearControles(true);
        new SwingWorker<List<Prestamo>, Void>() {
            @Override
            protected List<Prestamo> doInBackground() throws SQLException {
                return prestamoControlador.listar();
            }

            @Override
            protected void done() {
                try {
                    modelo.setRowCount(0);
                    for (Prestamo prestamo : get()) {
                        modelo.addRow(new Object[]{prestamo.getId(), prestamo.getIdEstudiante(),
                                prestamo.getIdLibro(), prestamo.getFechaPrestamo(),
                                prestamo.getFechaDevolucion(), obtenerEstado(prestamo)});
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpio la carga del historial.");
                } catch (ExecutionException exception) {
                    mostrarError(mensajeError(exception));
                } finally {
                    bloquearControles(false);
                }
            }
        }.execute();
    }

    private void ejecutarEnSegundoPlano(OperacionPrestamo operacion, String mensajeExito) {
        bloquearControles(true);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(PrestamoPanel.this, mensajeExito);
                        txtIdLibro.setText("");
                        cargarPrestamos();
                    } else {
                        mostrarError("El prestamo ya fue cerrado o no corresponde a tu cuenta.");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpio la operacion.");
                } catch (ExecutionException exception) {
                    mostrarError(mensajeError(exception));
                } finally {
                    bloquearControles(false);
                }
            }
        }.execute();
    }

    private String obtenerEstado(Prestamo prestamo) {
        if (prestamo.isDevuelto()) {
            return "Devuelto";
        }
        if (prestamo.getFechaDevolucion().isBefore(LocalDate.now())) {
            return "Atrasado";
        }
        return "Prestado";
    }

    private String mensajeError(ExecutionException exception) {
        Throwable causa = exception.getCause();
        if (causa == null) {
            return exception.getMessage();
        }
        return causa.getMessage();
    }

    private void completarLibroSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            txtIdLibro.setText(modelo.getValueAt(fila, 2).toString());
        }
    }

    private void bloquearControles(boolean bloqueados) {
        btnRegistrar.setEnabled(!bloqueados);
        btnDevolver.setEnabled(!bloqueados);
        btnActualizar.setEnabled(!bloqueados);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Prestamos", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @author Sergio Salgado
     */
    @FunctionalInterface
    private interface OperacionPrestamo {
        boolean ejecutar() throws Exception;
    }
}