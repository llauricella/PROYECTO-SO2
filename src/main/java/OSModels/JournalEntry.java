/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;
import DataStructures.TreeNode;

public class JournalEntry {
    private String transactionId;
    private String operation; // "CREATE" o "DELETE"
    private String status;    // "PENDIENTE", "CONFIRMADA", "DESHECHA (UNDO)"
    private TreeNode<FileDescriptor> targetNode; // El archivo afectado
    private TreeNode<FileDescriptor> parentNode; // La carpeta donde está guardado

    public JournalEntry(String transactionId, String operation, TreeNode<FileDescriptor> targetNode, TreeNode<FileDescriptor> parentNode) {
        this.transactionId = transactionId;
        this.operation = operation;
        this.status = "PENDIENTE";
        this.targetNode = targetNode;
        this.parentNode = parentNode;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getOperation() { return operation; }
    public TreeNode<FileDescriptor> getTargetNode() { return targetNode; }
    public TreeNode<FileDescriptor> getParentNode() { return parentNode; }

    @Override
    public String toString() {
        return "[" + status + "] TX: " + transactionId + " | Operación: " + operation + " -> " + targetNode.getData().getName();
    }
}