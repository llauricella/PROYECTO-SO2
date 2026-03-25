/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStructures;

/**
 * 
 * @author Luigi Lauricella & Sebastián González
 */
public class Semaphore {
    private int permisos;

    public Semaphore(int permisosIniciales) {
        this.permisos = permisosIniciales;
    }

    /**
     * Equivalente a acquire() o P(). 
     * Si no hay permisos, pone a dormir el proceso (hilo) actual.
     */
    public synchronized void waitS() {
        while (permisos <= 0) {
            try {
                // El hilo se suspende hasta que otro hilo llame a signal()
                wait(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        permisos--; // Toma el permiso (cierra la puerta)
    }

    /**
     * Equivalente a release() o V().
     * Libera un permiso y despierta a los procesos que estaban esperando.
     */
    public synchronized void signal() {
        permisos++; // Devuelve el permiso (abre la puerta)
        notifyAll(); // Avisa a los demás hilos que ya pueden intentar entrar
    }
}
