package OSModels;

import DataStructures.RWLock;

/**
 * Representa la información (metadata) de un archivo o directorio en el sistema.
 * @author Luigi Lauricella & Sebastián González
 */
public class FileDescriptor {
    private String name;
    private boolean isDirectory;
    private int sizeInBlocks;
    private int startBlockId; // Apunta al primer bloque en el VirtualDisk
    private String owner;     // Dueño del archivo (ej. "admin" o "usuario")
    private String colorHex;  // Color asociado para la GUI

    // --- NUEVA VARIABLE PARA LA CONCURRENCIA ---
    private RWLock lockArchivo; // Lock de Lectores/Escritores para este archivo

    // Constructor para Archivos
    public FileDescriptor(String name, int sizeInBlocks, int startBlockId, String owner, String colorHex) {
        this.name = name;
        this.isDirectory = false;
        this.sizeInBlocks = sizeInBlocks;
        this.startBlockId = startBlockId;
        this.owner = owner;
        this.colorHex = colorHex;
        
        // Inicializamos la cerradura al crear el archivo
        this.lockArchivo = new RWLock(); 
    }

    // Constructor para Directorios (Carpetas)
    public FileDescriptor(String name, String owner) {
        this.name = name;
        this.isDirectory = true;
        this.sizeInBlocks = 0; // Las carpetas no ocupan bloques físicos en nuestra simulación básica
        this.startBlockId = -1;
        this.owner = owner;
        this.colorHex = "#000000"; // Color neutro para carpetas
        
        // Inicializamos la cerradura también para los directorios por seguridad
        this.lockArchivo = new RWLock(); 
    }

    // --- GETTER DEL LOCK (NUEVO) ---
    /**
     * Devuelve el Lock asociado a este archivo para controlar su lectura/escritura.
     */
    public RWLock getLockArchivo() {
        return lockArchivo;
    }

    // --- GETTERS Y SETTERS ORIGINALES ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDirectory() { return isDirectory; }

    public int getSizeInBlocks() { return sizeInBlocks; }

    public int getStartBlockId() { return startBlockId; }
    
    public void setStartBlockId(int startBlockId) { this.startBlockId = startBlockId; }

    public String getOwner() { return owner; }

    public String getColorHex() { return colorHex; }
    
    // --- MÉTODO ACTUALIZADO PARA CUMPLIR EL REQUERIMIENTO 1 ---
    @Override
    public String toString() {
        if (this.isDirectory) {
            return this.name + " (Dir | Dueño: " + this.owner + ")";
        } else {
            return this.name + " [" + this.sizeInBlocks + " bloques] (Dueño: " + this.owner + ")";
        }
    }
}