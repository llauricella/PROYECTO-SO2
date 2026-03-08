package OSModels;

/**
 * Simulación del Disco Duro (SD).
 * Gestiona el espacio libre, la fragmentación y la asignación encadenada.
 */
public class VirtualDisk {
    private DiskBlock[] blocks;
    private int totalBlocks;
    private int freeBlocksCount;

    public VirtualDisk(int capacityInBlocks) {
        this.totalBlocks = capacityInBlocks;
        this.freeBlocksCount = capacityInBlocks;
        this.blocks = new DiskBlock[totalBlocks];
        
        // Formateo del disco: Inicializamos todos los bloques
        for (int i = 0; i < totalBlocks; i++) {
            blocks[i] = new DiskBlock(i);
        }
    }

    /**
     * Requerimiento 2: Asignación encadenada.
     * Busca bloques libres saltando los ocupados y los enlaza.
     * * @param blocksNeeded Tamaño del archivo en bloques.
     * @param fileName Nombre del archivo.
     * @param colorHex Color asignado al archivo para la GUI.
     * @return El ID del primer bloque (startBlock), o -1 si no hay espacio.
     */
    public int allocateBlocks(int blocksNeeded, String fileName, String colorHex) {
        // Requerimiento 2: Evitar creación si no hay espacio disponible
        if (blocksNeeded > freeBlocksCount) {
            return -1; 
        }

        int firstBlockId = -1;
        int previousBlockId = -1;
        int blocksAllocated = 0;

        // Recorremos el disco buscando huecos libres
        for (int i = 0; i < totalBlocks && blocksAllocated < blocksNeeded; i++) {
            if (blocks[i].isIsFree()) {
                
                // 1. Ocupar el bloque actual
                blocks[i].setIsFree(false);
                blocks[i].setOwnerFile(fileName);
                blocks[i].setColorHex(colorHex);
                
                // 2. Si es el primero, guardamos su ID para retornarlo
                if (firstBlockId == -1) {
                    firstBlockId = i; 
                }

                // 3. Si hay un bloque anterior, hacer que su puntero 'next' apunte a este bloque
                if (previousBlockId != -1) {
                    blocks[previousBlockId].setNextBlockId(i);
                }

                previousBlockId = i;
                blocksAllocated++;
                freeBlocksCount--;
            }
        }
        
        // 4. El último bloque de la cadena debe apuntar a -1 (EOF - End Of File)
        if (previousBlockId != -1) {
            blocks[previousBlockId].setNextBlockId(-1);
        }

        return firstBlockId;
    }

    /**
     * Requerimiento 2: Manejar la liberación de bloques cuando se eliminan archivos.
     * Recorre la lista enlazada desde el primer bloque y los marca como libres.
     */
    public void freeBlocks(int startBlockId) {
        int currentBlockId = startBlockId;
        
        while (currentBlockId != -1) {
            DiskBlock block = blocks[currentBlockId];
            int nextBlock = block.getNextBlockId(); // Guardamos a dónde apunta antes de borrarlo
            
            // Limpiamos el bloque
            block.setIsFree(true);
            block.setOwnerFile(null);
            block.setColorHex("#FFFFFF");
            block.setNextBlockId(-1);
            
            freeBlocksCount++;
            currentBlockId = nextBlock; // Saltamos al siguiente bloque de la cadena
        }
    }

    // Getters para la GUI
    public DiskBlock[] getBlocks() { return blocks; }
    public int getTotalBlocks() { return totalBlocks; }
    public int getFreeBlocksCount() { return freeBlocksCount; }
}