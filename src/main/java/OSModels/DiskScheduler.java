/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

import DataStructures.LinkedList;

/**
 * Planificador de Disco que decide el orden de atención de las peticiones de E/S.
 * @author Luigi
 */
public class DiskScheduler {
    
    private int currentHeadPosition;
    private SchedulingPolicy currentPolicy;
    private boolean movingUp; // Dirección del cabezal para SCAN y C-SCAN

    public DiskScheduler(int initialHeadPosition, SchedulingPolicy policy) {
        this.currentHeadPosition = initialHeadPosition; // Requerimiento: Configurable al inicio
        this.currentPolicy = policy;
        this.movingUp = true; // Asumimos que empieza moviéndose hacia arriba (bloques mayores)
    }

    // --- MÉTODOS DE CONFIGURACIÓN ---
    public void setCurrentHeadPosition(int position) { this.currentHeadPosition = position; }
    public int getCurrentHeadPosition() { return currentHeadPosition; }
    public void setPolicy(SchedulingPolicy policy) { this.currentPolicy = policy; }
    public SchedulingPolicy getPolicy() { return currentPolicy; }

    /**
     * Extrae y retorna el siguiente proceso a ejecutar según la política actual.
     */
    public PCB getNextProcess(LinkedList<PCB> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        PCB nextProcess = null;

        switch (currentPolicy) {
            case FIFO:
                nextProcess = getFIFO(readyQueue);
                break;
            case SSTF:
                nextProcess = getSSTF(readyQueue);
                break;
            case SCAN:
                nextProcess = getSCAN(readyQueue);
                break;
            case C_SCAN:
                nextProcess = getCSCAN(readyQueue);
                break;
        }

        // Actualizamos la posición del cabezal a donde se movió
        if (nextProcess != null) {
            this.currentHeadPosition = nextProcess.getTargetBlock();
            readyQueue.remove(nextProcess); // Lo sacamos de la lista de espera usando tu método remove()
        }

        return nextProcess;
    }

    // ==========================================
    //        ALGORITMOS DE PLANIFICACIÓN
    // ==========================================

    // 1. FIFO: El primero que llegó
    private PCB getFIFO(LinkedList<PCB> queue) {
        return queue.get(0); 
    }

    // 2. SSTF: El más cercano al cabezal actual
    private PCB getSSTF(LinkedList<PCB> queue) {
        PCB closest = queue.get(0);
        int minDistance = Math.abs(currentHeadPosition - closest.getTargetBlock());

        for (int i = 1; i < queue.getSize(); i++) {
            PCB pcb = queue.get(i);
            int distance = Math.abs(currentHeadPosition - pcb.getTargetBlock());
            if (distance < minDistance) {
                minDistance = distance;
                closest = pcb;
            }
        }
        return closest;
    }

    // 3. SCAN: Ascensor (Sube hasta el final, luego baja)
    private PCB getSCAN(LinkedList<PCB> queue) {
        PCB closest = null;
        int minDistance = Integer.MAX_VALUE;

        // Buscamos el más cercano EN LA DIRECCIÓN ACTUAL
        for (int i = 0; i < queue.getSize(); i++) {
            PCB pcb = queue.get(i);
            int distance = pcb.getTargetBlock() - currentHeadPosition;

            if (movingUp && distance >= 0 && distance < minDistance) {
                minDistance = distance;
                closest = pcb;
            } else if (!movingUp && distance <= 0 && Math.abs(distance) < minDistance) {
                minDistance = Math.abs(distance);
                closest = pcb;
            }
        }

        // Si no encontró nada en su dirección, cambia de dirección y vuelve a intentar
        if (closest == null) {
            movingUp = !movingUp;
            return getSCAN(queue); // Llamada recursiva con la nueva dirección
        }

        return closest;
    }

    // 4. C-SCAN: Circular (Sube hasta el final, y luego salta al inicio 0 sin atender bajando)
    private PCB getCSCAN(LinkedList<PCB> queue) {
        PCB closest = null;
        int minDistance = Integer.MAX_VALUE;

        // Buscamos el más cercano moviéndonos SOLO hacia arriba
        for (int i = 0; i < queue.getSize(); i++) {
            PCB pcb = queue.get(i);
            int distance = pcb.getTargetBlock() - currentHeadPosition;

            if (distance >= 0 && distance < minDistance) {
                minDistance = distance;
                closest = pcb;
            }
        }

        // Si no hay más peticiones hacia arriba, saltamos al bloque 0 y buscamos el primero
        if (closest == null) {
            currentHeadPosition = 0; // Salto circular al inicio
            return getCSCAN(queue);
        }

        return closest;
    }
}
