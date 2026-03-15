/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

import DataStructures.LinkedList;
import DataStructures.TreeNode;

/**
 * Gestor de Procesos del Sistema Operativo.
 * Recibe peticiones de los usuarios, crea los PCB, los encola y los procesa usando un Hilo (Thread).
 * @author Luigi Lauricella &
 */
public class ProcessManager implements Runnable {
    
    // CAMBIO 1: Usamos tu LinkedList en lugar de Queue para que el Scheduler pueda buscar
    private LinkedList<PCB> readyQueue; 
    
    private FileSystemManager fileSystem; 
    private DiskScheduler diskScheduler;
    private int nextPid; 
    private boolean isRunning; 

    public ProcessManager(FileSystemManager fileSystem, DiskScheduler diskScheduler) {
        this.readyQueue = new LinkedList<>();
        this.fileSystem = fileSystem;
        this.diskScheduler = diskScheduler;
        this.nextPid = 1;
        this.isRunning = true;
    }

    // --- 1. SOLICITUD DE OPERACIONES ---

    public void requestCreateFile(TreeNode<FileDescriptor> parent, String name, int sizeInBlocks, String colorHex) {
        // Al crear, no sabemos el bloque aún, así que le ponemos targetBlock = 0 por defecto
        PCB newProcess = new PCB(nextPid++, "CREATE_FILE", name, 0); 
        newProcess.setArgs(new Object[]{parent, sizeInBlocks, colorHex});
        newProcess.setState(ProcessState.READY);
        
        readyQueue.add(newProcess); // Añadimos a la lista de espera
        System.out.println("[Cola] Proceso " + newProcess.getPid() + " encolado. (Crear Archivo)");
    }
    
    public void requestDeleteNode(TreeNode<FileDescriptor> parent, TreeNode<FileDescriptor> nodeToDelete) {
        // Al eliminar, el cabezal debe ir al bloque inicial del archivo
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
                
                // CAMBIO 3: Ya no sacamos el primero ciegamente. 
                // Le pedimos al Planificador que elija el siguiente según la política (SSTF, SCAN, etc.)
                PCB nextProcess = diskScheduler.getNextProcess(readyQueue);
                
                if (nextProcess != null) {
                    System.out.println("-> [Cabezal movido al bloque " + diskScheduler.getCurrentHeadPosition() + "]");
                    
                    // Lanzamos el hilo concurrente
                    Thread processThread = new Thread(() -> executeProcess(nextProcess));
                    processThread.start();
                    
                    // Pequeña pausa para no saturar la creación de hilos y dar tiempo a simular el movimiento del disco
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
                
            } else {
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }
        }
    }

    // --- 3. LÓGICA DE EJECUCIÓN CONCURRENTE Y LOCKS ---
    private void executeProcess(PCB process) {
        process.setState(ProcessState.RUNNING);
        Object[] args = process.getArgs();
        TreeNode<FileDescriptor> parentFolder = (TreeNode<FileDescriptor>) args[0];
        
        try {
            // Pedimos el candado exclusivo
            process.setState(ProcessState.BLOCKED); 
            parentFolder.getData().getLock().acquireWriteLock(); 
            
            process.setState(ProcessState.RUNNING);
            System.out.println("[Hilo-" + process.getPid() + "] Ejecutando " + process.getOperation() + " en bloque " + process.getTargetBlock());
            
            // Simular el tiempo de E/S
            Thread.sleep(2000); 
            
            // Ejecutar la operación real
            if (process.getOperation().equals("CREATE_FILE")) {
                int size = (Integer) args[1];
                String color = (String) args[2];
                fileSystem.createFile(parentFolder, process.getFileName(), size, color);
            } else if (process.getOperation().equals("DELETE_NODE")) {
                TreeNode<FileDescriptor> nodeDelete = (TreeNode<FileDescriptor>) args[1];
                fileSystem.deleteNode(parentFolder, nodeDelete);
            }
            
            // Liberar el candado
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
    
    // Getter para la GUI
    public LinkedList<PCB> getReadyQueue() { return readyQueue; }
}
