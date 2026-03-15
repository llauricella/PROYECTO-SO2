package OSModels;

import DataStructures.LinkedList;
import DataStructures.TreeNode;

/**
 * Gestor de Procesos del Sistema Operativo.
 * Recibe peticiones de los usuarios, crea los PCB, los encola y los procesa usando un Hilo (Thread).
 * @author Luigi Lauricella & Sebastián González
 */
public class ProcessManager implements Runnable {
    
    private LinkedList<PCB> readyQueue; 
    private FileSystemManager fileSystem; 
    private DiskScheduler diskScheduler;
    private int nextPid; 
    private boolean isRunning;
    private JournalManager journal;

    // CAMBIO: Añadimos JournalManager al constructor para poder usarlo
    public ProcessManager(FileSystemManager fileSystem, DiskScheduler diskScheduler, JournalManager journal) {
        this.readyQueue = new LinkedList<>();
        this.fileSystem = fileSystem;
        this.diskScheduler = diskScheduler;
        this.journal = journal; // Lo guardamos aquí
        this.nextPid = 1;
        this.isRunning = true;
    }

    // --- 1. SOLICITUD DE OPERACIONES ---

    public void requestCreateFile(TreeNode<FileDescriptor> parent, String name, int sizeInBlocks, String colorHex) {
        PCB newProcess = new PCB(nextPid++, "CREATE_FILE", name, 0); 
        newProcess.setArgs(new Object[]{parent, sizeInBlocks, colorHex});
        newProcess.setState(ProcessState.READY);
        
        readyQueue.add(newProcess); 
        System.out.println("[Cola] Proceso " + newProcess.getPid() + " encolado. (Crear Archivo)");
    }
    
    public void requestDeleteNode(TreeNode<FileDescriptor> parent, TreeNode<FileDescriptor> nodeToDelete) {
        int targetBlock = nodeToDelete.getData().getStartBlockId();
        
        PCB newProcess = new PCB(nextPid++, "DELETE_NODE", nodeToDelete.getData().getName(), targetBlock);
        newProcess.setArgs(new Object[]{parent, nodeToDelete});
        newProcess.setState(ProcessState.READY);
        
        readyQueue.add(newProcess);
        System.out.println("[Cola] Proceso " + newProcess.getPid() + " encolado. Destino: Bloque " + targetBlock);
    }

    // --- 2. EL CICLO DE VIDA (El Despachador) ---
    
    @Override
    public void run() {
        System.out.println("Process Manager iniciado con política: " + diskScheduler.getPolicy());
        
        while (isRunning) {
            if (!readyQueue.isEmpty()) {
                PCB nextProcess = diskScheduler.getNextProcess(readyQueue);
                
                if (nextProcess != null) {
                    System.out.println("-> [Cabezal movido al bloque " + diskScheduler.getCurrentHeadPosition() + "]");
                    
                    Thread processThread = new Thread(() -> executeProcess(nextProcess));
                    processThread.start();
                    
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            } else {
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }
        }
    }

    // --- 3. LÓGICA DE EJECUCIÓN, LOCKS Y JOURNALING ---
    // ESTE ES EL MÉTODO QUE SE HABÍA BORRADO
    private void executeProcess(PCB process) {
        process.setState(ProcessState.RUNNING);
        Object[] args = process.getArgs();
        TreeNode<FileDescriptor> parentFolder = (TreeNode<FileDescriptor>) args[0];
        
        try {
            // 1. Pedimos el candado exclusivo (Locks de la Fase 4)
            process.setState(ProcessState.BLOCKED); 
            parentFolder.getData().getLock().acquireWriteLock(); 
            
            process.setState(ProcessState.RUNNING);
            
            // Simular el tiempo de E/S
            Thread.sleep(2000); 
            
            // 2. Ejecutar la operación con Journaling (Fase 6)
            if (process.getOperation().equals("CREATE_FILE")) {
                int size = (Integer) args[1];
                String color = (String) args[2];
                
                // --- INICIO JOURNALING ---
                JournalEntry logEntry = journal.logPendingOperation("CREATE_FILE", process.getFileName(), -1);
                
                // Ejecutamos operación real
                boolean success = fileSystem.createFile(parentFolder, process.getFileName(), size, color);
                
                // --- SIMULACIÓN DE FALLO (CRASH) ---
                if (journal.isCrashSimulated()) {
                    System.out.println("!!! ERROR FATAL SIMULADO: EL SISTEMA SE APAGA INESPERADAMENTE !!!");
                    parentFolder.getData().getLock().releaseWriteLock();
                    return; // Salimos ANTES de hacer el commit
                }

                // --- COMMIT ---
                if (success) {
                    journal.commitOperation(logEntry);
                }
                
            } else if (process.getOperation().equals("DELETE_NODE")) {
                TreeNode<FileDescriptor> nodeDelete = (TreeNode<FileDescriptor>) args[1];
                fileSystem.deleteNode(parentFolder, nodeDelete);
            }
            
            // 3. Liberar el candado
            parentFolder.getData().getLock().releaseWriteLock();
            System.out.println("[Hilo-" + process.getPid() + "] Operación terminada.");
            
        } catch (InterruptedException e) {
            System.out.println("Proceso " + process.getPid() + " interrumpido.");
        }

        process.setState(ProcessState.TERMINATED);
    }

    public void stopManager() {
        this.isRunning = false;
    }
    
    public LinkedList<PCB> getReadyQueue() { return readyQueue; }
}