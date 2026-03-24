package Views;

// --- 1. IMPORTACIONES NECESARIAS (SIN JAVA.UTIL.* PARA COLECCIONES) ---
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
    private javax.swing.Timer timerAnimacion;
    
    // CORRECCIÓN CRÍTICA: Se reemplaza java.util.List por tu propia DataStructures.LinkedList
    private DataStructures.LinkedList<Integer> rutaCompleta = new DataStructures.LinkedList<>();
    private int pasoActual = 0;

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
    //        4. MÉTODOS DE CONFIGURACIÓN
    // ==========================================

    private void inicializarSistema() {
        disk = new VirtualDisk(100);
        fsManager = new FileSystemManager(disk);
        
        fsManager.createDirectory(fsManager.getRoot(), "Documentos");
        fsManager.createDirectory(fsManager.getRoot(), "Imagenes");
        fsManager.createFile(fsManager.getRoot(), "notas_so2.txt", 4, "#FF5733");
        fsManager.createFile(fsManager.getRoot(), "foto_gato.png", 10, "#33FF57");
    }

    private void configurarComponentesVisuales() {
        comboPoliticas.removeAllItems(); 
        comboPoliticas.addItem("FIFO");
        comboPoliticas.addItem("SSTF");
        comboPoliticas.addItem("SCAN");
        comboPoliticas.addItem("C-SCAN");

        String[] columnas = {"Nombre", "Bloques", "Bloque Inicial", "Color"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        jTable1.setModel(modeloTabla);
        
        actualizarTabla();
    }

    // ==========================================
    //        5. ACTUALIZACIÓN DE LA INTERFAZ
    // ==========================================

    public void actualizarArbolVisual() {
        DefaultMutableTreeNode guiRoot = buildGUITree(fsManager.getRoot());
        DefaultTreeModel treeModel = new DefaultTreeModel(guiRoot);
        jTreeArchivos.setModel(treeModel);
        
        for (int i = 0; i < jTreeArchivos.getRowCount(); i++) {
            jTreeArchivos.expandRow(i);
        }
    }

    private DefaultMutableTreeNode buildGUITree(TreeNode<FileDescriptor> myNode) {
        DefaultMutableTreeNode guiNode = new DefaultMutableTreeNode(myNode.getData());
        LinkedList<TreeNode<FileDescriptor>> children = myNode.getChildren();
        for (int i = 0; i < children.getSize(); i++) {
            TreeNode<FileDescriptor> child = children.get(i);
            guiNode.add(buildGUITree(child)); 
        }
        return guiNode;
    }
    
    public void actualizarTabla() {
        modeloTabla.setRowCount(0); 
        LinkedList<TreeNode<FileDescriptor>> children = fsManager.getRoot().getChildren();
        for (int i = 0; i < children.getSize(); i++) {
            FileDescriptor file = children.get(i).getData();
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
    
    private void actualizarDiscoVisual() {
        panelDiscoVirtual.removeAll();
        panelDiscoVirtual.setLayout(new java.awt.GridLayout(10, 10, 2, 2)); 

        OSModels.DiskBlock[] bloques = disk.getBlocks();
        
        for (int i = 0; i < disk.getTotalBlocks(); i++) {
            javax.swing.JLabel cuadro = new javax.swing.JLabel(String.valueOf(i), javax.swing.SwingConstants.CENTER);
            cuadro.setOpaque(true); 
            
            if (bloques[i].isIsFree()) {
                cuadro.setBackground(new java.awt.Color(220, 220, 220)); 
            } else {
                cuadro.setBackground(java.awt.Color.decode(bloques[i].getColorHex()));
            }
            
            cuadro.setForeground(java.awt.Color.BLACK);
            
            if (i == posicionCabezal) {
                cuadro.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.RED, 3));
            } else {
                cuadro.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(150, 150, 150)));
            }
            
            cuadro.setToolTipText("Bloque " + i + (bloques[i].isIsFree() ? " (Libre)" : " - " + bloques[i].getOwnerFile()));
            panelDiscoVirtual.add(cuadro);
        }

        panelDiscoVirtual.revalidate();
        panelDiscoVirtual.repaint();
    }
    
    private TreeNode<FileDescriptor> obtenerNodoSeleccionado() {
        javax.swing.tree.DefaultMutableTreeNode nodoVisual = 
            (javax.swing.tree.DefaultMutableTreeNode) jTreeArchivos.getLastSelectedPathComponent();
        
        if (nodoVisual == null) return null;
        FileDescriptor fdSeleccionado = (FileDescriptor) nodoVisual.getUserObject();
        return buscarNodoLogico(fsManager.getRoot(), fdSeleccionado);
    }
    
    public void actualizarColaVisual() {
        txtCola.setText(""); 
        for (int i = 0; i < colaPeticiones.getSize(); i++) {
            txtCola.append(colaPeticiones.get(i).toString() + "\n");
        }
    }

    private TreeNode<FileDescriptor> buscarNodoLogico(TreeNode<FileDescriptor> actual, FileDescriptor objetivo) {
        if (actual.getData() == objetivo) return actual;

        LinkedList<TreeNode<FileDescriptor>> hijos = actual.getChildren();
        for (int i = 0; i < hijos.getSize(); i++) {
            TreeNode<FileDescriptor> encontrado = buscarNodoLogico(hijos.get(i), objetivo);
            if (encontrado != null) return encontrado;
        }
        return null;
    }
    
    private boolean eliminarNodoLogico(TreeNode<FileDescriptor> actual, FileDescriptor target) {
        if (actual == null) return false;
        LinkedList<TreeNode<FileDescriptor>> hijos = actual.getChildren();
        
        for (int i = 0; i < hijos.getSize(); i++) {
            TreeNode<FileDescriptor> hijo = hijos.get(i);
            if (hijo.getData() == target) {
                hijos.remove(hijo);
                return true; 
            }
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
        java.time.LocalTime horaActual = java.time.LocalTime.now();
        java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaFormateada = horaActual.format(formato);
        
        txtLog.append("[" + horaFormateada + "] " + mensaje + "\n");
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
        sliderVelocidad = new javax.swing.JSlider();
        btnPausa = new javax.swing.JButton();
        lblVelocidad = new javax.swing.JLabel();
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
        btnLeer = new javax.swing.JButton();
        lblEstadisticas = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuExportarTxt = new javax.swing.JMenuItem();
        menuExportarCsv = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
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

        sliderVelocidad.setMaximum(1000);
        sliderVelocidad.setMinimum(50);
        sliderVelocidad.setValue(500);
        sliderVelocidad.addChangeListener(this::sliderVelocidadStateChanged);

        btnPausa.setText("Pausar");
        btnPausa.addActionListener(this::btnPausaActionPerformed);

        lblVelocidad.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblVelocidad.setText("Velocidad: Normal (x1)");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCiclo)
                            .addComponent(lblCabeza))
                        .addGap(42, 42, 42)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioAdmin)
                            .addComponent(radioUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(42, 71, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(comboPoliticas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(sliderVelocidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnPausa))
                            .addComponent(lblVelocidad)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelDiscoVirtual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelDiscoVirtual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnPausa)
                            .addComponent(sliderVelocidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(radioAdmin)
                                    .addComponent(lblVelocidad))
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

        btnLeer.setText("Leer");
        btnLeer.addActionListener(this::btnLeerActionPerformed);

        lblEstadisticas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblEstadisticas.setText("Tiempo Promedio: 0 blq/pet");

        jMenu1.setText("Archivo");

        menuExportarTxt.setText("Exportar Resumen del Sistema (.txt)");
        menuExportarTxt.addActionListener(this::menuExportarTxtActionPerformed);
        jMenu1.add(menuExportarTxt);

        menuExportarCsv.setText("Exportar Estadísticas Procesos (.csv)");
        menuExportarCsv.addActionListener(this::menuExportarCsvActionPerformed);
        jMenu1.add(menuExportarCsv);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Reportes");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

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
                            .addComponent(jButton5)
                            .addComponent(btnLeer)
                            .addComponent(lblEstadisticas))
                        .addGap(37, 37, 37))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLeer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                        .addComponent(lblEstadisticas)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
