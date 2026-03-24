package OSModels;

import DataStructures.TreeNode;
import DataStructures.LinkedList;

/**
 * Controlador principal del Sistema de Archivos.
 * Conecta la estructura jerárquica (Árbol) con el Disco Virtual (SD).
 * @author sebas
 */
public class FileSystemManager {
    private TreeNode<FileDescriptor> root; // La raíz del sistema (Ej: "C:")
    private VirtualDisk disk;              // El disco físico
    private boolean isAdminMode;           // Requerimiento 5: Modos de usuario

    public FileSystemManager(VirtualDisk disk) {
        this.disk = disk;
        this.isAdminMode = true; // Por defecto, empezamos como Administrador
        
        // Creamos la carpeta principal del sistema
        FileDescriptor rootMetadata = new FileDescriptor("Raíz", "admin");
        this.root = new TreeNode<>(rootMetadata);
    }

    // --- MODO DE USUARIO (Requerimiento 5) ---
    public void setAdminMode(boolean isAdmin) { this.isAdminMode = isAdmin; }
    public boolean isAdminMode() { return isAdminMode; }
    
    public TreeNode<FileDescriptor> getRoot() { return root; }

    // ==========================================
    //            OPERACIONES CRUD
    // ==========================================

    // --- C: CREATE (Crear Archivo) ---
    public boolean createFile(TreeNode<FileDescriptor> parentFolder, String name, int sizeInBlocks, String colorHex) {
        if (!isAdminMode) return false; // Solo admin puede crear
        if (!parentFolder.getData().isDirectory()) return false; // El padre debe ser una carpeta

        // 1. Pedir espacio al Disco Virtual
        int startBlock = disk.allocateBlocks(sizeInBlocks, name, colorHex);
        if (startBlock == -1) {
            System.out.println("Error: No hay espacio suficiente en el disco.");
            return false;
        }

        // 2. Crear los metadatos y el nodo para el árbol
        FileDescriptor newFile = new FileDescriptor(name, sizeInBlocks, startBlock, "admin", colorHex);
        TreeNode<FileDescriptor> fileNode = new TreeNode<>(newFile);

        // 3. Añadir el archivo a la lista de hijos de la carpeta padre
        parentFolder.addChild(fileNode);
        return true;
    }

    // --- C: CREATE (Crear Directorio) ---
    public boolean createDirectory(TreeNode<FileDescriptor> parentFolder, String name) {
        if (!isAdminMode) return false;
        if (!parentFolder.getData().isDirectory()) return false;

        // Las carpetas no ocupan espacio en nuestro SD simulado
        FileDescriptor newDir = new FileDescriptor(name, "admin");
        TreeNode<FileDescriptor> dirNode = new TreeNode<>(newDir);
        
        parentFolder.addChild(dirNode);
        return true;
    }

    // --- U: UPDATE (Renombrar) ---
    public boolean renameNode(TreeNode<FileDescriptor> targetNode, String newName) {
        if (!isAdminMode) return false; // Solo admin
        targetNode.getData().setName(newName);
        return true;
    }

    // --- D: DELETE (Eliminar Archivo o Carpeta) ---
    public boolean deleteNode(TreeNode<FileDescriptor> parentFolder, TreeNode<FileDescriptor> nodeToDelete) {
        if (!isAdminMode) return false; // Solo admin

        FileDescriptor metadata = nodeToDelete.getData();

        // 1. Si es un DIRECTORIO, debemos borrar todo su contenido recursivamente (Requerimiento 3)
        if (metadata.isDirectory()) {
            LinkedList<TreeNode<FileDescriptor>> children = nodeToDelete.getChildren();
            
            // Borramos los hijos uno por uno hasta que la carpeta quede vacía
            // (Asumiendo que tu LinkedList actualiza su size al hacer remove)
            while (children.getSize() > 0) {
                TreeNode<FileDescriptor> child = children.get(0);
                deleteNode(nodeToDelete, child); // Llamada recursiva
            }
        } else {
            // 2. Si es un ARCHIVO, liberamos sus bloques en el Disco Virtual (SD)
            disk.freeBlocks(metadata.getStartBlockId());
        }

        // 3. Finalmente, quitamos el nodo de la carpeta padre
        parentFolder.getChildren().remove(nodeToDelete);
        return true;
    }
}