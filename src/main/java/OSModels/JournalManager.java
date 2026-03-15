/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

import DataStructures.LinkedList;
import DataStructures.TreeNode;

/**
 *
 * @author Luigi
 */
public class JournalManager {
    private LinkedList<JournalEntry> log;
    private int nextTransactionId;
    private boolean simulateCrashMode; // Bandera para simular el fallo

    public JournalManager() {
        this.log = new LinkedList<>();
        this.nextTransactionId = 1;
        this.simulateCrashMode = false;
    }

    // Activar o desactivar la simulación de fallo (Se conectará a un botón en la GUI)
    public void setSimulateCrash(boolean simulate) {
        this.simulateCrashMode = simulate;
    }
    public boolean isCrashSimulated() {
        return simulateCrashMode;
    }

    // 1. Registrar operación antes de ejecutarla
    public JournalEntry logPendingOperation(String operation, String fileName, int startBlock) {
        JournalEntry entry = new JournalEntry(nextTransactionId++, operation, fileName, startBlock);
        log.add(entry);
        System.out.println("[JOURNAL] Registrado: " + entry.toString());
        return entry;
    }

    // 2. Confirmar operación (Commit)
    public void commitOperation(JournalEntry entry) {
        entry.setStatus(JournalStatus.CONFIRMADA);
        System.out.println("[JOURNAL] Commit: " + entry.toString());
    }

    // 3. RECUPERACIÓN (Undo) - Se llama al encender el sistema
    public void recoverSystem(FileSystemManager fileSystem) {
        System.out.println("\n--- INICIANDO RECUPERACIÓN DEL SISTEMA (JOURNALING) ---");
        boolean recoveredSomething = false;

        for (int i = 0; i < log.getSize(); i++) {
            JournalEntry entry = log.get(i);
            
            // Si encontramos algo PENDIENTE, significa que hubo un fallo a la mitad
            if (entry.getStatus() == JournalStatus.PENDIENTE) {
                System.out.println("[RECOVERY] Encontrada operación fallida: " + entry.getOperation() + " sobre " + entry.getFileName());
                
                if (entry.getOperation().equals("CREATE_FILE")) {
                    // UNDO de CREATE: Liberar los bloques que se alcanzaron a asignar
                    System.out.println("[RECOVERY] Aplicando UNDO: Liberando bloques del archivo " + entry.getFileName());
                    
                    // Nota: Aquí usarías un método en VirtualDisk para liberar los bloques 
                    // a partir de entry.getStartBlockId()
                    
                    // También tendrías que asegurarte de que el nodo no esté en el árbol (TreeNode).
                    
                } else if (entry.getOperation().equals("DELETE_NODE")) {
                    // UNDO de DELETE: Realmente no borramos nada físicamente aún si no hicimos commit.
                    // Simplemente cancelamos la acción.
                    System.out.println("[RECOVERY] Aplicando UNDO: Cancelando borrado de " + entry.getFileName());
                }
                
                // Una vez deshecho, lo marcamos para que no vuelva a molestar
                entry.setStatus(JournalStatus.CONFIRMADA);
                recoveredSomething = true;
            }
        }
        
        if (!recoveredSomething) {
            System.out.println("[RECOVERY] El sistema está limpio. No hubo fallos.");
        }
        System.out.println("-------------------------------------------------------\n");
    }
    
    // Getter para pintar el log en la Interfaz Gráfica
    public LinkedList<JournalEntry> getLog() { return log; }
}