try {
            TreeNode<FileDescriptor> carpetaDestino = obtenerNodoSeleccionado();
            if (carpetaDestino == null) {
                carpetaDestino = fsManager.getRoot();
            }

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

            boolean exito = fsManager.createFile(carpetaDestino, nombre, tamano, colorAleatorio);

            if (exito) {
                actualizarArbolVisual();
                actualizarTabla();
                actualizarDiscoVisual(); 
                javax.swing.JOptionPane.showMessageDialog(this, "¡Archivo creado en " + carpetaDestino.getData().getName() + "!");
                agregarLog("Se creó el archivo '" + nombre + "' (" + tamano + " bloques) en la carpeta '" + carpetaDestino.getData().getName() + "'.");
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: No hay espacio suficiente en el disco o no tienes permisos.");
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
                                    
       TreeNode<FileDescriptor> nodoLogico = obtenerNodoSeleccionado();

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

        String nombreActual = nodoLogico.getData().getName();
        String nuevoNombre = javax.swing.JOptionPane.showInputDialog(this, 
            "Ingresa el nuevo nombre para '" + nombreActual + "':", 
            "Renombrar", javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            
            // Requerimiento 5: Solo admin, validado internamente en fsManager o antes de actuar
            boolean exito = fsManager.renameNode(nodoLogico, nuevoNombre.trim());
            
            if(exito){
                actualizarArbolVisual();
                actualizarTabla(); 
                javax.swing.JOptionPane.showMessageDialog(this, "Renombrado exitosamente a '" + nuevoNombre.trim() + "'.");
                agregarLog("El elemento '" + nombreActual + "' fue renombrado a '" + nuevoNombre.trim() + "'.");
            } else {
                 javax.swing.JOptionPane.showMessageDialog(this, "No tienes permisos de Administrador.");
            }
        }
    
 // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
                                          
       TreeNode<FileDescriptor> nodoLogico = obtenerNodoSeleccionado();
        javax.swing.tree.DefaultMutableTreeNode nodoVisual = (javax.swing.tree.DefaultMutableTreeNode) jTreeArchivos.getLastSelectedPathComponent();

        if (nodoVisual == null || nodoVisual.isRoot() || nodoLogico == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta (que no sea la Raíz) para eliminar.");
            return;
        }

        FileDescriptor fd = (FileDescriptor) nodoVisual.getUserObject();
        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(this, "¿Eliminar '" + fd.getName() + "'?");

        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            
            // Requerimiento 3 y 5: Borrar y liberar (Controlado por fsManager)
            boolean exito = fsManager.deleteNode(fsManager.getRoot(), nodoLogico); // La eliminación real la controla el Manager
            
            if (exito) {
                javax.swing.tree.DefaultTreeModel modeloArbol = (javax.swing.tree.DefaultTreeModel) jTreeArchivos.getModel();
                modeloArbol.removeNodeFromParent(nodoVisual);

                actualizarTabla();
                actualizarDiscoVisual();
                agregarLog("ELIMINADO: " + fd.getName());
            } else {
                 javax.swing.JOptionPane.showMessageDialog(this, "No tienes permisos de Administrador para eliminar.");
            }
        }
    
    
    // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
                                       
       TreeNode<FileDescriptor> nodo = obtenerNodoSeleccionado();

        if (nodo == null || nodo.getData().isDirectory()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un ARCHIVO en el árbol para solicitar su lectura.");
            return;
        }

        FileDescriptor fd = nodo.getData();
        OSModels.DiskRequest nuevaPeticion = new OSModels.DiskRequest(fd.getName(), fd.getStartBlockId(), "LECTURA");
        
        colaPeticiones.add(nuevaPeticion);
        
        actualizarColaVisual();
        agregarLog("PETICIÓN RECIBIDA: El archivo '" + fd.getName() + "' solicita el bloque " + fd.getStartBlockId());
    
            // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

   procesarPlanificador();
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void radioUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioUsuarioActionPerformed
                                            
 fsManager.setAdminMode(false);
        jButton2.setEnabled(false); 
        jButton3.setEnabled(false); 
        jButton4.setEnabled(false); 
        jButton5.setEnabled(false); 
        agregarLog("Cambiado a Modo USUARIO: Permisos de escritura revocados.");
        // TODO add your handling code here:
    }//GEN-LAST:event_radioUsuarioActionPerformed

    private void radioAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioAdminActionPerformed
                                        
   fsManager.setAdminMode(true);
        jButton2.setEnabled(true); 
        jButton3.setEnabled(true); 
        jButton4.setEnabled(true); 
        jButton5.setEnabled(true); 
        agregarLog("Cambiado a Modo ADMINISTRADOR: Todos los permisos concedidos.");
        // TODO add your handling code here:
    }//GEN-LAST:event_radioAdminActionPerformed

    private void btnLeerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLeerActionPerformed
javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = 
            (javax.swing.tree.DefaultMutableTreeNode) jTreeArchivos.getLastSelectedPathComponent();

        if (nodoSeleccionado == null || !nodoSeleccionado.isLeaf()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, selecciona un archivo final válido.");
            return;
        }

        String nombreArchivo = nodoSeleccionado.getUserObject().toString().split(" ")[0]; 
        OSModels.DiskBlock[] bloques = disk.getBlocks();
        int bloquesEncolados = 0;

        for (int i = 0; i < disk.getTotalBlocks(); i++) {
            if (!bloques[i].isIsFree() && bloques[i].getOwnerFile() != null) {
                if (bloques[i].getOwnerFile().equals(nombreArchivo)) {
                    OSModels.DiskRequest nuevaPeticion = new OSModels.DiskRequest(nombreArchivo, i, "LECTURA");
                    colaPeticiones.add(nuevaPeticion);
                    bloquesEncolados++;
                }
            }
        }

        if (bloquesEncolados > 0) {
            agregarLog("Se enviaron " + bloquesEncolados + " bloques de '" + nombreArchivo + "' a la cola.");
            actualizarColaVisual(); 
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "No se encontraron datos de este archivo en el disco.");
        }
    
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLeerActionPerformed

    private void btnPausaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPausaActionPerformed
