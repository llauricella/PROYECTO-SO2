/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStructures;

/**
 * Lock de Lectores y Escritores implementado desde cero.
 * Permite múltiples lectores simultáneos, pero solo un escritor a la vez.
 * @author Luigi Lauricella & Sebastián González
 */
public class RWLock {
    private int readers = 0;
    private boolean isWriting = false;
    private int writeRequests = 0; // Prioridad para evitar que los escritores se queden esperando para siempre

    // --- LOCK COMPARTIDO (PARA LECTURA) ---
    
    public synchronized void acquireRead() {
        // Un lector espera si hay alguien escribiendo o si hay un escritor en la fila de espera
        while (isWriting || writeRequests > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        readers++; // Entra un lector más
    }

    public synchronized void releaseRead() {
        readers--; // Sale un lector
        if (readers == 0) {
            notifyAll(); // Si ya no queda NINGÚN lector, despierta a los escritores
        }
    }

    // --- LOCK EXCLUSIVO (PARA ESCRITURA/MODIFICACIÓN) ---
    
    public synchronized void acquireWrite() {
        writeRequests++; // Anotamos que un escritor quiere entrar
        
        // Un escritor debe esperar si hay alguien leyendo o si hay otro escribiendo
        while (readers > 0 || isWriting) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        writeRequests--; // Ya le toca entrar
        isWriting = true; // Tranca la puerta por completo
    }

    public synchronized void releaseWrite() {
        isWriting = false; // Termina de escribir y abre la puerta
        notifyAll(); // Despierta a todos (lectores y escritores) que estaban esperando
    }
}
