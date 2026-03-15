/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

/**
 *
 * @author Luigi
 */
public class JournalEntry {
    private int id;
    private String operation; // "CREATE" o "DELETE"
    private String fileName;
    private JournalStatus status;
    private int startBlockId; // Muy importante para hacer el Undo y liberar los bloques

    public JournalEntry(int id, String operation, String fileName, int startBlockId) {
        this.id = id;
        this.operation = operation;
        this.fileName = fileName;
        this.status = JournalStatus.PENDIENTE; // Siempre inicia como pendiente
        this.startBlockId = startBlockId;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getOperation() { return operation; }
    public String getFileName() { return fileName; }
    public JournalStatus getStatus() { return status; }
    public void setStatus(JournalStatus status) { this.status = status; }
    public int getStartBlockId() { return startBlockId; }
    
    @Override
    public String toString() {
        return "[" + status + "] " + operation + " -> " + fileName;
    }
}