if (timerAnimacion != null) {
             if (timerAnimacion.isRunning()) {
                 timerAnimacion.stop();
                 btnPausa.setText("Reanudar");
             } else {
                 timerAnimacion.start();
                 btnPausa.setText("Pausar");
             }
         }
    
// TODO add your handling code here:
    }//GEN-LAST:event_btnPausaActionPerformed

    private void sliderVelocidadStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderVelocidadStateChanged
                                            
      int valor = sliderVelocidad.getValue();
        if (timerAnimacion != null) {
            timerAnimacion.setDelay(valor);
        }
        
        if (lblVelocidad != null) {
            if (valor <= 50) lblVelocidad.setText("Velocidad: Muy Rápida (x4)");
            else if (valor <= 250) lblVelocidad.setText("Velocidad: Rápida (x2)");
            else if (valor <= 500) lblVelocidad.setText("Velocidad: Normal (x1)");
            else lblVelocidad.setText("Velocidad: Lenta (x0.5)");
        }
    
// TODO add your handling code here:
    }//GEN-LAST:event_sliderVelocidadStateChanged

    private void menuExportarTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExportarTxtActionPerformed
                                             
   javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Simulación");
        
        if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoDestino = fileChooser.getSelectedFile();
            if (!archivoDestino.getName().toLowerCase().endsWith(".txt")) {
                archivoDestino = new java.io.File(archivoDestino.getParentFile(), archivoDestino.getName() + ".txt");
            }
            
            try (java.io.FileWriter escritor = new java.io.FileWriter(archivoDestino)) {
                escritor.write("========================================\n");
                escritor.write("   REPORTE DE SIMULACIÓN DE DISCO\n");
                escritor.write("========================================\n\n");
                escritor.write("Política utilizada: " + comboPoliticas.getSelectedItem().toString() + "\n");
                if (lblEstadisticas != null) escritor.write(lblEstadisticas.getText() + "\n\n");
                escritor.write("--- REGISTRO DE EVENTOS ---\n");
                escritor.write(txtLog.getText()); 
                javax.swing.JOptionPane.showMessageDialog(this, "Reporte guardado exitosamente en:\n" + archivoDestino.getAbsolutePath());
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    
            // TODO add your handling code here:
    }//GEN-LAST:event_menuExportarTxtActionPerformed

    private void menuExportarCsvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExportarCsvActionPerformed
                                            
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Exportar Tabla a Excel (CSV)");
        
        if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoDestino = fileChooser.getSelectedFile();
            if (!archivoDestino.getName().toLowerCase().endsWith(".csv")) {
                archivoDestino = new java.io.File(archivoDestino.getParentFile(), archivoDestino.getName() + ".csv");
            }
            
            try (java.io.PrintWriter escritor = new java.io.PrintWriter(archivoDestino)) {
                javax.swing.table.TableModel modelo = jTable1.getModel();
                int columnas = modelo.getColumnCount();
                int filas = modelo.getRowCount();
                
                for (int i = 0; i < columnas; i++) {
                    escritor.print(modelo.getColumnName(i));
                    if (i < columnas - 1) escritor.print(","); 
                }
                escritor.println(); 
                
                for (int i = 0; i < filas; i++) {
                    for (int j = 0; j < columnas; j++) {
                        Object valor = modelo.getValueAt(i, j);
                        escritor.print(valor != null ? valor.toString() : "");
                        if (j < columnas - 1) escritor.print(",");
                    }
                    escritor.println(); 
                }
                javax.swing.JOptionPane.showMessageDialog(this, "Tabla exportada exitosamente a Excel en:\n" + archivoDestino.getAbsolutePath());
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
            // TODO add your handling code here:
    }//GEN-LAST:event_menuExportarCsvActionPerformed

    // Este método calcula paso a paso por dónde pasará el cabezal
  private void construirRuta(int destino) {
        if (posicionCabezal < destino) {
            for (int i = posicionCabezal + 1; i <= destino; i++) {
                rutaCompleta.add(i); // Asumiendo que tu LinkedList tiene el método .add()
            }
        } else if (posicionCabezal > destino) {
            for (int i = posicionCabezal - 1; i >= destino; i--) {
                rutaCompleta.add(i);
            }
        }
        posicionCabezal = destino; 
    }

    // =======================================================
    // MÉTODO DE ORDENAMIENTO MANUAL (CERO COLLECTIONS.SORT)
    // =======================================================
    private OSModels.DiskRequest[] convertirAArreglo(DataStructures.LinkedList<OSModels.DiskRequest> lista) {
        OSModels.DiskRequest[] arr = new OSModels.DiskRequest[lista.getSize()];
        for(int i = 0; i < lista.getSize(); i++) arr[i] = lista.get(i);
        return arr;
    }

    private void ordenarArregloPeticiones(OSModels.DiskRequest[] arr, boolean ascendente) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                boolean condicion = ascendente ? (arr[j].getBlockId() > arr[j + 1].getBlockId())
                                               : (arr[j].getBlockId() < arr[j + 1].getBlockId());
                if (condicion) {
                    OSModels.DiskRequest temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
    
    // =======================================================
    //        LÓGICA REAL DEL PLANIFICADOR 100% LEGAL
    // =======================================================
    private void procesarPlanificador() {
        if (colaPeticiones.getSize() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "La cola de peticiones está vacía.");
            return;
        }

        final String politica = comboPoliticas.getSelectedItem().toString(); 
        int movimientosTotales = 0;
        
        agregarLog("--- INICIANDO PLANIFICADOR: " + politica + " ---");
        agregarLog("Cabezal inicia en el bloque: " + posicionCabezal);

        // Limpiamos la lista instanciando una nueva propia tuya
        rutaCompleta = new DataStructures.LinkedList<>(); 

        if (politica.equals("FIFO")) {
            for (int i = 0; i < colaPeticiones.getSize(); i++) {
                OSModels.DiskRequest peticion = colaPeticiones.get(i);
                int distancia = Math.abs(posicionCabezal - peticion.getBlockId());
                movimientosTotales += distancia;
                agregarLog("-> [FIFO] Leyendo '" + peticion.getFileName() + "' en bloque " + peticion.getBlockId() + " (Mov: " + distancia + ")");
                construirRuta(peticion.getBlockId()); 
            }
        } 
        
        else if (politica.equals("SSTF")) {
            // Lógica sin usar .remove() sobre colecciones
            int n = colaPeticiones.getSize();
            boolean[] procesado = new boolean[n];
            int peticionesRestantes = n;

            while (peticionesRestantes > 0) {
                int indiceMasCercano = -1;
                int distanciaMinima = Integer.MAX_VALUE;

                for (int i = 0; i < n; i++) {
                    if (!procesado[i]) {
                        int distancia = Math.abs(posicionCabezal - colaPeticiones.get(i).getBlockId());
                        if (distancia < distanciaMinima) {
                            distanciaMinima = distancia;
                            indiceMasCercano = i;
                        }
                    }
                }
                procesado[indiceMasCercano] = true;
                peticionesRestantes--;
                
                OSModels.DiskRequest peticionElegida = colaPeticiones.get(indiceMasCercano);
                movimientosTotales += distanciaMinima;
                agregarLog("-> [SSTF] Leyendo '" + peticionElegida.getFileName() + "' en bloque " + peticionElegida.getBlockId() + " (Mov: " + distanciaMinima + ")");
                construirRuta(peticionElegida.getBlockId()); 
            }
        } 
        
        else if (politica.equals("SCAN")) {
            int limiteDisco = disk.getBlocks().length - 1; 
            DataStructures.LinkedList<OSModels.DiskRequest> derecha = new DataStructures.LinkedList<>();
            DataStructures.LinkedList<OSModels.DiskRequest> izquierda = new DataStructures.LinkedList<>();

            for (int i = 0; i < colaPeticiones.getSize(); i++) {
                OSModels.DiskRequest req = colaPeticiones.get(i);
                if (req.getBlockId() >= posicionCabezal) derecha.add(req);
                else izquierda.add(req);
            }
            
            // Usamos nuestro ordenamiento legal
            OSModels.DiskRequest[] arrDerecha = convertirAArreglo(derecha);
            OSModels.DiskRequest[] arrIzquierda = convertirAArreglo(izquierda);

            ordenarArregloPeticiones(arrDerecha, true);  // Ascendente
            ordenarArregloPeticiones(arrIzquierda, false); // Descendente

            for (int i = 0; i < arrDerecha.length; i++) {
                OSModels.DiskRequest req = arrDerecha[i];
                movimientosTotales += Math.abs(posicionCabezal - req.getBlockId());
                agregarLog("-> [SCAN-Sube] Leyendo bloque " + req.getBlockId());
                construirRuta(req.getBlockId());
            }
            if (arrIzquierda.length > 0) {
                movimientosTotales += Math.abs(posicionCabezal - limiteDisco);
                agregarLog("-> [SCAN] Toca el fondo del disco (Bloque " + limiteDisco + ")");
                construirRuta(limiteDisco);

                for (int i = 0; i < arrIzquierda.length; i++) {
                    OSModels.DiskRequest req = arrIzquierda[i];
                    movimientosTotales += Math.abs(posicionCabezal - req.getBlockId());
                    agregarLog("-> [SCAN-Baja] Leyendo bloque " + req.getBlockId());
                    construirRuta(req.getBlockId());
                }
            }
        } 
        
        else if (politica.equals("C-SCAN")) {
            int limiteDisco = disk.getBlocks().length - 1; 
            DataStructures.LinkedList<OSModels.DiskRequest> derecha = new DataStructures.LinkedList<>();
            DataStructures.LinkedList<OSModels.DiskRequest> izquierda = new DataStructures.LinkedList<>();

            for (int i = 0; i < colaPeticiones.getSize(); i++) {
                OSModels.DiskRequest req = colaPeticiones.get(i);
                if (req.getBlockId() >= posicionCabezal) derecha.add(req);
                else izquierda.add(req);
            }
            
            OSModels.DiskRequest[] arrDerecha = convertirAArreglo(derecha);
            OSModels.DiskRequest[] arrIzquierda = convertirAArreglo(izquierda);

            ordenarArregloPeticiones(arrDerecha, true);   // Ascendente
            ordenarArregloPeticiones(arrIzquierda, true);  // Ascendente (¡Vuelve a subir!)

            for (int i = 0; i < arrDerecha.length; i++) {
                OSModels.DiskRequest req = arrDerecha[i];
                movimientosTotales += Math.abs(posicionCabezal - req.getBlockId());
                agregarLog("-> [C-SCAN] Leyendo bloque " + req.getBlockId());
                construirRuta(req.getBlockId());
            }
            if (arrIzquierda.length > 0) {
                movimientosTotales += Math.abs(posicionCabezal - limiteDisco);
                construirRuta(limiteDisco);
                movimientosTotales += limiteDisco; 
                agregarLog("-> [C-SCAN] Salta al inicio del disco (Bloque 0)");
                construirRuta(0); // Regresa rápido al inicio
                
                for (int i = 0; i < arrIzquierda.length; i++) {
                    OSModels.DiskRequest req = arrIzquierda[i];
                    movimientosTotales += Math.abs(posicionCabezal - req.getBlockId());
                    agregarLog("-> [C-SCAN] Leyendo bloque " + req.getBlockId());
                    construirRuta(req.getBlockId());
                }
            }
        }

        // ==========================================
        // RESUMEN Y ANIMACIÓN (EL TIMER)
        // ==========================================
        agregarLog("TOTAL DE BLOQUES RECORRIDOS: " + movimientosTotales);
        
        double promedio = (double) movimientosTotales / colaPeticiones.getSize();
        lblEstadisticas.setText(String.format("Tiempo Promedio: %.2f blq/pet", promedio));
        
        colaPeticiones = new DataStructures.LinkedList<>(); 
        actualizarColaVisual();
        
        pasoActual = 0;
        if (timerAnimacion != null && timerAnimacion.isRunning()) { timerAnimacion.stop(); }

        timerAnimacion = new javax.swing.Timer(sliderVelocidad.getValue(), new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (pasoActual < rutaCompleta.getSize()) { // CORREGIDO PARA TU LINKEDLIST
                    posicionCabezal = rutaCompleta.get(pasoActual); 
                    lblCabeza.setText("Cabeza: " + posicionCabezal);
                    actualizarDiscoVisual(); 
                    pasoActual++;
                } else {
                    timerAnimacion.stop();
                    javax.swing.JOptionPane.showMessageDialog(null, "¡Planificación terminada!");
                }
            }
        });
        timerAnimacion.start();
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
    private javax.swing.JButton btnLeer;
    private javax.swing.JButton btnPausa;
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
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
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
    private javax.swing.JLabel lblEstadisticas;
    private javax.swing.JLabel lblVelocidad;
    private javax.swing.JMenuItem menuExportarCsv;
    private javax.swing.JMenuItem menuExportarTxt;
    private javax.swing.JPanel panelDiscoVirtual;
    private javax.swing.JRadioButton radioAdmin;
    private javax.swing.JRadioButton radioUsuario;
    private javax.swing.JSlider sliderVelocidad;
    private javax.swing.JScrollPane tablaAsignacion;
    private javax.swing.JTextArea txtCola;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
}
