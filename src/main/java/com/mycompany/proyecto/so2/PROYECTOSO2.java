package com.mycompany.proyecto.so2;

// Importamos nuestra ventana desde el paquete Views
import Views.Dahsboard;

/**
 * @author Luigi Lauricella & Sebastian Gonzalez
 */
public class PROYECTOSO2 {

    public static void main(String[] args) {
        // Le decimos a Java que inicie nuestra interfaz gráfica
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dahsboard().setVisible(true);
            }
        });
    }
}