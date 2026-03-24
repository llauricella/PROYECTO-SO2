package OSModels;

import DataStructures.TreeNode;
import DataStructures.LinkedList;

/**
 * Controlador principal del Sistema de Archivos.
 * Conecta la estructura jerárquica (Árbol) con el Disco Virtual (SD).
 * @author sebas
 */
public class FileSystemManager {
    private TreeNode<FileDescriptor> root; 
    private VirtualDisk disk;              
    private boolean isAdminMode;           

    // --- NUEVAS VARIABLES PARA EL JOURNALING ---
    private DataStructures.LinkedList<JournalEntry> journal;
    private boolean simularFallo; // Switch para causar el "Crash"

    public FileSystemManager(VirtualDisk disk) {
        this.disk = disk;
        this.isAdminMode = true; 
        this.journal = new DataStructures.LinkedList<>();
        this.simularFallo = false;
        
        FileDescriptor rootMetadata = new FileDescriptor("Raíz", "admin");
        this.root = new TreeNode<>(rootMetadata);
    }

    public void setAdminMode(boolean isAdmin) { this.isAdminMode = isAdmin; }
    public boolean isAdminMode() { return isAdminMode; }
    public TreeNode<FileDescriptor> getRoot() { return root; }
    
    // Controles del Journal
    public void setSimularFallo(boolean simular) { this.simularFallo = simular; }
    public boolean isSimularFallo() { return simularFallo; }
    public DataStructures.LinkedList<JournalEntry> getJournal() { return journal; }

    // --- C: CREATE CON JOURNALING Y SIMULACIÓN DE FALLO ---
    public boolean createFile(TreeNode<FileDescriptor> parentFolder, String name, int sizeInBlocks, String colorHex) {
        if (!isAdminMode || !parentFolder.getData().isDirectory()) return false;

        // 1. Preparamos el archivo y registramos el PENDIENTE en el Journal
        FileDescriptor newFile = new FileDescriptor(name, sizeInBlocks, -1, "admin", colorHex);
        TreeNode<FileDescriptor> fileNode = new TreeNode<>(newFile);
        
        JournalEntry tx = new JournalEntry("TX-" + System.currentTimeMillis(), "CREATE", fileNode, parentFolder);
        journal.add(tx); // Lo guardamos en la bitácora

        // 2. Ejecutamos la operación real (Reserva de Disco y asignación al árbol)
        int startBlock = disk.allocateBlocks(sizeInBlocks, name, colorHex);
        if (startBlock == -1) {
            tx.setStatus("ABORTADA (Sin Espacio)");
            return false;
        }
        fileNode.getData().setStartBlockId(startBlock); 
        parentFolder.addChild(fileNode);

        // 3. ¡SIMULAMOS EL CRASH JUSTO ANTES DEL COMMIT!
        if (simularFallo) {
            throw new RuntimeException("CRASH_SISTEMA"); // Rompemos el sistema a propósito
        }

        // 4. Si todo salió bien, hacemos COMMIT
        tx.setStatus("CONFIRMADA");
        return true;
    }

    public boolean createDirectory(TreeNode<FileDescriptor> parentFolder, String name) {
        if (!isAdminMode || !parentFolder.getData().isDirectory()) return false;
        FileDescriptor newDir = new FileDescriptor(name, "admin");
        parentFolder.addChild(new TreeNode<>(newDir));
        return true;
    }

    public boolean renameNode(TreeNode<FileDescriptor> targetNode, String newName) {
        if (!isAdminMode) return false; 
        targetNode.getData().setName(newName);
        return true;
    }

    public boolean deleteNode(TreeNode<FileDescriptor> parentFolder, TreeNode<FileDescriptor> nodeToDelete) {
        if (!isAdminMode) return false; 
        FileDescriptor metadata = nodeToDelete.getData();

        if (metadata.isDirectory()) {
            LinkedList<TreeNode<FileDescriptor>> children = nodeToDelete.getChildren();
            while (children.getSize() > 0) {
                deleteNode(nodeToDelete, children.get(0)); 
            }
        } else {
            disk.freeBlocks(metadata.getStartBlockId());
        }
        parentFolder.getChildren().remove(nodeToDelete);
        return true;
    }

    // --- LÓGICA DE RECOVERY (UNDO) ---
    public String recuperarSistema() {
        if (journal.getSize() == 0) return "Journal vacío.";
        
        JournalEntry ultimaTx = journal.get(journal.getSize() - 1);
        if (ultimaTx.getStatus().equals("CONFIRMADA")) {
            return "El sistema está estable. Última transacción fue exitosa.";
        }
        
        if (ultimaTx.getStatus().equals("PENDIENTE") && ultimaTx.getOperation().equals("CREATE")) {
            // ¡HAY UN ARCHIVO CORRUPTO! Vamos a hacer UNDO
            ultimaTx.getParentNode().getChildren().remove(ultimaTx.getTargetNode()); // Lo quitamos del árbol
            
            int bloqueId = ultimaTx.getTargetNode().getData().getStartBlockId();
            if(bloqueId != -1) {
                disk.freeBlocks(bloqueId); // Liberamos los bloques secuestrados en el disco
            }
            ultimaTx.setStatus("DESHECHA (UNDO)");
            return "Se detectó un crash en CREATE. UNDO Aplicado: Archivo borrado y bloques liberados.";
        }
        return "No hay transacciones pendientes de recuperar.";
    }
}