package Views;

// --- 1. IMPORTACIONES NECESARIAS ---
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.table.DefaultTableModel;
import OSModels.VirtualDisk;
import OSModels.FileSystemManager;
import OSModels.FileDescriptor;
import DataStructures.TreeNode;
import DataStructures.LinkedList;

/**
 *
 * @author sebas
 */
public class Dahsboard extends javax.swing.JFrame {
    
    // --- 2. NUESTRAS VARIABLES GLOBALES ---
    private VirtualDisk disk;
    private FileSystemManager fsManager;
    private DefaultTableModel modeloTabla;
    private DataStructures.LinkedList<OSModels.DiskRequest> colaPeticiones = new DataStructures.LinkedList<>();
    private int posicionCabezal = 0;

    /**
     * Creates new form Dahsboard
     */
    public Dahsboard() {
        initComponents(); // ESTO NO SE TOCA (Carga la interfaz gráfica)
        
        // --- 3. INICIAR NUESTRO SISTEMA ---
        this.setLocationRelativeTo(null); // Centrar la ventana en la pantalla al abrir
        inicializarSistema();
        configurarComponentesVisuales();
        actualizarArbolVisual();
        actualizarDiscoVisual();
    }

    // ==========================================
    //       4. MÉTODOS DE CONFIGURACIÓN
    // ==========================================

    private void inicializarSistema() {
        // 1. Creamos un disco de 100 bloques (Requerimiento 2)
        disk = new VirtualDisk(100);
        
        // 2. Iniciamos el Controlador del Sistema de Archivos
        fsManager = new FileSystemManager(disk);
        
        // --- DATOS DE PRUEBA (Para ver que funciona la interfaz) ---
        fsManager.createDirectory(fsManager.getRoot(), "Documentos");
        fsManager.createDirectory(fsManager.getRoot(), "Imagenes");
        fsManager.createFile(fsManager.getRoot(), "notas_so2.txt", 4, "#FF5733"); // Naranja
        fsManager.createFile(fsManager.getRoot(), "foto_gato.png", 10, "#33FF57"); // Verde
    }

