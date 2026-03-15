/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

import DataStructures.Queue;
import DataStructures.TreeNode;

/**
 * Gestor de Procesos del Sistema Operativo.
 * Recibe peticiones de los usuarios, crea los PCB, los encola y los procesa usando un Hilo (Thread).
 * @author Luigi Lauricella &
 */
public class ProcessManager implements Runnable {
    
    private Queue<PCB> processQueue; 
    private FileSystemManager fileSystem; 
    private int nextPid; 
    private boolean isRunning; 

    public ProcessManager(FileSystemManager fileSystem) {
        this.processQueue = new Queue<>();
        this.fileSystem = fileSystem;
        this.nextPid = 1;
        this.isRunning = true;
    }

    public void requestCreateFile(TreeNode<FileDescriptor> parent, String name, int sizeInBlocks, String colorHex) {
        PCB newProcess = new PCB(nextPid++, "CREATE_FILE", name);
        newProcess.setArgs(new Object[]{parent, sizeInBlocks, colorHex});
        newProcess.setState(ProcessState.READY);
        processQueue.enqueue(newProcess);
        System.out.println("[Cola] Proceso " + newProcess.getPid() + " encolado.");
    }
    
    // El hilo principal del Manager (Despachador)
    @Override
    public void run() {
        System.out.println("Process Manager iniciado (Despachador)...");
        
        while (isRunning) {
            if (!processQueue.isEmpty()) {
                PCB currentProcess = processQueue.dequeue();
                
                // En vez de ejecutarlo aquí mismo y detener toda la cola, 
                // creamos un HILO dedicado para este proceso.
                Thread processThread = new Thread(() -> executeProcess(currentProcess));
                processThread.start();
                
            } else {
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }
        }
    }

    // --- LÓGICA DE EJECUCIÓN CONCURRENTE Y LOCKS ---
    private void executeProcess(PCB process) {
        process.setState(ProcessState.RUNNING);
        System.out.println("[Hilo-" + process.getPid() + "] Iniciando " + process.getOperation());
        
        Object[] args = process.getArgs();
        TreeNode<FileDescriptor> parentFolder = (TreeNode<FileDescriptor>) args[0];
        
        try {
            // 1. PEDIR EL LOCK (Como crear es modificar la carpeta padre, pedimos WriteLock)
            // Si otro proceso está modificando esta carpeta, este hilo pasará a BLOCKED automáticamente.
            System.out.println("[Hilo-" + process.getPid() + "] Solicitando acceso exclusivo a " + parentFolder.getData().getName());
            
            // Si el candado está ocupado, el estado cambia a bloqueado mientras espera
            process.setState(ProcessState.BLOCKED); 
            parentFolder.getData().getLock().acquireWriteLock(); 
            
            // Si pasó la línea anterior, significa que obtuvo el candado!
            process.setState(ProcessState.RUNNING);
            System.out.println("[Hilo-" + process.getPid() + "] Candado obtenido. Ejecutando E/S...");
            
            // 2. SIMULAR EL TIEMPO DE E/S (Ej: 2 segundos)
            Thread.sleep(2000); 
            
            // 3. EJECUTAR LA OPERACIÓN REAL
            boolean success = false;
            if (process.getOperation().equals("CREATE_FILE")) {
                int size = (Integer) args[1];
                String color = (String) args[2];
                success = fileSystem.createFile(parentFolder, process.getFileName(), size, color);
            }
            
            // 4. LIBERAR EL LOCK
            parentFolder.getData().getLock().releaseWriteLock();
            System.out.println("[Hilo-" + process.getPid() + "] Operación terminada. Candado liberado.");
            
        } catch (InterruptedException e) {
            System.out.println("Proceso " + process.getPid() + " interrumpido.");
        }

        process.setState(ProcessState.TERMINATED);
    }

    public void stopManager() {
        this.isRunning = false;
    }
}
