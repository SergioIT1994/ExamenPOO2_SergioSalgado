package cl.duoc.ui;

import cl.duoc.controlador.ReporteControlador;
import cl.duoc.model.LibroPopular;
import cl.duoc.model.PrestamoResumen;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Sergio Salgado
 */
public class ReportePanel extends JPanel {
    private final ReporteControlador reporteControlador;
    private final JTextField txtIdEstudiante = new JTextField(6);
    private final JButton btnMasPrestados = new JButton("Libros mas prestados");
    private final JButton btnHistorial = new JButton("Historial estudiante");
    private final JButton btnPrestados = new JButton("Actualmente en prestamo");
    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public ReportePanel(Usuario usuario) {
        super(new BorderLayout(8, 8));
        reporteControlador = new ReporteControlador(usuario);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEADING));
        if (usuario.puedeGestionarPrestamosDeOtros()) {
            acciones.add(new JLabel("ID estudiante:"));
            acciones.add(txtIdEstudiante);
        }
        acciones.add(btnMasPrestados);
        acciones.add(btnHistorial);
        acciones.add(btnPrestados);

        add(acciones, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnMasPrestados.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mostrarMasPrestados();
            }
        });
        btnHistorial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mostrarHistorial();
            }
        });
        btnPrestados.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                mostrarActualmentePrestados();
            }
        });
        mostrarMasPrestados();
    }

    private void mostrarMasPrestados() {
        ejecutarConsulta(new ConsultaReporte() {
            @Override
            public ResultadoReporte ejecutar() throws SQLException {
                List<Object[]> filas = new ArrayList<>();
                for (LibroPopular libro : reporteControlador.librosMasPrestados()) {
                    filas.add(new Object[]{libro.getTitulo(), libro.getAutor(), libro.getCantidadPrestamos()});
                }
                return new ResultadoReporte(new String[]{"Titulo", "Autor", "Prestamos"}, filas);
            }
        });
    }

    private void mostrarHistorial() {
        try {
            final int idEstudiante;
            if (txtIdEstudiante.getText().isBlank()) {
                idEstudiante = 0;
            } else {
                idEstudiante = Integer.parseInt(txtIdEstudiante.getText().trim());
            }
            ejecutarConsulta(new ConsultaReporte() {
                @Override
                public ResultadoReporte ejecutar() throws SQLException {
                    List<Object[]> filas = new ArrayList<>();
                    for (PrestamoResumen prestamo : reporteControlador.historialPorEstudiante(idEstudiante)) {
                        filas.add(new Object[]{prestamo.getId(), prestamo.getEstudiante(), prestamo.getLibro(),
                                prestamo.getFechaPrestamo(), prestamo.getFechaLimite(), estado(prestamo)});
                    }
                    return new ResultadoReporte(new String[]{"ID", "Estudiante", "Libro", "Prestamo",
                            "Fecha limite", "Estado"}, filas);
                }
            });
        } catch (NumberFormatException exception) {
            mostrarError("El ID del estudiante debe ser numerico.");
        }
    }

    private void mostrarActualmentePrestados() {
        ejecutarConsulta(new ConsultaReporte() {
            @Override
            public ResultadoReporte ejecutar() throws SQLException {
                List<Object[]> filas = new ArrayList<>();
                for (PrestamoResumen prestamo : reporteControlador.librosActualmentePrestados()) {
                    filas.add(new Object[]{prestamo.getId(), prestamo.getEstudiante(), prestamo.getLibro(),
                            prestamo.getFechaPrestamo(), prestamo.getFechaLimite(), estado(prestamo)});
                }
                return new ResultadoReporte(new String[]{"ID", "Estudiante", "Libro", "Prestamo",
                        "Fecha limite", "Estado"}, filas);
            }
        });
    }

    private void ejecutarConsulta(ConsultaReporte consulta) {
        bloquearAcciones(true);
        new SwingWorker<ResultadoReporte, Void>() {
            @Override
            protected ResultadoReporte doInBackground() throws Exception {
                return consulta.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    ResultadoReporte resultado = get();
                        modelo.setDataVector(resultado.filas().toArray(new Object[resultado.filas().size()][]),
                            resultado.columnas());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpio la consulta del reporte.");
                } catch (ExecutionException exception) {
                    Throwable causa = exception.getCause();
                    if (causa == null) {
                        mostrarError(exception.getMessage());
                    } else {
                        mostrarError(causa.getMessage());
                    }
                } finally {
                    bloquearAcciones(false);
                }
            }
        }.execute();
    }

    private String estado(PrestamoResumen prestamo) {
        if (prestamo.isDevuelto()) {
            return "Devuelto";
        }
        if (prestamo.isAtrasado()) {
            return "Atrasado";
        }
        return "Prestado";
    }

    private void bloquearAcciones(boolean bloqueadas) {
        btnMasPrestados.setEnabled(!bloqueadas);
        btnHistorial.setEnabled(!bloqueadas);
        btnPrestados.setEnabled(!bloqueadas);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Reportes", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @author Sergio Salgado
     */
    @FunctionalInterface
    private interface ConsultaReporte {
        ResultadoReporte ejecutar() throws SQLException;
    }

    /**
     * @author Sergio Salgado
     */
    private record ResultadoReporte(String[] columnas, List<Object[]> filas) {
    }
}