    private void configurarComponentesVisuales() {
        // 1. Configurar el ComboBox de Políticas (Requerimiento 4)
        comboPoliticas.removeAllItems(); 
        comboPoliticas.addItem("FIFO");
        comboPoliticas.addItem("SSTF");
        comboPoliticas.addItem("SCAN");
        comboPoliticas.addItem("C-SCAN");

        // 2. Configurar la Tabla de Asignación (Requerimiento 6)
        String[] columnas = {"Nombre", "Bloques", "Bloque Inicial", "Color"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        jTable1.setModel(modeloTabla);
        
        // Llenar la tabla inicial
        actualizarTabla();
    }

    // ==========================================
    //       5. ACTUALIZACIÓN DE LA INTERFAZ
    // ==========================================

    public void actualizarArbolVisual() {
        // Traducimos tu árbol lógico a uno que Swing pueda dibujar
        DefaultMutableTreeNode guiRoot = buildGUITree(fsManager.getRoot());
        
        // Le aplicamos el modelo al JTree de tu interfaz
        DefaultTreeModel treeModel = new DefaultTreeModel(guiRoot);
        jTreeArchivos.setModel(treeModel);
        
        // Expandir todas las carpetas para que no salgan cerradas por defecto
        for (int i = 0; i < jTreeArchivos.getRowCount(); i++) {
            jTreeArchivos.expandRow(i);
        }
    }

    // Traductor recursivo de tu árbol lógico (TreeNode) a nodos visuales (DefaultMutableTreeNode)
    private DefaultMutableTreeNode buildGUITree(TreeNode<FileDescriptor> myNode) {
        DefaultMutableTreeNode guiNode = new DefaultMutableTreeNode(myNode.getData());

        LinkedList<TreeNode<FileDescriptor>> children = myNode.getChildren();
        for (int i = 0; i < children.getSize(); i++) {
            TreeNode<FileDescriptor> child = children.get(i);
            guiNode.add(buildGUITree(child)); // Llamada recursiva a sí mismo
        }
        return guiNode;
    }
    
    // Método para llenar la tabla leyendo los archivos del disco (Requerimiento 6)
    public void actualizarTabla() {
        modeloTabla.setRowCount(0); // Limpiar la tabla antes de llenarla de nuevo
        
        LinkedList<TreeNode<FileDescriptor>> children = fsManager.getRoot().getChildren();
        for (int i = 0; i < children.getSize(); i++) {
            FileDescriptor file = children.get(i).getData();
            
            // La tabla solo debe mostrar los archivos que ocupan disco, no las carpetas
            if (!file.isDirectory()) { 
                Object[] fila = {
                    file.getName(),
                    file.getSizeInBlocks(),
                    file.getStartBlockId(),
                    file.getColorHex()
                };
                modeloTabla.addRow(fila);
            }
        }
    }
    
 // Método para pintar los cuadritos del disco (Requerimiento 6 visual)
    private void actualizarDiscoVisual() {
        panelDiscoVirtual.removeAll();
        // Creamos una cuadrícula de 10x10 (100 bloques) con 2 pixeles de separación
        panelDiscoVirtual.setLayout(new java.awt.GridLayout(10, 10, 2, 2)); 

        OSModels.DiskBlock[] bloques = disk.getBlocks();
        
        for (int i = 0; i < disk.getTotalBlocks(); i++) {
            // CAMBIO: Usamos JLabel en lugar de JPanel para poder mostrar el número centrado
            javax.swing.JLabel cuadro = new javax.swing.JLabel(String.valueOf(i), javax.swing.SwingConstants.CENTER);
            cuadro.setOpaque(true); // OBLIGATORIO en los JLabel para que el color de fondo se pueda ver
            
            // Si el bloque está libre, lo pintamos gris claro. Si está ocupado, usamos su color Hex.
            if (bloques[i].isIsFree()) {
                cuadro.setBackground(new java.awt.Color(220, 220, 220)); 
            } else {
                cuadro.setBackground(java.awt.Color.decode(bloques[i].getColorHex()));
            }
            
            // Le ponemos la letra negra para que el número del bloque resalte
            cuadro.setForeground(java.awt.Color.BLACK);
            
            // ---> LA MAGIA DEL CABEZAL (BORDE ROJO) <---
            if (i == posicionCabezal) {
                // Borde rojo, de 3 pixeles de grosor, si es donde está la aguja
                cuadro.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.RED, 3));
            } else {
                // Borde gris delgadito para el resto de los bloques
                cuadro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(150, 150, 150)));
            }
            
            // Le ponemos el texto que sale al pasar el mouse por encima (Hover)
            cuadro.setToolTipText("Bloque " + i + (bloques[i].isIsFree() ? " (Libre)" : " - " + bloques[i].getOwnerFile()));
            
            panelDiscoVirtual.add(cuadro);
        }

        // Actualizamos el panel para que muestre los cambios
        panelDiscoVirtual.revalidate();
        panelDiscoVirtual.repaint();
    }
    
    // Método clave: Convierte el nodo seleccionado en la interfaz (JTree) al nodo lógico de tu sistema (TreeNode)
    private TreeNode<FileDescriptor> obtenerNodoSeleccionado() {
        // 1. Verificamos si el usuario tiene algo seleccionado en el árbol visual
        javax.swing.tree.DefaultMutableTreeNode nodoVisual = 
            (javax.swing.tree.DefaultMutableTreeNode) jTreeArchivos.getLastSelectedPathComponent();
        
        if (nodoVisual == null) return null; // No hay nada seleccionado

        // 2. Extraemos el 'FileDescriptor' que está guardado dentro de ese nodo visual
        FileDescriptor fdSeleccionado = (FileDescriptor) nodoVisual.getUserObject();

        // 3. Buscamos en nuestro árbol lógico cuál 'TreeNode' tiene ese 'FileDescriptor'
        return buscarNodoLogico(fsManager.getRoot(), fdSeleccionado);
    }
    
    public void actualizarColaVisual() {
        txtCola.setText(""); // Limpiamos el área de texto
        for (int i = 0; i < colaPeticiones.getSize(); i++) {
            txtCola.append(colaPeticiones.get(i).toString() + "\n");
        }
    }

    // Buscador recursivo auxiliar (porque no usamos ArrayList, buscamos a mano)
    private TreeNode<FileDescriptor> buscarNodoLogico(TreeNode<FileDescriptor> actual, FileDescriptor objetivo) {
        if (actual.getData() == objetivo) return actual;

        LinkedList<TreeNode<FileDescriptor>> hijos = actual.getChildren();
        for (int i = 0; i < hijos.getSize(); i++) {
            TreeNode<FileDescriptor> encontrado = buscarNodoLogico(hijos.get(i), objetivo);
            if (encontrado != null) return encontrado;
        }
        return null;
    }
    
    // Método recursivo para buscar y eliminar el nodo de la LinkedList lógica
    private boolean eliminarNodoLogico(TreeNode<FileDescriptor> actual, FileDescriptor target) {
        if (actual == null) return false;
        
        LinkedList<TreeNode<FileDescriptor>> hijos = actual.getChildren();
        
        for (int i = 0; i < hijos.getSize(); i++) {
            TreeNode<FileDescriptor> hijo = hijos.get(i);
            
            // Si encontramos el archivo/carpeta exacto, lo borramos de la LinkedList
            if (hijo.getData() == target) {
                hijos.remove(hijo);
                return true; 
            }
            
            // Si es una carpeta, entramos a buscar dentro de ella
            if (eliminarNodoLogico(hijo, target)) {
                return true;
            }
        }
        return false;
    }
    
    // ==========================================
    //        6. MÉTODOS DE MONITOREO (LOGS)
    // ==========================================

    public void agregarLog(String mensaje) {
        // Obtenemos la hora actual para que el log se vea profesional
        java.time.LocalTime horaActual = java.time.LocalTime.now();
        java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaFormateada = horaActual.format(formato);
        
        // Agregamos el mensaje al JTextArea con un salto de línea
        txtLog.append("[" + horaFormateada + "] " + mensaje + "\n");
        
        // Hacemos que el scroll baje automáticamente al último mensaje
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeArchivos = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        tablaAsignacion = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        panelDiscoVirtual = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        comboPoliticas = new javax.swing.JComboBox<>();
        jButton6 = new javax.swing.JButton();
        lblCiclo = new javax.swing.JLabel();
        lblCabeza = new javax.swing.JLabel();
        radioAdmin = new javax.swing.JRadioButton();
        radioUsuario = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtCola = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Directorio"));

        jScrollPane1.setViewportView(jTreeArchivos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Tabla de Asignación"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaAsignacion.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tablaAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tablaAsignacion, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Disco Duro"));

        javax.swing.GroupLayout panelDiscoVirtualLayout = new javax.swing.GroupLayout(panelDiscoVirtual);
        panelDiscoVirtual.setLayout(panelDiscoVirtualLayout);
        panelDiscoVirtualLayout.setHorizontalGroup(
            panelDiscoVirtualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelDiscoVirtualLayout.setVerticalGroup(
            panelDiscoVirtualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 278, Short.MAX_VALUE)
        );

        jButton1.setText("Agregar a cola");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        comboPoliticas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FIFO", "SSTF", "SCAN", "C-SCAN" }));

        jButton6.setText("Procesar cola");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        lblCiclo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblCiclo.setText("Ciclo: 0");

        lblCabeza.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblCabeza.setText("Cabeza: 0");

        buttonGroup1.add(radioAdmin);
        radioAdmin.setSelected(true);
        radioAdmin.setText("Administrador");
        radioAdmin.addActionListener(this::radioAdminActionPerformed);

        buttonGroup1.add(radioUsuario);
        radioUsuario.setText("Usuario");
        radioUsuario.addActionListener(this::radioUsuarioActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelDiscoVirtual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCiclo)
                            .addComponent(lblCabeza))
                        .addGap(42, 42, 42)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioAdmin)
                            .addComponent(radioUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                        .addComponent(comboPoliticas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton6)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelDiscoVirtual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboPoliticas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton6)
                            .addComponent(jButton1))
                        .addGap(13, 13, 13))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(lblCiclo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblCabeza))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(radioAdmin)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioUsuario)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Monitoreo del Sistema"));

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane2.setViewportView(txtLog);

        txtCola.setEditable(false);
        txtCola.setColumns(20);
        txtCola.setRows(5);
        jScrollPane3.setViewportView(txtCola);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("LOG DE EVENTOS ");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("COLA DE PROCESOS");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(27, 27, 27)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                        .addGap(17, 17, 17))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addGap(152, 152, 152))
        );

        jButton2.setText("Crear Archivo");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setText("Crear Directorio");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setText("Renombrar");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setText("Eliminar");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addComponent(jButton3)
                            .addComponent(jButton4)
                            .addComponent(jButton5))
                        .addGap(0, 24, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5)
                        .addGap(0, 102, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// Importa esto hasta arriba en tu archivo: import javax.swing.JOptionPane;
        
       try {
            // Novedad: Averiguamos dónde hizo clic el usuario
            TreeNode<FileDescriptor> carpetaDestino = obtenerNodoSeleccionado();
            
            // Si no seleccionó nada, por defecto lo guardamos en la Raíz
            if (carpetaDestino == null) {
                carpetaDestino = fsManager.getRoot();
            }

            // Validar que el destino sea una CARPETA (no puedes crear un archivo dentro de un archivo)
            if (!carpetaDestino.getData().isDirectory()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Selecciona una carpeta, no puedes crear archivos dentro de otros archivos.");
                return;
            }

            String nombre = javax.swing.JOptionPane.showInputDialog(this, "Nombre del archivo (Ej. tarea.txt):");
            if (nombre == null || nombre.trim().isEmpty()) return; 

            String tamanoStr = javax.swing.JOptionPane.showInputDialog(this, "Tamaño en bloques (Ej. 5):");
            if (tamanoStr == null || tamanoStr.trim().isEmpty()) return;
            int tamano = Integer.parseInt(tamanoStr);

            String[] colores = {"#FF5733", "#33FF57", "#3357FF", "#FF33A8", "#F3FF33", "#33FFF3", "#8D33FF"};
            String colorAleatorio = colores[(int)(Math.random() * colores.length)];

            // ¡Acá está el cambio principal! Usamos 'carpetaDestino'
            boolean exito = fsManager.createFile(carpetaDestino, nombre, tamano, colorAleatorio);

            if (exito) {
                actualizarArbolVisual();
                actualizarTabla();
                actualizarDiscoVisual(); 
                javax.swing.JOptionPane.showMessageDialog(this, "¡Archivo creado en " + carpetaDestino.getData().getName() + "!");
                agregarLog("Se creó el archivo '" + nombre + "' (" + tamano + " bloques) en la carpeta '" + carpetaDestino.getData().getName() + "'.");
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: No hay espacio suficiente en el disco.");
            }
            
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, ingresa un número válido para el tamaño.");
        }      // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
TreeNode<FileDescriptor> carpetaDestino = obtenerNodoSeleccionado();
        if (carpetaDestino == null) carpetaDestino = fsManager.getRoot();

        if (!carpetaDestino.getData().isDirectory()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona una carpeta para crear el directorio adentro.");
            return;
        }

        String nombre = javax.swing.JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        // Usamos 'carpetaDestino'
        boolean exito = fsManager.createDirectory(carpetaDestino, nombre);

        if (exito) {
            actualizarArbolVisual(); 
            javax.swing.JOptionPane.showMessageDialog(this, "¡Carpeta creada en " + carpetaDestino.getData().getName() + "!");
            agregarLog("Se creó el directorio '" + nombre + "'.");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al crear la carpeta.");
        }     // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
                                    
        // 1. Obtener el nodo lógico directamente (nuestro backend)
        TreeNode<FileDescriptor> nodoLogico = obtenerNodoSeleccionado();

        // 2. Validaciones de seguridad
        if (nodoLogico == null) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un archivo o carpeta del árbol primero.", 
                "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nodoLogico == fsManager.getRoot()) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "No puedes renombrar el directorio Raíz.", 
                "Acción denegada", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Pedir el nuevo nombre mostrando el actual
        String nombreActual = nodoLogico.getData().getName();
        String nuevoNombre = javax.swing.JOptionPane.showInputDialog(this, 
            "Ingresa el nuevo nombre para '" + nombreActual + "':", 
            "Renombrar", javax.swing.JOptionPane.QUESTION_MESSAGE);

        // 4. Si el usuario escribió un nombre válido y aceptó
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            
            // Actualizamos el nombre en la memoria lógica
            nodoLogico.getData().setName(nuevoNombre.trim());
            
            // Refrescamos toda la interfaz
            actualizarArbolVisual();
            actualizarTabla(); 
            
            javax.swing.JOptionPane.showMessageDialog(this, "Renombrado exitosamente a '" + nuevoNombre.trim() + "'.");
            agregarLog("El elemento '" + nombreActual + "' fue renombrado a '" + nuevoNombre.trim() + "'.");
            
            // ¡AQUÍ ESTÁ EL LOG!
            agregarLog("El elemento '" + nombreActual + "' fue renombrado a '" + nuevoNombre.trim() + "'.");
        }
    
 // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
                                          
        javax.swing.tree.DefaultMutableTreeNode nodoVisual = (javax.swing.tree.DefaultMutableTreeNode) jTreeArchivos.getLastSelectedPathComponent();

        if (nodoVisual == null || nodoVisual.isRoot()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta (que no sea la Raíz) para eliminar.");
            return;
        }

        // Extraemos directamente el FileDescriptor
        FileDescriptor fd = (FileDescriptor) nodoVisual.getUserObject();

        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(this, "¿Eliminar '" + fd.getName() + "'?");

        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            
            // 1. Liberar bloques del disco (si es un archivo)
            if (!fd.isDirectory()) {
                OSModels.DiskBlock[] bloques = disk.getBlocks();
                for (int i = fd.getStartBlockId(); i < fd.getStartBlockId() + fd.getSizeInBlocks(); i++) {
                    if (i < bloques.length) {
                        bloques[i].setIsFree(true);
                        bloques[i].setOwnerFile(null);
                        bloques[i].setColorHex("#DCDCDC"); // Gris de libre
                    }
                }
            }

            // 2. ELIMINACIÓN LÓGICA (Buscamos y borramos desde la Raíz)
            eliminarNodoLogico(fsManager.getRoot(), fd);
            
            // 3. ELIMINACIÓN VISUAL
            javax.swing.tree.DefaultTreeModel modeloArbol = (javax.swing.tree.DefaultTreeModel) jTreeArchivos.getModel();
            modeloArbol.removeNodeFromParent(nodoVisual);

            // 4. Refrescar interfaces
            actualizarTabla();
            actualizarDiscoVisual();
            agregarLog("ELIMINADO: " + fd.getName());
        }
    
    
    // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
                                       
        // 1. Usamos tu método existente para saber qué archivo tocar
        TreeNode<FileDescriptor> nodo = obtenerNodoSeleccionado();

        // 2. Validamos que sea un archivo
        if (nodo == null || nodo.getData().isDirectory()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un ARCHIVO en el árbol para solicitar su lectura.");
            return;
        }

        FileDescriptor fd = nodo.getData();
        
        // 3. Creamos la petición lógica
        OSModels.DiskRequest nuevaPeticion = new OSModels.DiskRequest(fd.getName(), fd.getStartBlockId(), "LECTURA");
        
        // 4. La añadimos a tu LinkedList
        colaPeticiones.add(nuevaPeticion);
        
        // 5. Refrescamos la interfaz
        actualizarColaVisual();
        agregarLog("PETICIÓN RECIBIDA: El archivo '" + fd.getName() + "' solicita el bloque " + fd.getStartBlockId());
        
            // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

    procesarPlanificador(); // <-- ¡ESTO ES LO MÁS IMPORTANTE!
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void radioUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioUsuarioActionPerformed
                                            
    // Desactivamos los botones de modificación
    jButton2.setEnabled(false); // Asumiendo que este es Crear Archivo
    jButton3.setEnabled(false); // Crear Directorio
    jButton4.setEnabled(false); // Renombrar
    jButton5.setEnabled(false); // Eliminar

    agregarLog("Cambiado a Modo USUARIO: Permisos de escritura revocados.");
        // TODO add your handling code here:
    }//GEN-LAST:event_radioUsuarioActionPerformed

    private void radioAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioAdminActionPerformed
                                        
    // Reactivamos todos los botones
    jButton2.setEnabled(true); 
    jButton3.setEnabled(true); 
    jButton4.setEnabled(true); 
    jButton5.setEnabled(true); 

    agregarLog("Cambiado a Modo ADMINISTRADOR: Todos los permisos concedidos.");
        // TODO add your handling code here:
    }//GEN-LAST:event_radioAdminActionPerformed

   private void procesarPlanificador() {
        // 1. Validar que haya algo que procesar
        if (colaPeticiones.getSize() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "La cola de peticiones está vacía.");
            return;
        }

        String politica = comboPoliticas.getSelectedItem().toString();
        int movimientosTotales = 0;
        
        agregarLog("--- INICIANDO PLANIFICADOR: " + politica + " ---");
        agregarLog("Cabezal inicia en el bloque: " + posicionCabezal);

        // ==========================================
        // 1. LÓGICA FIFO (First In, First Out)
        // ==========================================
        if (politica.equals("FIFO")) {
            for (int i = 0; i < colaPeticiones.getSize(); i++) {
                OSModels.DiskRequest peticion = colaPeticiones.get(i);
                int distancia = Math.abs(posicionCabezal - peticion.getBlockId());
                movimientosTotales += distancia;
                
                agregarLog("-> [FIFO] Leyendo '" + peticion.getFileName() + "' en bloque " + peticion.getBlockId() + " (Mov: " + distancia + ")");
                posicionCabezal = peticion.getBlockId(); 
            }
            
        // ==========================================
        // 2. LÓGICA SSTF (Shortest Seek Time First)
        // ==========================================
        } else if (politica.equals("SSTF")) {
            java.util.List<OSModels.DiskRequest> pendientes = new java.util.ArrayList<>();
            for (int i = 0; i < colaPeticiones.getSize(); i++) { pendientes.add(colaPeticiones.get(i)); }

            while (!pendientes.isEmpty()) {
                int indiceMasCercano = -1;
                int distanciaMinima = Integer.MAX_VALUE;

                for (int i = 0; i < pendientes.size(); i++) {
                    int distancia = Math.abs(posicionCabezal - pendientes.get(i).getBlockId());
                    if (distancia < distanciaMinima) {
                        distanciaMinima = distancia;
                        indiceMasCercano = i;
                    }
                }

                OSModels.DiskRequest peticionElegida = pendientes.get(indiceMasCercano);
                movimientosTotales += distanciaMinima;
                
                agregarLog("-> [SSTF] Leyendo '" + peticionElegida.getFileName() + "' en bloque " + peticionElegida.getBlockId() + " (Mov: " + distanciaMinima + ")");
                posicionCabezal = peticionElegida.getBlockId();
                pendientes.remove(indiceMasCercano);
            }
            
        // ==========================================
        // 3. LÓGICA SCAN (Algoritmo del Elevador)
        // ==========================================
        } else if (politica.equals("SCAN")) {
            int limiteDisco = disk.getBlocks().length - 1; // El último bloque físico del disco
            
            java.util.List<OSModels.DiskRequest> derecha = new java.util.ArrayList<>();
            java.util.List<OSModels.DiskRequest> izquierda = new java.util.ArrayList<>();

            // Separamos las peticiones según dónde están respecto al cabezal
            for (int i = 0; i < colaPeticiones.getSize(); i++) {
                OSModels.DiskRequest req = colaPeticiones.get(i);
                if (req.getBlockId() >= posicionCabezal) derecha.add(req);
                else izquierda.add(req);
            }

            // Ordenamos: Derecha sube (ascendente), Izquierda baja (descendente)
            derecha.sort((a, b) -> Integer.compare(a.getBlockId(), b.getBlockId()));
            izquierda.sort((a, b) -> Integer.compare(b.getBlockId(), a.getBlockId()));

            // Subimos recogiendo peticiones
            for (OSModels.DiskRequest req : derecha) {
                int distancia = Math.abs(posicionCabezal - req.getBlockId());
                movimientosTotales += distancia;
                agregarLog("-> [SCAN-Sube] Leyendo '" + req.getFileName() + "' en bloque " + req.getBlockId() + " (Mov: " + distancia + ")");
                posicionCabezal = req.getBlockId();
            }

            // Si hay peticiones a la izquierda, el ascensor debe ir hasta el tope y rebotar
            if (!izquierda.isEmpty()) {
                int distAlFinal = Math.abs(posicionCabezal - limiteDisco);
                movimientosTotales += distAlFinal;
                agregarLog("-> [SCAN] Toca el fondo del disco (Bloque " + limiteDisco + ") (Mov: " + distAlFinal + ")");
                posicionCabezal = limiteDisco;

                // Bajamos recogiendo el resto
                for (OSModels.DiskRequest req : izquierda) {
                    int distancia = Math.abs(posicionCabezal - req.getBlockId());
                    movimientosTotales += distancia;
                    agregarLog("-> [SCAN-Baja] Leyendo '" + req.getFileName() + "' en bloque " + req.getBlockId() + " (Mov: " + distancia + ")");
                    posicionCabezal = req.getBlockId();
                }
            }
            
        // ==========================================
        // 4. LÓGICA C-SCAN (Elevador Circular)
        // ==========================================
        } else if (politica.equals("C-SCAN")) {
            int limiteDisco = disk.getBlocks().length - 1; 
            
            java.util.List<OSModels.DiskRequest> derecha = new java.util.ArrayList<>();
            java.util.List<OSModels.DiskRequest> izquierda = new java.util.ArrayList<>();

            for (int i = 0; i < colaPeticiones.getSize(); i++) {
                OSModels.DiskRequest req = colaPeticiones.get(i);
                if (req.getBlockId() >= posicionCabezal) derecha.add(req);
                else izquierda.add(req);
            }

            // En C-SCAN TODO se lee de subida (ascendente)
            derecha.sort((a, b) -> Integer.compare(a.getBlockId(), b.getBlockId()));
            izquierda.sort((a, b) -> Integer.compare(a.getBlockId(), b.getBlockId()));

            for (OSModels.DiskRequest req : derecha) {
                int distancia = Math.abs(posicionCabezal - req.getBlockId());
                movimientosTotales += distancia;
                agregarLog("-> [C-SCAN] Leyendo '" + req.getFileName() + "' en bloque " + req.getBlockId() + " (Mov: " + distancia + ")");
                posicionCabezal = req.getBlockId();
            }

            if (!izquierda.isEmpty()) {
                // Toca el final
                movimientosTotales += Math.abs(posicionCabezal - limiteDisco);
                agregarLog("-> [C-SCAN] Toca el final del disco (Bloque " + limiteDisco + ")");
                
                // Salto brusco al inicio (bloque 0)
                movimientosTotales += limiteDisco; 
                posicionCabezal = 0;
                agregarLog("-> [C-SCAN] Salta al inicio del disco (Bloque 0)");

                // Vuelve a subir recogiendo las que faltaban
                for (OSModels.DiskRequest req : izquierda) {
                    int distancia = Math.abs(posicionCabezal - req.getBlockId());
                    movimientosTotales += distancia;
                    agregarLog("-> [C-SCAN] Leyendo '" + req.getFileName() + "' en bloque " + req.getBlockId() + " (Mov: " + distancia + ")");
                    posicionCabezal = req.getBlockId();
                }
            }
        }

        // ==========================================
        // 5. RESUMEN Y LIMPIEZA
        // ==========================================
        agregarLog("TOTAL DE BLOQUES RECORRIDOS: " + movimientosTotales);
        agregarLog("--- FIN DEL PROCESAMIENTO ---");
        
        colaPeticiones = new DataStructures.LinkedList<>(); 
        actualizarColaVisual();
        
        lblCabeza.setText("Cabeza: " + posicionCabezal);
        actualizarDiscoVisual();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dahsboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Dahsboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> comboPoliticas;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTree jTreeArchivos;
    private javax.swing.JLabel lblCabeza;
    private javax.swing.JLabel lblCiclo;
    private javax.swing.JPanel panelDiscoVirtual;
    private javax.swing.JRadioButton radioAdmin;
    private javax.swing.JRadioButton radioUsuario;
    private javax.swing.JScrollPane tablaAsignacion;
    private javax.swing.JTextArea txtCola;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
}
