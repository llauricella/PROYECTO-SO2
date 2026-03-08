/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

/**
 *
 * @author Luigi Lauricella & Sebastián González
 */
public class DiskBlock {
    private int blockId;
    private boolean isFree;
    private String ownerFile; // Nombre del archivo que lo ocupa (para colorearlo en la GUI)
    private String colorHex;  // Para pintar el bloque en la GUI
    private int nextBlockId;  // Apuntador al siguiente bloque (-1 si es el último o está libre)

    public DiskBlock(int blockId) {
        this.blockId = blockId;
        this.isFree = true;
        this.ownerFile = null;
        this.colorHex = "#FFFFFF"; // Blanco por defecto (Libre)
        this.nextBlockId = -1;
    }
    
    // Getters & Setters
    /**
     * @return the blockId
     */
    public int getBlockId() {
        return blockId;
    }

    /**
     * @param blockId the blockId to set
     */
    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    /**
     * @return the isFree
     */
    public boolean isIsFree() {
        return isFree;
    }

    /**
     * @param isFree the isFree to set
     */
    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
    }

    /**
     * @return the ownerFile
     */
    public String getOwnerFile() {
        return ownerFile;
    }

    /**
     * @param ownerFile the ownerFile to set
     */
    public void setOwnerFile(String ownerFile) {
        this.ownerFile = ownerFile;
    }

    /**
     * @return the nextBlockId
     */
    public int getNextBlockId() {
        return nextBlockId;
    }

    /**
     * @param nextBlockId the nextBlockId to set
     */
    public void setNextBlockId(int nextBlockId) {
        this.nextBlockId = nextBlockId;
    }

    /**
     * @return the colorHex
     */
    public String getColorHex() {
        return colorHex;
    }

    /**
     * @param colorHex the colorHex to set
     */
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}