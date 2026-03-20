/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

/**
 *
 * @author sebas
 */
public class DiskRequest {
     private String fileName;
    private int blockId;
    private String type; 

    public DiskRequest(String fileName, int blockId, String type) {
        this.fileName = fileName;
        this.blockId = blockId;
        this.type = type;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + fileName + " (Bloque: " + blockId + ")";
    }

    public int getBlockId() { return blockId; }
    public String getFileName() { return fileName; }
}