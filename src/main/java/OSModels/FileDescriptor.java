package OSModels;

/**
 *
 * @author sebas
 */

/**
 * Representa la información (metadata) de un archivo o directorio en el sistema.
 */
public class FileDescriptor {
    private String name;
    private boolean isDirectory;
    private int sizeInBlocks;
    private int startBlockId; // Apunta al primer bloque en el VirtualDisk
    private String owner;     // Dueño del archivo (ej. "admin" o "usuario")
    private String colorHex;  // Color asociado para la GUI

    // Constructor para Archivos
    public FileDescriptor(String name, int sizeInBlocks, int startBlockId, String owner, String colorHex) {
        this.name = name;
        this.isDirectory = false;
        this.sizeInBlocks = sizeInBlocks;
        this.startBlockId = startBlockId;
        this.owner = owner;
        this.colorHex = colorHex;
    }

    // Constructor para Directorios (Carpetas)
    public FileDescriptor(String name, String owner) {
        this.name = name;
        this.isDirectory = true;
        this.sizeInBlocks = 0; // Las carpetas no ocupan bloques físicos en nuestra simulación básica
        this.startBlockId = -1;
        this.owner = owner;
        this.colorHex = "#000000"; // Color neutro para carpetas
    }

    // --- GETTERS Y SETTERS ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDirectory() { return isDirectory; }

    public int getSizeInBlocks() { return sizeInBlocks; }

    public int getStartBlockId() { return startBlockId; }

    public String getOwner() { return owner; }

    public String getColorHex() { return colorHex; }
    
    @Override
    public String toString() {
        return this.name; 
    }
}