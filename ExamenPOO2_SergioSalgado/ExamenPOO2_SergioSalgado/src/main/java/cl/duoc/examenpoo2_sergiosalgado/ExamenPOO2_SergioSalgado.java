/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package cl.duoc.examenpoo2_sergiosalgado;

import cl.duoc.ui.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * @author Sergio Salgado
 */
public class ExamenPOO2_SergioSalgado {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
