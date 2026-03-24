package OSModels;

/**
 *
 * @author Luigi Lauricella & Sebastián González
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

    // --- GETTERS Y SETTERS ---
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "[" + type + "] " + fileName + " (Bloque: " + blockId + ")";
    }
}