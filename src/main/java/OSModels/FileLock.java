/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

/**
 * Gestor de Concurrencia para un Archivo o Directorio.
 * Implementa la lógica de Lectores y Escritores.
 * @author Luigi
 */
public class FileLock {
    private int readers;       // Cantidad de procesos leyendo actualmente
    private boolean isWriting; // Indica si hay un proceso escribiendo

    public FileLock() {
        this.readers = 0;
        this.isWriting = false;
    }

    // --- BLOQUEO DE LECTURA (Compartido) ---
    public synchronized void acquireReadLock() throws InterruptedException {
        // Si alguien está escribiendo, el lector debe esperar (Bloqueado)
        while (isWriting) {
            wait(); 
        }
        readers++; // Entra un nuevo lector
    }

    public synchronized void releaseReadLock() {
        readers--; // Sale un lector
        if (readers == 0) {
            notifyAll(); // Si ya no hay lectores, avisa a los escritores que están esperando
        }
    }

    // --- BLOQUEO DE ESCRITURA (Exclusivo) ---
    public synchronized void acquireWriteLock() throws InterruptedException {
        // Si alguien está escribiendo O alguien está leyendo, el escritor debe esperar
        while (isWriting || readers > 0) {
            wait();
        }
        isWriting = true; // El escritor toma el control exclusivo
    }

    public synchronized void releaseWriteLock() {
        isWriting = false; // El escritor termina
        notifyAll(); // Avisa a todos los que estén esperando (lectores o escritores)
    }
}
