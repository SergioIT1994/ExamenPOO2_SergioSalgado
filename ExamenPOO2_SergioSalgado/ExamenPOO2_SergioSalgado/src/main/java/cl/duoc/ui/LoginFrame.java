package cl.duoc.ui;

import cl.duoc.controlador.LoginControlador;
import cl.duoc.model.Usuario;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * @author Sergio Salgado
 */
public class LoginFrame extends JFrame {
    private final LoginControlador loginControlador = new LoginControlador();
    private final JTextField txtRut = new JTextField(16);
    private final JPasswordField txtContrasena = new JPasswordField(16);

    public LoginFrame() {
        super("Biblioteca escolar - Iniciar sesion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(390, 210);

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.anchor = GridBagConstraints.LINE_START;
        restricciones.insets = new Insets(0, 0, 12, 10);
        restricciones.gridx = 0;
        restricciones.gridy = 0;
        formulario.add(new JLabel("RUT:"), restricciones);

        restricciones.gridx = 1;
        restricciones.weightx = 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.insets = new Insets(0, 0, 12, 0);
        formulario.add(txtRut, restricciones);

        restricciones.gridx = 0;
        restricciones.gridy = 1;
        restricciones.weightx = 0;
        restricciones.fill = GridBagConstraints.NONE;
        restricciones.insets = new Insets(0, 0, 0, 10);
        formulario.add(new JLabel("Contraseña:"), restricciones);

        restricciones.gridx = 1;
        restricciones.weightx = 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.insets = new Insets(0, 0, 0, 0);
        formulario.add(txtContrasena, restricciones);

        JButton btnIngresar = new JButton("Ingresar");
        ActionListener accionIngresar = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ingresar();
            }
        };
        btnIngresar.addActionListener(accionIngresar);
        txtContrasena.addActionListener(accionIngresar);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.add(btnIngresar);

        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            String contrasena = new String(txtContrasena.getPassword());
            Usuario usuario = loginControlador.autenticar(txtRut.getText(), contrasena).orElse(null);
            if (usuario == null) {
                JOptionPane.showMessageDialog(this, "RUT o contrasena incorrectos.",
                        "Inicio de sesion", JOptionPane.WARNING_MESSAGE);
                return;
            }

            new BibliotecaFrame(usuario).setVisible(true);
            dispose();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Error de conexion", JOptionPane.ERROR_MESSAGE);
        }
    }
}