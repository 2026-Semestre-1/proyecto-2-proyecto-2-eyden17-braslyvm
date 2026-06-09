/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package UI;

import CPU.Dispatcher;
import CPU.CPU;
import CPU.BCP;
import Memoria.Memoria;
import Memoria.Disco;
import Parser.Parser;
import Algorithms.FCFS;
import Algorithms.SJF;
import Algorithms.SRT;
import Algorithms.RR;
import Algorithms.SRR;
import Algorithms.HRRN;
import Algorithms.Lottery;
import Settings.Constants;

/**
 *
 * @author braslyvm
 */
public class Interfaz extends javax.swing.JPanel {
    private Memoria memoria;
    private Disco disco;
    private CPU cpu;
    private java.util.List<CPU> cpus = new java.util.ArrayList<>();
    private int cpuSeleccionado = 0;
    
    // Índice rotatorio para selección Round-Robin / SRR en la interfaz
    private Dispatcher dispatcher;
    private Parser parser;
    private boolean sistemaIniciado = false;
    private String algoritmoEjecucionActual = "";
    private int quantumEjecucionActual = -1;
    private FCFS fcfsRuntime = new FCFS();
    private SJF sjfRuntime = new SJF();
    private SRT srtRuntime = new SRT();
    private RR rrRuntime = null;
    private SRR srrRuntime = null;
    private HRRN hrrnRuntime = new HRRN();
    private Lottery lotteryRuntime = null;

    private static final int DEFAULT_MEMORIA = Constants.DEFAULT_MEMORIA;
    private static final int DEFAULT_VIRTUAL = Constants.DEFAULT_VIRTUAL;
    private static final int DEFAULT_DISCO = Constants.DEFAULT_DISCO;
    private static final int DEFAULT_CPU_COUNT = Constants.DEFAULT_CPU_COUNT;
    private static final String DEFAULT_SETTINGS_PATH = Constants.DEFAULT_SETTINGS_PATH;

    // Estrategia por defecto. Valores aceptados:
    // "Default", "Best_Fit", "Pagination", "Partition_Equal", "Partition_Different".
    private static final String DEFAULT_STRATEGY = Constants.DEFAULT_STRATEGY;

    // Para Pagination, este valor representa el tamaño de página.
    private static final int DEFAULT_PAGE_SIZE = Constants.DEFAULT_PAGE_SIZE;

    // Para Partition_Equal, este valor representa la cantidad de particiones.
    private static final int DEFAULT_COUNT_PARTITIONS = Constants.DEFAULT_COUNT_PARTITIONS;

    // Para Partition_Different, este arreglo representa el patrón de particiones.
    private static final int[] DEFAULT_PARTITION_SIZES = Constants.DEFAULT_PARTITION_SIZES;
    
    private static final int TAMANO_BCP = Constants.TAMANO_BCP;
    private static final java.awt.Color COLOR_EJECUCION = Constants.COLOR_EJECUCION;
    private static final java.awt.Color COLOR_INSTRUCCION = Constants.COLOR_INSTRUCCION;
    private static final java.awt.Color COLOR_TEXTO_BASE = Constants.COLOR_TEXTO_BASE;
    private static final java.awt.Color COLOR_TOAST = Constants.COLOR_TOAST;
    private static final String[] CAMPOS_BCP_KERNEL = Constants.CAMPOS_BCP_KERNEL;

    // Colores para visualizar marcos, particiones y bloques.
    // Se usan colores un poco más fuertes para que se noten en la tabla.
    private static final java.awt.Color[] COLORES_BLOQUES = Constants.COLORES_BLOQUES;

    private static final java.awt.Color[] COLORES_BCP = Constants.COLORES_BCP;

    private static final java.awt.Color COLOR_HUECO_LIBRE = Constants.COLOR_HUECO_LIBRE;
    private static final int TIEMPO_ESPERA_MS = Constants.TIEMPO_ESPERA_MS;
    private int tiempoGlobal = 0;
    private final java.util.Map<String, java.awt.Color> coloresPorBCP = new java.util.LinkedHashMap<>();
    private javax.swing.Popup toastPopup;
    private javax.swing.Timer toastTimer;

    // Componentes para seleccionar el algoritmo de planificación.
    private javax.swing.JLabel lblAlgoritmo;
    private javax.swing.JComboBox<String> cmbAlgoritmo;
    private javax.swing.JLabel lblQuantum;
    private javax.swing.JSpinner spnQuantum;
    // Componentes para seleccionar cantidad de CPUs.
    private javax.swing.JLabel lblCantidadCpus;
    private javax.swing.JComboBox<String> cmbCantidadCpus;

    // Copia base de los procesos cargados.
    // Esta lista NO se modifica durante las ejecuciones de algoritmos.
    private java.util.List<BCP> procesosBasePlanificacion = new java.util.ArrayList<>();

    // Historial acumulativo de resultados por algoritmo.
    private java.util.List<ResultadoEstadistica> historialEstadisticas = new java.util.ArrayList<>();
    private int contadorEjecucionesEstadisticas = 0;
    private String ultimaFirmaEstadisticas = "";
    private final java.util.Set<String> firmasEstadisticasGuardadas = new java.util.LinkedHashSet<>();

    // Archivos base cargados. Se usan para reconstruir el sistema al cambiar algoritmo.
    private java.util.List<java.io.File> archivosBasePlanificacion = new java.util.ArrayList<>();
    private java.util.Map<String, Integer> tiemposLlegadaPorArchivo = new java.util.LinkedHashMap<>();
    
    private int configMemoria = DEFAULT_MEMORIA;
    private int configVirtual = DEFAULT_VIRTUAL;
    private int configDisco = DEFAULT_DISCO;
    private String configStrategy = DEFAULT_STRATEGY;
    private int configPageSize = DEFAULT_PAGE_SIZE;
    private int configCountPartitions = DEFAULT_COUNT_PARTITIONS;
    private int[] configPartitionSizes = java.util.Arrays.copyOf(DEFAULT_PARTITION_SIZES, DEFAULT_PARTITION_SIZES.length);

    private boolean ejecucionEnCurso = false;

    /**
     * Inicializa la interfaz
     */
    public Interfaz() {
        initComponents();

        if (java.beans.Beans.isDesignTime()) {
            return;
        }

        configurarPanelSelectorCpu();

        terminalInput.setEnabled(false);
        terminalInput.setText("");

        btnEnviar.setVisible(false);
        btnEnviar.setEnabled(false);

        terminalArea.setText("");

        aplicarEstiloVisual();
        aplicarRenderersDeEjecucion();
        actualizarVisibilidadQuantum();
        cargarConfiguracionInicial();
        actualizarSelectorCpu();
    }
    /**
     * Aplica los coloresde los procesos que estan en ejecución a las tablas de la interfaz.
     */
    private void aplicarRenderersDeEjecucion() {
        MemoriaRenderer memoriaRenderer = new MemoriaRenderer();
        MemoriaVirtualRenderer memoriaVirtualRenderer = new MemoriaVirtualRenderer();
        ProcesosRenderer procesosRenderer = new ProcesosRenderer();
        DiscoRenderer discoRenderer = new DiscoRenderer();

        // Se asigna a Object, String e Integer para evitar que JTable use
        // renderers por defecto según el tipo de dato de la columna.
        tablaMemoria.setDefaultRenderer(Object.class, memoriaRenderer);
        tablaMemoria.setDefaultRenderer(String.class, memoriaRenderer);
        tablaMemoria.setDefaultRenderer(Integer.class, memoriaRenderer);

        tablaMemoriaVirtual.setDefaultRenderer(Object.class, memoriaVirtualRenderer);
        tablaMemoriaVirtual.setDefaultRenderer(String.class, memoriaVirtualRenderer);
        tablaMemoriaVirtual.setDefaultRenderer(Integer.class, memoriaVirtualRenderer);

        tablaProcesos.setDefaultRenderer(Object.class, procesosRenderer);
        tablaProcesos.setDefaultRenderer(String.class, procesosRenderer);

        tablaDisco.setDefaultRenderer(Object.class, discoRenderer);
        tablaDisco.setDefaultRenderer(String.class, discoRenderer);
        tablaDisco.setDefaultRenderer(Integer.class, discoRenderer);
    }
    
    /**
     * Aplica un estilo visual a los componentes de la interfaz.
     */
    private void aplicarEstiloVisual() {
        java.awt.Color fondo = new java.awt.Color(245, 247, 250);
        java.awt.Color panel = java.awt.Color.WHITE;
        java.awt.Color borde = new java.awt.Color(218, 226, 236);
        java.awt.Color texto = new java.awt.Color(35, 49, 66);

        setBackground(fondo);
        setOpaque(true);
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(224, 229, 238), 1));

        javax.swing.JButton[] botones = {
            btnCargarArchivos, btnEjecutar, btnPasoPaso,
            btnLimpiar, btnEstadisticas, btnCargarConfig,
            btnCpuAnterior, btnCpuSiguiente
        };

        for (javax.swing.JButton b : botones) {
            b.setFocusPainted(false);
            b.setBackground(java.awt.Color.WHITE);
            b.setForeground(texto);
            b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(borde),
                    javax.swing.BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        }

        btnCargarArchivos.setText("↥  Cargar Archivos");
        btnEjecutar.setText("▷  Ejecutar");
        btnPasoPaso.setText("▻▻  Paso a Paso");
        btnLimpiar.setText("⌫  Limpiar");
        btnEstadisticas.setText("▥  Estadísticas");
        btnCargarConfig.setText("⚙  Configuración");

        jPanel1.setBackground(panel);
        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createTitledBorder(
                        javax.swing.BorderFactory.createLineBorder(borde), "BCP ACTUAL"),
                javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        javax.swing.JLabel[] etiquetas = {
            jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6,
            jLabel8, jLabel9, jLabel10, jLabel11, jLabel12, jLabel13,
            jLabel14, jLabel15, jLabel16, jLabel17, jLabel18, jLabel19,
            jLabel20, jLabel25, jLabel26
        };

        for (javax.swing.JLabel l : etiquetas) {
            l.setForeground(texto);
            l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        }

        javax.swing.JLabel[] valores = {
            lblBcpId, lblBcpNombre, lblBcpEstado, lblBcpBase, lblBcpLimite,
            lblBcpPila, lblBcpPrioridad, lblBcpSiguiente, lblBcpTiempoInicio,
            lblBcpTiempoEmpleado, lblBcpArchivos, lblBcpCpu,
            ac, ax, bx, cx, dx, al
        };

        for (javax.swing.JLabel l : valores) {
            l.setForeground(new java.awt.Color(22, 34, 51));
            l.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 13));
        }

        estilizarTabla(tablaMemoria);
        estilizarTabla(tablaMemoriaVirtual);
        estilizarTabla(tablaDisco);
        estilizarTabla(tablaProcesos);

        jScrollPane3.setBorder(tituloPanel("MEMORIA PRINCIPAL"));
        jScrollPane1.setBorder(tituloPanel("MEMORIA VIRTUAL"));
        jScrollPane5.setBorder(tituloPanel("DISCO / PROGRAMAS"));
        jScrollPane2.setBorder(tituloPanel("COLA DE PROCESOS"));
        jScrollPane6.setBorder(tituloPanel("CONSOLA"));

        panelSelectorCpu.setBackground(panel);
        lblCpuSeleccionado.setForeground(texto);
        lblCpuSeleccionado.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        terminalArea.setBackground(new java.awt.Color(11, 22, 28));
        terminalArea.setForeground(new java.awt.Color(198, 255, 221));
        terminalArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 14));
        terminalArea.setLineWrap(true);
        terminalArea.setWrapStyleWord(true);
        terminalArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));

        terminalInput.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 14));
        terminalInput.setBackground(java.awt.Color.WHITE);
        terminalInput.setForeground(texto);
        terminalInput.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(borde),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        terminalInput.setToolTipText("Entrada disponible solamente durante INT 09H. Presione ENTER para enviar.");

        btnEnviar.setVisible(false);
        btnEnviar.setEnabled(false);
    }

    private void configurarPanelSelectorCpu() {
        panelSelectorCpu.removeAll();

        panelSelectorCpu.setLayout(new java.awt.FlowLayout(
                java.awt.FlowLayout.CENTER,
                8,
                4
        ));

        panelSelectorCpu.add(btnCpuAnterior);
        panelSelectorCpu.add(lblCpuSeleccionado);
        panelSelectorCpu.add(btnCpuSiguiente);

        panelSelectorCpu.revalidate();
        panelSelectorCpu.repaint();
    }

    private javax.swing.border.Border tituloPanel(String titulo) {
        javax.swing.border.TitledBorder tb = javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(218, 226, 236)), titulo);
        tb.setTitleFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        tb.setTitleColor(new java.awt.Color(50, 66, 85));
        return tb;
    }

    private java.awt.Color obtenerColorBCP(String idProceso) {
        if (idProceso == null || idProceso.isBlank()) {
            return COLOR_TEXTO_BASE;
        }

        java.awt.Color color = coloresPorBCP.get(idProceso);

        if (color == null) {
            color = COLORES_BCP[coloresPorBCP.size() % COLORES_BCP.length];
            coloresPorBCP.put(idProceso, color);
        }

        return color;
    }

    private void mostrarToast(String mensaje) {
        mostrarToast(mensaje, COLOR_TOAST);
    }

    private void mostrarToast(String mensaje, java.awt.Color acento) {
        if (mensaje == null || mensaje.isBlank()) {
            return;
        }

        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> mostrarToast(mensaje, acento));
            return;
        }

        if (!isShowing()) {
            return;
        }

        if (toastPopup != null) {
            toastPopup.hide();
            toastPopup = null;
        }

        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }

        javax.swing.JLabel label = new javax.swing.JLabel(mensaje);
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        label.setForeground(java.awt.Color.WHITE);
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14));

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
        panel.setBackground(acento);
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255, 180)),
                javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        panel.add(label, java.awt.BorderLayout.CENTER);

        java.awt.Point location;

        try {
            location = getLocationOnScreen();
        } catch (java.awt.IllegalComponentStateException e) {
            return;
        }

        java.awt.Dimension size = panel.getPreferredSize();
        int x = location.x + Math.max(12, (getWidth() - size.width) / 2);
        int y = location.y + Math.max(12, (getHeight() - size.height) / 2);

        toastPopup = javax.swing.PopupFactory.getSharedInstance().getPopup(this, panel, x, y);
        toastPopup.show();

        toastTimer = new javax.swing.Timer(1800, e -> {
            if (toastPopup != null) {
                toastPopup.hide();
                toastPopup = null;
            }
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private void asignarTiempoInicioSiEsPrimeraVez(BCP bcp) {
        if (bcp == null) return;

        if (bcp.getTiempoEmpleado() == 0 && bcp.getTiempoInicio() == 0) {
            bcp.setTiempoInicio(tiempoGlobal);
        }
    }

    private void estilizarTabla(javax.swing.JTable tabla) {
        tabla.setRowHeight(28);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tabla.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
        tabla.setForeground(COLOR_TEXTO_BASE);
        tabla.setSelectionBackground(new java.awt.Color(220, 234, 255));
        tabla.setSelectionForeground(new java.awt.Color(20, 40, 80));
        tabla.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new java.awt.Color(238, 242, 247));
        tabla.getTableHeader().setForeground(new java.awt.Color(50, 66, 85));
        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(246, 248, 251));
                }
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }
    /**
     * Actualiza la tabla de procesos con la información actualizada.
     */
    public void actualizarTablaProcesos() {
        if (dispatcher == null) return;
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) tablaProcesos.getModel();
        model.setRowCount(0);
        for (BCP bcp : dispatcher.obtenerColaProcesos()) {
            String cpuAsignado = bcp.getCpuAsignado() >= 0
                    ? "CPU " + bcp.getCpuAsignado()
                    : "-";
            model.addRow(new Object[]{bcp.getIdProceso(), bcp.getEstado(), cpuAsignado});
        }
        tablaProcesos.repaint();
    }
      public void actualizarBCP() {
        BCP bcp = obtenerBCPVisible();
        if (bcp == null) { limpiarBCP(); return; }

        lblBcpId.setText(bcp.getIdProceso());
        lblBcpNombre.setText(bcp.getNombreProceso());
        lblBcpEstado.setText(bcp.getEstado());
        lblBcpBase.setText(String.valueOf(bcp.getBase()));
        lblBcpLimite.setText(String.valueOf(bcp.getLimite()));
        lblBcpPrioridad.setText(String.valueOf(bcp.getPrioridad()));
        lblBcpSiguiente.setText(bcp.getSiguienteBCP() != null
                ? bcp.getSiguienteBCP().getIdProceso() : "null");
        jLabel10.setText("PC: " + bcp.getPc());
        jLabel11.setText("IR: " + bcp.getIr());
        lblBcpTiempoInicio.setText(String.valueOf(bcp.getTiempoInicio()));
        lblBcpTiempoEmpleado.setText(String.valueOf(bcp.getTiempoEmpleado()));
        lblBcpArchivos.setText(bcp.getArchivosAbiertos().isEmpty()
                ? "ninguno" : bcp.getArchivosAbiertos().toString());
        lblBcpCpu.setText(String.format("%02XH", bcp.getAh()));
        ac.setText(String.valueOf(bcp.getAc()));
        ax.setText(String.valueOf(bcp.getAx()));
        bx.setText(String.valueOf(bcp.getBx()));
        cx.setText(String.valueOf(bcp.getCx()));
        dx.setText(String.valueOf(bcp.getDx()));
        al.setText(String.valueOf(bcp.getAl()));

        // Pila
        int[] pila = bcp.getPila();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i <= bcp.getPuntero_pila(); i++) {
            if (i > 0) sb.append(",");
            sb.append(pila[i]);
        }
        sb.append("]");
        aplicarColorBCPActual(bcp);
        lblBcpPila.setText(bcp.getPuntero_pila() < 0 ? "vacía" : sb.toString());
    }
    private void aplicarColorBCPActual(BCP bcp) {
        java.awt.Color color = obtenerColorBCP(bcp != null ? bcp.getIdProceso() : "");

        javax.swing.JLabel[] valores = {
            lblBcpId, lblBcpNombre, lblBcpEstado, lblBcpBase, lblBcpLimite,
            lblBcpPila, lblBcpPrioridad, lblBcpSiguiente, lblBcpTiempoInicio,
            lblBcpTiempoEmpleado, lblBcpArchivos, lblBcpCpu,
            ac, ax, bx, cx, dx, al, jLabel10, jLabel11
        };

        for (javax.swing.JLabel label : valores) {
            label.setForeground(color);
            label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
        }
    }

    private void restaurarColorBCPActual() {
        javax.swing.JLabel[] valores = {
            lblBcpId, lblBcpNombre, lblBcpEstado, lblBcpBase, lblBcpLimite,
            lblBcpPila, lblBcpPrioridad, lblBcpSiguiente, lblBcpTiempoInicio,
            lblBcpTiempoEmpleado, lblBcpArchivos, lblBcpCpu,
            ac, ax, bx, cx, dx, al, jLabel10, jLabel11
        };

        for (javax.swing.JLabel label : valores) {
            label.setForeground(new java.awt.Color(22, 34, 51));
            label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
        }
    }

    private BCP obtenerBCPVisible() {
        CPU cpuActual = obtenerCpuSeleccionada();

        if (cpuActual != null && cpuActual.getBcp() != null) {
            return cpuActual.getBcp();
        }

        if (hayCpuActiva()) {
            return null;
        }

        if (cpu != null && cpu.getBcp() != null) {
            return cpu.getBcp();
        }

        return (memoria != null) ? memoria.obtenerPrimerBCP() : null;
    }

    private CPU obtenerCpuSeleccionada() {
        if (cpus == null || cpus.isEmpty()) {
            return null;
        }

        if (cpuSeleccionado < 0) {
            cpuSeleccionado = 0;
        }

        if (cpuSeleccionado >= cpus.size()) {
            cpuSeleccionado = cpus.size() - 1;
        }

        return cpus.get(cpuSeleccionado);
    }

    /* */
    private String getIdProcesoActual() {
        for (CPU cpuActual : cpus) {
            if (cpuActual != null && cpuActual.getBcp() != null) {
                return cpuActual.getBcp().getIdProceso();
            }
        }

        if (cpu != null && cpu.getBcp() != null) {
            return cpu.getBcp().getIdProceso();
        }

        if (memoria != null) {
            BCP bcp = memoria.obtenerPrimerBCP();

            if (bcp != null) {
                return bcp.getIdProceso();
            }
        }

        return "";
    }

    private boolean esProcesoActivoEnAlgunaCPU(String idProceso) {
        if (idProceso == null || idProceso.isBlank()) {
            return false;
        }

        for (CPU cpuActual : cpus) {
            if (cpuActual != null
                    && cpuActual.getBcp() != null
                    && idProceso.equals(cpuActual.getBcp().getIdProceso())) {
                return true;
            }
        }

        return false;
    }

    private boolean esInstruccionActualEnAlgunaCPU(int posicion) {
        for (CPU cpuActual : cpus) {
            if (cpuActual != null
                    && cpuActual.getBcp() != null
                    && posicion == obtenerDireccionMarcada(cpuActual.getBcp())) {
                return true;
            }
        }

        if (cpu != null
                && cpu.getBcp() != null
                && posicion == obtenerDireccionMarcada(cpu.getBcp())) {
            return true;
        }

        return false;
    }

    private int obtenerDireccionMarcada(BCP bcp) {
        if (bcp == null || memoria == null) {
            return -1;
        }

        return memoria.obtenerDireccionFisicaMarcada(bcp);
    }

    private String obtenerIdProcesoPorPosicionMemoria(int posicion) {
        if (memoria == null || posicion < 0) {
            return "";
        }

        if (posicion < memoria.getInicioUsuario()) {
            int inicioBCP = (posicion / TAMANO_BCP) * TAMANO_BCP;
            String[] mem = memoria.getMemoria();

            if (inicioBCP >= 0 && inicioBCP < memoria.getPunteroSO() && inicioBCP < mem.length) {
                String idProceso = mem[inicioBCP];
                return idProceso == null ? "" : idProceso;
            }

            return "";
        }

        for (BCP bcp : memoria.obtenerTodosBCPsEnMemoria()) {
            if (bcp == null) {
                continue;
            }

            if (posicion >= bcp.getBase() && posicion <= bcp.getLimite()) {
                return bcp.getIdProceso();
            }
        }

        return "";
    }

    private String formatearValorMemoriaKernel(int posicion, String valor) {
        if (memoria == null || posicion < 0 || posicion >= memoria.getPunteroSO()) {
            return valor;
        }

        String[] mem = memoria.getMemoria();
        int inicioBCP = (posicion / TAMANO_BCP) * TAMANO_BCP;
        int offset = posicion - inicioBCP;

        if (inicioBCP < 0 || inicioBCP >= mem.length || offset < 0 || offset >= CAMPOS_BCP_KERNEL.length) {
            return valor;
        }

        String idProceso = mem[inicioBCP];

        if (idProceso == null || idProceso.isBlank()) {
            idProceso = "sin_id";
        }

        String valorVisible = (valor == null || valor.isBlank()) ? "(vacio)" : valor;
        return "BCP " + idProceso + " | " + CAMPOS_BCP_KERNEL[offset] + " = " + valorVisible;
    }
    /**
     * Verifica si la fila dada corresponde a un bloque de memoria ocupado por el proceso actualmente en ejecución
     * @param posicion
     * @return
     */
    private boolean esFilaBCPActualEnMemoria(int posicion) {
        if (memoria == null) {
            return false;
        }

        String idActual = getIdProcesoActual();

        if (idActual.isBlank()) {
            return false;
        }

        int cantidadBCP = memoria.getPunteroSO() / TAMANO_BCP;
        String[] mem = memoria.getMemoria();

        for (int i = 0; i < cantidadBCP; i++) {
            int inicio = i * TAMANO_BCP;
            int fin = inicio + TAMANO_BCP - 1;

            if (posicion >= inicio && posicion <= fin) {
                String idEnBloque = mem[inicio];
                return idActual.equals(idEnBloque) || esProcesoActivoEnAlgunaCPU(idEnBloque);
            }
        }

        return false;
    }

    private void limpiarBCP() {
        lblBcpId.setText("—");         lblBcpNombre.setText("—");
        lblBcpEstado.setText("—");     lblBcpBase.setText("—");
        lblBcpLimite.setText("—");     lblBcpPila.setText("—");
        lblBcpPrioridad.setText("—");  lblBcpSiguiente.setText("—");
        lblBcpTiempoInicio.setText("—"); lblBcpTiempoEmpleado.setText("—");
        lblBcpArchivos.setText("—");   lblBcpCpu.setText("—");
        jLabel10.setText("PC:");       jLabel11.setText("IR:");
        ac.setText("0"); ax.setText("0"); bx.setText("0");
        cx.setText("0"); dx.setText("0"); al.setText("0");
        restaurarColorBCPActual();
    }

    public void actualizarTablaMemoria() {
        if (memoria == null) return;

        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) tablaMemoria.getModel();
        model.setRowCount(0);

        String[] mem = memoria.getMemoria();

        // Se recorre toda la memoria porque con Best Fit, particiones o paginación
        // el punteroUsuario puede no representar hasta dónde hay datos cargados.
        int inicioUsuario = memoria.getInicioUsuario();
        int punteroSO = memoria.getPunteroSO();
        boolean kernelVacioOculto = false;

        for (int i = 0; i < mem.length; i++) {
            String val = (mem[i] != null && !mem[i].isEmpty()) ? mem[i] : "";

            if (i < inicioUsuario) {
                if (i < punteroSO || !val.isBlank()) {
                    model.addRow(new Object[]{i, formatearValorMemoriaKernel(i, val)});
                } else if (!kernelVacioOculto) {
                    model.addRow(new Object[]{"...", "Kernel reservado vacio oculto"});
                    kernelVacioOculto = true;
                }

                continue;
            }

            model.addRow(new Object[]{i, formatearValorMemoriaKernel(i, val)});
        }

        tablaMemoria.revalidate();
        tablaMemoria.repaint();
    }

    public void actualizarTablaVirtual() {
        if (disco == null) return;

        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) tablaMemoriaVirtual.getModel();
        model.setRowCount(0);

        String[] mem = disco.getMemoriaVirtual();

        // Se recorre toda la memoria virtual porque en paginación se escribe
        // por marcos virtuales y no necesariamente se mueve el punteroVirtual.
        for (int i = 0; i < mem.length; i++) {
            String val = (mem[i] != null && !mem[i].isEmpty()) ? mem[i] : "";
            model.addRow(new Object[]{i, val});
        }

        tablaMemoriaVirtual.revalidate();
        tablaMemoriaVirtual.repaint();
    }

    public void actualizarTablaDisco() {
        if (disco == null) return;
        javax.swing.table.DefaultTableModel model =
                (javax.swing.table.DefaultTableModel) tablaDisco.getModel();
        model.setRowCount(0);
        String[] mem = disco.getMemoria();
        String[] txt = disco.getTextoMemoria();
        for (int i = 0; i < disco.getPunteroUltimo(); i++) {
            String etiqueta = (txt[i] != null) ? txt[i] : "";
            String valor    = (mem[i] != null) ? mem[i] : "";
            model.addRow(new Object[]{i, etiqueta + valor});
        }
        tablaDisco.repaint();
    }


    private void limpiarTabla(javax.swing.JTable tabla) {
        ((javax.swing.table.DefaultTableModel) tabla.getModel()).setRowCount(0);
    }
        public void imprimirTerminal(String mensaje) {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> imprimirTerminal(mensaje));
            return;
        }

        String texto = mensaje == null ? "" : mensaje;
        terminalArea.append(">> " + texto + "\n");
        terminalArea.setCaretPosition(terminalArea.getDocument().getLength());
    }
    public void activarEntrada() {
        terminalInput.setEnabled(true);
        terminalInput.setText("");
        terminalInput.requestFocus();
    }

    public void desactivarEntrada() {
        terminalInput.setEnabled(false);
        terminalInput.setText("");
    }

    private void cambiarCpuSeleccionado(int direccion) {
        int total = (cpus != null && !cpus.isEmpty()) ? cpus.size() : DEFAULT_CPU_COUNT;

        if (total <= 0) {
            cpuSeleccionado = 0;
        } else {
            cpuSeleccionado = Math.floorMod(cpuSeleccionado + direccion, total);
        }

        actualizarSelectorCpu();
        actualizarBCP();
        actualizarTablaProcesos();
        actualizarTablaMemoria();
        actualizarTablaDisco();
    }

    private void actualizarSelectorCpu() {
        if (lblCpuSeleccionado != null) {
            int total = (cpus != null && !cpus.isEmpty()) ? cpus.size() : obtenerCantidadCpusSeleccionada();

            if (total <= 0) {
                lblCpuSeleccionado.setText("CPU -");
            } else {
                lblCpuSeleccionado.setText("CPU " + (cpuSeleccionado + 1) + " / " + total);
            }
        }
    }

    private void btnCpuAnteriorActionPerformed(java.awt.event.ActionEvent evt) {
        cambiarCpuSeleccionado(-1);
    }

    private void btnCpuSiguienteActionPerformed(java.awt.event.ActionEvent evt) {
        cambiarCpuSeleccionado(1);
    }

    private class ProcesosRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            java.awt.Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            String idActual = getIdProcesoActual();
            String idFila = String.valueOf(table.getValueAt(row, 0));
            boolean procesoActivo = (!idActual.isBlank() && idActual.equals(idFila))
                    || esProcesoActivoEnAlgunaCPU(idFila);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(246, 248, 251));

                if (procesoActivo) {
                    c.setBackground(COLOR_EJECUCION);
                }
            }

            c.setForeground(obtenerColorBCP(idFila));
            c.setFont(table.getFont().deriveFont(procesoActivo ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return c;
        }
    }

    /**
     * Retorna el color del bloque de memoria física según la estrategia activa.
     *
     * Pagination:
     *      Pinta por marcos físicos de tamaño pageSize.
     *
     * Partition_Equal / Partition_Different:
     *      Pinta por particiones.
     *
     * Best_Fit / Default:
     *      No pinta bloques especiales.
     */
    private java.awt.Color obtenerColorBloqueMemoriaFisica(int posicion) {
        if (memoria == null) {
            return null;
        }

        String strategy = memoria.getStrategy();

        if (strategy == null) {
            return null;
        }

        int inicioUsuario = memoria.getInicioUsuario();

        // No pintar zona del SO / BCP con bloques de usuario.
        if (posicion < inicioUsuario) {
            return null;
        }

        // ==============================
        // Paginación: pintar por marcos físicos.
        // ==============================
        if ("Pagination".equals(strategy) && memoria.getPagination_Strategy() != null) {
            int pageSize = memoria.getPagination_Strategy().getPageSize();

            if (pageSize <= 0) {
                return null;
            }

            int frame = (posicion - inicioUsuario) / pageSize;
            return COLORES_BLOQUES[frame % COLORES_BLOQUES.length];
        }

        // ==============================
        // Partición fija: pintar por particiones.
        // ==============================
        if (("Partition_Equal".equals(strategy) || "Partition_Different".equals(strategy))
                && memoria.getPartitio_Strategy() != null) {

            int[] starts = memoria.getPartitio_Strategy().getStart();
            int[] sizes = memoria.getPartitio_Strategy().getSize();

            for (int i = 0; i < starts.length; i++) {
                int start = starts[i];
                int end = start + sizes[i];

                if (posicion >= start && posicion < end) {
                    return COLORES_BLOQUES[i % COLORES_BLOQUES.length];
                }
            }
        }

        // ==============================
        // Best Fit: pintar bloques ocupados y huecos.
        // ==============================
        if ("Best_Fit".equals(strategy) && memoria.getBestFit_Strategy() != null) {

            int[] occupiedStart = memoria.getBestFit_Strategy().getOccupiedStart();
            int[] occupiedEnd = memoria.getBestFit_Strategy().getOccupiedEnd();
            int occupiedCount = memoria.getBestFit_Strategy().getOccupiedCount();

            // Caso correcto: BestFit sí registró los bloques ocupados.
            for (int i = 0; i < occupiedCount; i++) {
                int start = occupiedStart[i];
                int end = occupiedEnd[i];

                if (posicion >= start && posicion < end) {
                    return COLORES_BLOQUES[i % COLORES_BLOQUES.length];
                }
            }

            // Respaldo visual:
            // Si occupiedCount está en 0, normalmente significa que Parser todavía
            // está cargando instrucciones con cargarInstruccionesSiCabe() y no con
            // memoria.asignarProceso(). Aun así, pintamos usando las bases/límites
            // guardados en los BCP para que la interfaz muestre los procesos.
            if (occupiedCount == 0) {
                java.util.List<BCP> bcps = memoria.obtenerTodosBCPsEnMemoria();

                for (int i = 0; i < bcps.size(); i++) {
                    BCP bcp = bcps.get(i);

                    if (bcp == null) {
                        continue;
                    }

                    int base = bcp.getBase();
                    int limite = bcp.getLimite();

                    if (base >= inicioUsuario && limite >= base
                            && posicion >= base && posicion <= limite) {
                        return COLORES_BLOQUES[i % COLORES_BLOQUES.length];
                    }
                }
            }

            int[] holeStart = memoria.getBestFit_Strategy().getHoleStart();
            int[] holeEnd = memoria.getBestFit_Strategy().getHoleEnd();
            int holeCount = memoria.getBestFit_Strategy().getHoleCount();

            // Huecos libres en gris.
            for (int i = 0; i < holeCount; i++) {
                int start = holeStart[i];
                int end = holeEnd[i];

                if (posicion >= start && posicion < end) {
                    return COLOR_HUECO_LIBRE;
                }
            }
        }

        return null;
    }

    /**
     * Retorna el color del bloque de memoria virtual.
     *
     * Solo se pinta cuando la estrategia es Pagination, porque ahí la
     * memoria virtual también se divide en marcos virtuales del tamaño pageSize.
     */
    private java.awt.Color obtenerColorBloqueMemoriaVirtual(int posicion) {
        if (memoria == null) {
            return null;
        }

        if (!"Pagination".equals(memoria.getStrategy())) {
            return null;
        }

        if (memoria.getPagination_Strategy() == null) {
            return null;
        }

        int pageSize = memoria.getPagination_Strategy().getPageSize();

        if (pageSize <= 0) {
            return null;
        }

        int virtualFrame = posicion / pageSize;

        return COLORES_BLOQUES[virtualFrame % COLORES_BLOQUES.length];
    }

    private class MemoriaRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            java.awt.Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            int posicion = -1;

            try {
                posicion = Integer.parseInt(String.valueOf(table.getValueAt(row, 0)));
            } catch (NumberFormatException e) {
                posicion = -1;
            }

            boolean instruccionActual = esInstruccionActualEnAlgunaCPU(posicion);
            String idProcesoPosicion = obtenerIdProcesoPorPosicionMemoria(posicion);
            boolean filaBCP = memoria != null
                    && posicion >= 0
                    && posicion < memoria.getInicioUsuario()
                    && !idProcesoPosicion.isBlank();

            if (!isSelected) {
                java.awt.Color colorBloque = obtenerColorBloqueMemoriaFisica(posicion);

                if (colorBloque != null) {
                    c.setBackground(colorBloque);
                } else {
                    c.setBackground(row % 2 == 0
                            ? java.awt.Color.WHITE
                            : new java.awt.Color(246, 248, 251));
                }

                // Resalta el BCP actual encima del color de partición/marco.
                if (esFilaBCPActualEnMemoria(posicion)) {
                    c.setBackground(COLOR_EJECUCION);
                }

                // Resalta la instrucción actual encima de todo.
                // En paginación el PC es lógico, por eso no se compara con posición física.
                if (instruccionActual) {
                    c.setBackground(COLOR_INSTRUCCION);
                }
            }

            if (!idProcesoPosicion.isBlank()) {
                c.setForeground(obtenerColorBCP(idProcesoPosicion));
            } else {
                c.setForeground(table.getForeground());
            }

            if (instruccionActual || filaBCP) {
                c.setFont(table.getFont().deriveFont(java.awt.Font.BOLD));

                if (instruccionActual && column == 1) {
                    String texto = value == null ? "" : String.valueOf(value);
                    setText("> " + texto);
                }
            } else {
                c.setFont(table.getFont());
            }

            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return c;
        }
    }

    private class MemoriaVirtualRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            java.awt.Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            int posicion = -1;

            try {
                posicion = Integer.parseInt(String.valueOf(table.getValueAt(row, 0)));
            } catch (NumberFormatException e) {
                posicion = -1;
            }

            if (!isSelected) {
                java.awt.Color colorBloque = obtenerColorBloqueMemoriaVirtual(posicion);

                if (colorBloque != null) {
                    c.setBackground(colorBloque);
                } else {
                    c.setBackground(row % 2 == 0
                            ? java.awt.Color.WHITE
                            : new java.awt.Color(246, 248, 251));
                }
            }

            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return c;
        }
    }
    private boolean esFilaBCPActualEnDisco(int posicion) {
        if (disco == null) {
            return false;
        }

        String idActual = getIdProcesoActual();

        if (idActual.isBlank()) {
            return false;
        }

        String[] discoMem = disco.getMemoria();
        String valor = (posicion >= 0 && posicion < discoMem.length) ? discoMem[posicion] : null;

        return valor != null && valor.contains(idActual);
    }
    private class DiscoRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            java.awt.Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(246, 248, 251));

                int posicion = -1;

                try {
                    posicion = Integer.parseInt(String.valueOf(table.getValueAt(row, 0)));
                } catch (NumberFormatException e) {
                    posicion = -1;
                }

                if (esFilaBCPActualEnDisco(posicion)) {
                    c.setBackground(COLOR_EJECUCION);
                }
            }

            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return c;
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCargarArchivos = new javax.swing.JButton();
        btnEjecutar = new javax.swing.JButton();
        btnPasoPaso = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnEstadisticas = new javax.swing.JButton();
        btnCargarConfig = new javax.swing.JButton();
        lblAlgoritmo = new javax.swing.JLabel();
        cmbAlgoritmo = new javax.swing.JComboBox<>();
        lblQuantum = new javax.swing.JLabel();
        spnQuantum = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblBcpId = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblBcpNombre = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblBcpEstado = new javax.swing.JLabel();
        lblBcpBase = new javax.swing.JLabel();
        lblBcpLimite = new javax.swing.JLabel();
        lblBcpPila = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblBcpPrioridad = new javax.swing.JLabel();
        lblBcpSiguiente = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        ac = new javax.swing.JLabel();
        ax = new javax.swing.JLabel();
        bx = new javax.swing.JLabel();
        cx = new javax.swing.JLabel();
        dx = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        lblBcpTiempoInicio = new javax.swing.JLabel();
        lblBcpTiempoEmpleado = new javax.swing.JLabel();
        lblBcpArchivos = new javax.swing.JLabel();
        lblBcpCpu = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        al = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        panelSelectorCpu = new javax.swing.JPanel();
        btnCpuAnterior = new javax.swing.JButton();
        lblCpuSeleccionado = new javax.swing.JLabel();
        btnCpuSiguiente = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaProcesos = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaMemoria = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaMemoriaVirtual = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        tablaDisco = new javax.swing.JTable();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        terminalArea = new javax.swing.JTextArea();
        terminalInput = new javax.swing.JTextField();
        btnEnviar = new javax.swing.JButton();

        btnCargarArchivos.setText("↥  Cargar Archivos");
        btnCargarArchivos.addActionListener(this::btnCargarArchivosActionPerformed);

        btnEjecutar.setText("▷  Ejecutar");
        btnEjecutar.addActionListener(this::btnEjecutarActionPerformed);

        btnPasoPaso.setText("▻▻  Paso a Paso");
        btnPasoPaso.addActionListener(this::btnPasoPasoActionPerformed);

        btnLimpiar.setText("⌫  Limpiar");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);

        btnEstadisticas.setText("▥  Estadísticas");
        btnEstadisticas.addActionListener(this::btnEstadisticasActionPerformed);

        btnCargarConfig.setText("⚙  Configuración");
        btnCargarConfig.addActionListener(this::btnCargarConfigActionPerformed);

        lblAlgoritmo.setText("Algoritmo:");
        lblAlgoritmo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        cmbAlgoritmo.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"FCFS", "SJF", "SRT", "RR", "SRR", "HRRN", "Lottery"}
        ));
        cmbAlgoritmo.setSelectedItem("FCFS");
        cmbAlgoritmo.addActionListener(e -> {
            actualizarVisibilidadQuantum();
            resetPlanificadorEjecucion();
            prepararNuevaEjecucionDesdeArchivos("Algoritmo cambiado. Sistema reiniciado para nueva ejecución.");
        });

        lblQuantum.setText("Quantum:");
        lblQuantum.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        spnQuantum.setModel(new javax.swing.SpinnerNumberModel(
                Constants.DEFAULT_QUANTUM,
                1,
                Constants.MAX_QUANTUM,
                1
        ));
        spnQuantum.addChangeListener(e -> {
            resetPlanificadorEjecucion();
            prepararNuevaEjecucionDesdeArchivos("Quantum cambiado. Sistema reiniciado para nueva ejecución.");
        });

        lblCantidadCpus = new javax.swing.JLabel("CPUs:");
        lblCantidadCpus.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        cmbCantidadCpus = new javax.swing.JComboBox<>(Constants.CPU_OPTIONS);
        cmbCantidadCpus.setSelectedItem(String.valueOf(DEFAULT_CPU_COUNT));
        cmbCantidadCpus.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        cmbCantidadCpus.setPreferredSize(new java.awt.Dimension(60, 28));
        cmbCantidadCpus.addActionListener(e -> {
            if (!sistemaIniciado) return;
            resetPlanificadorEjecucion();
            prepararNuevaEjecucionDesdeArchivos("Cantidad de CPUs cambiada. Sistema reiniciado para nueva ejecución.");
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BCP ACTUAL"));
        jPanel1.setForeground(new java.awt.Color(35, 49, 66));

        jLabel1.setText("ID:");

        lblBcpId.setText("-");

        jLabel2.setText("Nombre:");

        lblBcpNombre.setText("-");

        jLabel3.setText("Estado:");

        jLabel4.setText("Base:");

        jLabel5.setText("Límite");

        jLabel6.setText("Pila:");

        lblBcpEstado.setText("-");

        lblBcpBase.setText("-");

        lblBcpLimite.setText("-");

        lblBcpPila.setText("-");

        jLabel8.setText("Prioridad:");

        jLabel9.setText("Siguiente BCP:");

        lblBcpPrioridad.setText("-");

        lblBcpSiguiente.setText("-");

        jLabel10.setText("PC:");

        jLabel11.setText("IR:");

        jLabel12.setText("AC");

        jLabel13.setText("AX");

        jLabel14.setText("BX");

        jLabel15.setText("CX");

        jLabel16.setText("DX");

        ac.setText("0");

        ax.setText("0");

        bx.setText("0");

        cx.setText("0");

        dx.setText("0");

        jLabel17.setText("T.Inicio:");

        jLabel18.setText("T.Empleado:");

        jLabel19.setText("Archivos:");

        jLabel20.setText("AH");

        lblBcpTiempoInicio.setText("0");

        lblBcpTiempoEmpleado.setText("0");

        lblBcpArchivos.setText("vacio");

        lblBcpCpu.setText("0");

        jLabel25.setText("AL:");

        al.setText("0");

        jLabel26.setText("AL");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblBcpId)
                                    .addComponent(lblBcpNombre)
                                    .addComponent(lblBcpEstado)
                                    .addComponent(lblBcpBase)
                                    .addComponent(lblBcpLimite)
                                    .addComponent(lblBcpPila)
                                    .addComponent(lblBcpPrioridad)
                                    .addComponent(lblBcpSiguiente)
                                    .addComponent(lblBcpTiempoInicio)
                                    .addComponent(lblBcpTiempoEmpleado)
                                    .addComponent(lblBcpArchivos)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(bx))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(2, 2, 2)
                                                .addComponent(ac))
                                            .addComponent(dx))
                                        .addGap(33, 33, 33)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cx)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel13)
                                                .addComponent(ax)))))
                                .addGap(31, 31, 31))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel16)
                                    .addGap(80, 80, 80))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel15)
                                    .addGap(34, 34, 34))))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel26)
                                    .addComponent(al))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblBcpCpu)
                                    .addComponent(jLabel20))))))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblBcpId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblBcpNombre))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblBcpEstado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblBcpBase))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblBcpLimite))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblBcpPila))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(lblBcpPrioridad))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(lblBcpSiguiente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ac)
                    .addComponent(ax)
                    .addComponent(bx))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dx)
                    .addComponent(cx)
                    .addComponent(al)
                    .addComponent(lblBcpCpu))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel15)
                    .addComponent(jLabel26)
                    .addComponent(jLabel20))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(lblBcpTiempoInicio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(lblBcpTiempoEmpleado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(lblBcpArchivos))
                .addGap(51, 51, 51))
        );

        panelSelectorCpu.setBackground(new java.awt.Color(255, 255, 255));
        panelSelectorCpu.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));

        btnCpuAnterior.setText("<");
        btnCpuAnterior.setFocusPainted(false);
        btnCpuAnterior.addActionListener(this::btnCpuAnteriorActionPerformed);

        lblCpuSeleccionado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCpuSeleccionado.setText("CPU 0");
        lblCpuSeleccionado.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        btnCpuSiguiente.setText(">");
        btnCpuSiguiente.setFocusPainted(false);
        btnCpuSiguiente.addActionListener(this::btnCpuSiguienteActionPerformed);

        javax.swing.GroupLayout panelSelectorCpuLayout = new javax.swing.GroupLayout(panelSelectorCpu);
        panelSelectorCpu.setLayout(panelSelectorCpuLayout);
        panelSelectorCpuLayout.setHorizontalGroup(
            panelSelectorCpuLayout.createSequentialGroup()
                .addComponent(btnCpuAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCpuSeleccionado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCpuSiguiente, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        panelSelectorCpuLayout.setVerticalGroup(
            panelSelectorCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnCpuAnterior)
                .addComponent(lblCpuSeleccionado)
                .addComponent(btnCpuSiguiente)
        );

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("COLA DE PROCESOS"));

        tablaProcesos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID Proceso", "Estado", "CPU"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tablaProcesos);
        if (tablaProcesos.getColumnModel().getColumnCount() > 0) {
            tablaProcesos.getColumnModel().getColumn(0).setMinWidth(90);
            tablaProcesos.getColumnModel().getColumn(0).setPreferredWidth(90);
            tablaProcesos.getColumnModel().getColumn(0).setMaxWidth(90);
        }

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("MEMORIA PRINCIPAL"));

        tablaMemoria.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Pos", "Valor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tablaMemoria);
        if (tablaMemoria.getColumnModel().getColumnCount() > 0) {
            tablaMemoria.getColumnModel().getColumn(0).setMinWidth(70);
            tablaMemoria.getColumnModel().getColumn(0).setPreferredWidth(70);
            tablaMemoria.getColumnModel().getColumn(0).setMaxWidth(70);
            tablaMemoria.getColumnModel().getColumn(1).setMinWidth(330);
            tablaMemoria.getColumnModel().getColumn(1).setPreferredWidth(360);
        }

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("MEMORIA VIRTUAL"));

        tablaMemoriaVirtual.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Pos", "Valor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaMemoriaVirtual);
        if (tablaMemoriaVirtual.getColumnModel().getColumnCount() > 0) {
            tablaMemoriaVirtual.getColumnModel().getColumn(0).setMinWidth(70);
            tablaMemoriaVirtual.getColumnModel().getColumn(0).setPreferredWidth(70);
            tablaMemoriaVirtual.getColumnModel().getColumn(0).setMaxWidth(70);
        }

        jScrollPane5.setBorder(javax.swing.BorderFactory.createTitledBorder("DISCO / BCP"));

        tablaDisco.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Pos", "Valor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tablaDisco);
        if (tablaDisco.getColumnModel().getColumnCount() > 0) {
            tablaDisco.getColumnModel().getColumn(0).setMinWidth(70);
            tablaDisco.getColumnModel().getColumn(0).setPreferredWidth(70);
            tablaDisco.getColumnModel().getColumn(0).setMaxWidth(70);
        }

        jLabel21.setText("Lista de Proceso");

        jLabel22.setText("Memoria Virtual ");

        jLabel23.setText("Disco");

        jLabel24.setText("Memoria");

        jScrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder("CONSOLA"));

        terminalArea.setEditable(false);
        terminalArea.setBackground(new java.awt.Color(11, 22, 28));
        terminalArea.setColumns(20);
        terminalArea.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        terminalArea.setForeground(new java.awt.Color(198, 255, 221));
        terminalArea.setRows(5);
        jScrollPane6.setViewportView(terminalArea);

        terminalInput.setText("Escribir..........");
        terminalInput.addActionListener(this::terminalInputActionPerformed);

        btnEnviar.setText("Enviar");
        btnEnviar.addActionListener(this::btnEnviarActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCargarArchivos)
                        .addGap(18, 18, 18)
                        .addComponent(btnEjecutar)
                        .addGap(18, 18, 18)
                        .addComponent(btnPasoPaso)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiar)
                        .addGap(18, 18, 18)
                        .addComponent(btnEstadisticas)
                        .addGap(18, 18, 18)
                        .addComponent(btnCargarConfig)
                        .addGap(18, 18, 18)
                        .addComponent(lblAlgoritmo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbAlgoritmo, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(lblQuantum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(lblCantidadCpus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbCantidadCpus, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelSelectorCpu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(terminalInput)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnEnviar))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCargarArchivos)
                    .addComponent(btnEjecutar)
                    .addComponent(btnPasoPaso)
                    .addComponent(btnLimpiar)
                    .addComponent(btnEstadisticas)
                    .addComponent(btnCargarConfig)
                    .addComponent(lblAlgoritmo)
                    .addComponent(cmbAlgoritmo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblQuantum)
                    .addComponent(spnQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidadCpus)
                    .addComponent(cmbCantidadCpus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelSelectorCpu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane5))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(terminalInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnEnviar)))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private boolean validarSistema() {
        if (!sistemaIniciado) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Debe cargar la configuración antes de continuar.",
                    "Sistema no iniciado",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        return true;
    }
    /**
     * Inicializa el sistema usando la configuración básica por defecto.
     * Se mantiene para no tener que cambiar todas las llamadas existentes.
     */
    private void cargarConfiguracionInicial() {
        try {
            java.nio.file.Path ruta = java.nio.file.Paths.get(DEFAULT_SETTINGS_PATH);

            if (!java.nio.file.Files.exists(ruta)) {
                ruta = java.nio.file.Paths.get("Proyecto_2", DEFAULT_SETTINGS_PATH);
            }

            if (!java.nio.file.Files.exists(ruta)) {
                inicializarSistema(DEFAULT_MEMORIA, DEFAULT_VIRTUAL, DEFAULT_DISCO);
                return;
            }

            String contenido = java.nio.file.Files.readString(ruta);

            int sizeMemoria = extraerValorJSON(contenido, "size_memoria", DEFAULT_MEMORIA);
            int sizeVirtual = extraerValorJSON(contenido, "size_memoriavirtual", DEFAULT_VIRTUAL);
            int sizeDisco = extraerValorJSON(contenido, "size_disco", DEFAULT_DISCO);
            String strategy = extraerTextoJSON(contenido, "strategy", DEFAULT_STRATEGY);
            int pageSize = extraerValorJSON(contenido, "page_size", DEFAULT_PAGE_SIZE);
            int countPartitions = extraerValorJSON(contenido, "count_partitions", DEFAULT_COUNT_PARTITIONS);
            int[] partitionSizes = extraerArregloJSON(contenido, "partition_sizes", DEFAULT_PARTITION_SIZES);

            inicializarSistema(
                    sizeMemoria,
                    sizeVirtual,
                    sizeDisco,
                    strategy,
                    pageSize,
                    countPartitions,
                    partitionSizes
            );

        } catch (Exception e) {
            inicializarSistema(DEFAULT_MEMORIA, DEFAULT_VIRTUAL, DEFAULT_DISCO);
            imprimirTerminal("No se pudo leer Ajuste.json. Usando configuracion por defecto.");
            mostrarToast("Configuracion por defecto aplicada");
        }
    }

    private void inicializarSistema(int sizeMemoria, int sizeVirtual, int sizeDisco) {
        inicializarSistema(
                sizeMemoria,
                sizeVirtual,
                sizeDisco,
                DEFAULT_STRATEGY,
                DEFAULT_PAGE_SIZE,
                DEFAULT_COUNT_PARTITIONS,
                DEFAULT_PARTITION_SIZES
        );
    }

    private int obtenerCantidadCpusSeleccionada() {
        if (cmbCantidadCpus == null) {
            return DEFAULT_CPU_COUNT;
        }

        try {
            return Integer.parseInt((String) cmbCantidadCpus.getSelectedItem());
        } catch (Exception e) {
            return DEFAULT_CPU_COUNT;
        }
    }

    /**
     * Inicializa el sistema con estrategia de memoria.
     *
     * Orden correcto:
     * 1. Crear Disco.
     * 2. Crear Memoria con Strategy y Disco.
     * 3. Crear la estrategia interna de memoria.
     * 4. Crear CPU, Dispatcher y Parser.
     */
    private void inicializarSistema(
            int sizeMemoria,
            int sizeVirtual,
            int sizeDisco,
            String strategy,
            int pageSize,
            int countPartitions,
            int[] partitionSizes
    ) {
        strategy = normalizarStrategy(strategy);

        configMemoria = sizeMemoria;
        configVirtual = sizeVirtual;
        configDisco = sizeDisco;
        configStrategy = strategy;
        configPageSize = pageSize;
        configCountPartitions = countPartitions;
        configPartitionSizes = partitionSizes == null
                ? java.util.Arrays.copyOf(DEFAULT_PARTITION_SIZES, DEFAULT_PARTITION_SIZES.length)
                : java.util.Arrays.copyOf(partitionSizes, partitionSizes.length);

        // Primero se crea Disco porque Pagination necesita usar memoria virtual.
        disco = new Disco(sizeVirtual, sizeDisco);

        // Luego se crea Memoria con la estrategia y el disco.
        memoria = new Memoria(sizeMemoria, strategy, disco);

        // Crear la estrategia concreta.
        switch (strategy) {
            case "Pagination":
                memoria.Creation_Strategy(pageSize, null);
                break;

            case "Partition_Equal":
                memoria.Creation_Strategy(countPartitions, null);
                break;

            case "Partition_Different":
                memoria.Creation_Strategy(0, partitionSizes);
                break;

            case "Best_Fit":
                memoria.Creation_Strategy(0, null);
                break;

            case "Default":
            default:
                memoria.Creation_Strategy(0, null);
                break;
        }

        int cantidadCpus = obtenerCantidadCpusSeleccionada();

        cpus.clear();

        for (int i = 0; i < cantidadCpus; i++) {
            cpus.add(new CPU(memoria, disco));
        }

        cpuSeleccionado = 0;
        actualizarSelectorCpu();
        cpu = cpus.get(0);
        dispatcher = new Dispatcher();
        parser     = new Parser();
        sistemaIniciado = true;

        terminalInput.setEnabled(false);
        btnEnviar.setEnabled(false);

        resetPlanificadorEjecucion();

        tiempoGlobal = 0;
        coloresPorBCP.clear();
        procesosBasePlanificacion.clear();
        archivosBasePlanificacion.clear();
        tiemposLlegadaPorArchivo.clear();
        historialEstadisticas.clear();
        contadorEjecucionesEstadisticas = 0;
        ultimaFirmaEstadisticas = "";
        firmasEstadisticasGuardadas.clear();
        ejecucionEnCurso = false;

        limpiarTabla(tablaMemoria);
        limpiarTabla(tablaMemoriaVirtual);
        limpiarTabla(tablaDisco);
        limpiarTabla(tablaProcesos);

        imprimirTerminal("Sistema iniciado. Memoria: " + sizeMemoria
                + " | Virtual: " + sizeVirtual
                + " | Disco: " + sizeDisco
                + " | Estrategia: " + strategy
                + " | CPUs: " + cantidadCpus);
        mostrarToast("Sistema listo");

        if ("Pagination".equals(strategy)) {
            imprimirTerminal("Paginación activada. Tamaño de página: " + pageSize);
        } else if ("Partition_Equal".equals(strategy)) {
            imprimirTerminal("Partición fija igual activada. Particiones: " + countPartitions);
        } else if ("Partition_Different".equals(strategy)) {
            imprimirTerminal("Partición fija diferente activada.");
        } else if ("Best_Fit".equals(strategy)) {
            imprimirTerminal("Best Fit activado.");
        }
    }

    private String normalizarStrategy(String strategy) {
        if (strategy == null || strategy.isBlank()) {
            return DEFAULT_STRATEGY;
        }

        String value = strategy.trim();

        if (value.equalsIgnoreCase("Default")) return "Default";
        if (value.equalsIgnoreCase("Best_Fit") || value.equalsIgnoreCase("BestFit")) return "Best_Fit";
        if (value.equalsIgnoreCase("Pagination") || value.equalsIgnoreCase("Paginacion")) return "Pagination";
        if (value.equalsIgnoreCase("Partition_Equal")) return "Partition_Equal";
        if (value.equalsIgnoreCase("Partition_Different")) return "Partition_Different";

        return DEFAULT_STRATEGY;
    }
 
    private int extraerValorJSON(String json, String clave, int valorDefecto) {
        try {
            String buscar = "\"" + clave + "\"";
            int idx = json.indexOf(buscar);
            if (idx == -1) return valorDefecto;

            int dosPuntos = json.indexOf(":", idx);
            int inicio = dosPuntos + 1;
            int fin = inicio;

            while (fin < json.length() &&
                    (Character.isDigit(json.charAt(fin)) || json.charAt(fin) == ' ' || json.charAt(fin) == '-')) {
                fin++;
            }

            return Integer.parseInt(json.substring(inicio, fin).trim());
        } catch (Exception e) {
            return valorDefecto;
        }
    }

    private String extraerTextoJSON(String json, String clave, String valorDefecto) {
        try {
            String buscar = "\"" + clave + "\"";
            int idx = json.indexOf(buscar);

            if (idx == -1) {
                return valorDefecto;
            }

            int dosPuntos = json.indexOf(":", idx);
            int primeraComilla = json.indexOf("\"", dosPuntos + 1);
            int segundaComilla = json.indexOf("\"", primeraComilla + 1);

            if (primeraComilla == -1 || segundaComilla == -1) {
                return valorDefecto;
            }

            return json.substring(primeraComilla + 1, segundaComilla).trim();

        } catch (Exception e) {
            return valorDefecto;
        }
    }

    private int[] extraerArregloJSON(String json, String clave, int[] valorDefecto) {
        try {
            String buscar = "\"" + clave + "\"";
            int idx = json.indexOf(buscar);

            if (idx == -1) {
                return valorDefecto;
            }

            int inicioCorchete = json.indexOf("[", idx);
            int finCorchete = json.indexOf("]", inicioCorchete);

            if (inicioCorchete == -1 || finCorchete == -1) {
                return valorDefecto;
            }

            String contenido = json.substring(inicioCorchete + 1, finCorchete).trim();

            if (contenido.isEmpty()) {
                return valorDefecto;
            }

            String[] partes = contenido.split(",");
            int[] resultado = new int[partes.length];

            for (int i = 0; i < partes.length; i++) {
                resultado[i] = Integer.parseInt(partes[i].trim());
            }

            return resultado;

        } catch (Exception e) {
            return valorDefecto;
        }
    }
     

    private void actualizarVisibilidadQuantum() {
        if (cmbAlgoritmo == null || lblQuantum == null || spnQuantum == null) {
            return;
        }

        String algoritmo = String.valueOf(cmbAlgoritmo.getSelectedItem());
        boolean requiereQuantum = algoritmoUsaQuantum(algoritmo);

        lblQuantum.setVisible(requiereQuantum);
        spnQuantum.setVisible(requiereQuantum);

        revalidate();
        repaint();
    }

    private boolean algoritmoUsaQuantum(String algoritmo) {
        return "RR".equals(algoritmo)
                || "SRR".equals(algoritmo)
                || "Lottery".equals(algoritmo);
    }

    private int obtenerQuantumSeleccionado() {
        try {
            return ((Number) spnQuantum.getValue()).intValue();
        } catch (Exception e) {
            return 2;
        }
    }

    private void resetPlanificadorEjecucion() {
        algoritmoEjecucionActual = "";
        quantumEjecucionActual = -1;
        fcfsRuntime = new FCFS();
        sjfRuntime = new SJF();
        srtRuntime = new SRT();
        rrRuntime = null;
        srrRuntime = null;
        hrrnRuntime = new HRRN();
        lotteryRuntime = null;
    }

    private void asegurarPlanificadorEjecucion() {
        String algoritmo = (cmbAlgoritmo == null) ? "FCFS" : String.valueOf(cmbAlgoritmo.getSelectedItem());
        int quantum = obtenerQuantumSeleccionado();

        if (algoritmo.equals(algoritmoEjecucionActual)
                && (!algoritmoUsaQuantum(algoritmo) || quantum == quantumEjecucionActual)) {
            return;
        }

        algoritmoEjecucionActual = algoritmo;
        quantumEjecucionActual = quantum;

        fcfsRuntime = new FCFS();
        sjfRuntime = new SJF();
        srtRuntime = new SRT();
        hrrnRuntime = new HRRN();
        rrRuntime = "RR".equals(algoritmo) ? new RR(quantum) : null;
        srrRuntime = "SRR".equals(algoritmo)
                ? new SRR(quantum, Constants.SRR_NEW_PRIORITY_RATE, Constants.SRR_ACCEPTED_PRIORITY_RATE)
                : null;
        lotteryRuntime = "Lottery".equals(algoritmo) ? new Lottery(quantum) : null;
    }


    private String claveArchivo(java.io.File archivo) {
        if (archivo == null) {
            return "";
        }
        try {
            return archivo.getCanonicalPath();
        } catch (Exception e) {
            return archivo.getAbsolutePath();
        }
    }

    private void registrarArchivoBase(java.io.File archivo) {
        if (archivo == null) {
            return;
        }

        String clave = claveArchivo(archivo);
        for (java.io.File existente : archivosBasePlanificacion) {
            if (claveArchivo(existente).equals(clave)) {
                return;
            }
        }

        archivosBasePlanificacion.add(archivo);
        tiemposLlegadaPorArchivo.putIfAbsent(clave, 0);
    }

    private Integer buscarTiempoLlegadaPorNombre(String nombreArchivo) {
        if (nombreArchivo == null) {
            return null;
        }

        for (java.io.File archivo : archivosBasePlanificacion) {
            if (archivo != null && nombreArchivo.equals(archivo.getName())) {
                return tiemposLlegadaPorArchivo.getOrDefault(claveArchivo(archivo), 0);
            }
        }

        return null;
    }

    private void aplicarTiempoLlegadaABCP(BCP bcp, int tiempoLlegada) {
        if (bcp == null) {
            return;
        }

        bcp.setTiempoLlegada(Math.max(0, tiempoLlegada));
        bcp.setTiempoInicio(0);
        bcp.setTiempoFinal(-1);
        bcp.setTiempoEmpleado(0);
        bcp.setTiempoEspera(0);
        bcp.setTurnaround(0);
        bcp.setTrTs(0.0);
        bcp.setIniciado(false);

        if (bcp.getRafagaTotal() <= 0) {
            int rafaga = Math.max(1, bcp.getLimite() - bcp.getBase() + 1);
            bcp.setRafagaTotal(rafaga);
        }

        bcp.setRafagaRestante(bcp.getRafagaTotal());
    }

    private void mostrarModalTiemposLlegada(java.util.List<BCP> procesos) {
        if (procesos == null || procesos.isEmpty()) {
            return;
        }

        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        javax.swing.JDialog dialog = new javax.swing.JDialog(owner, "Tiempos de llegada", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);

        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout(12, 12));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setBackground(java.awt.Color.WHITE);

        javax.swing.JLabel title = new javax.swing.JLabel("Asignar tiempos de llegada");
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        title.setForeground(new java.awt.Color(30, 45, 65));

        javax.swing.JLabel subtitle = new javax.swing.JLabel("Edite el tiempo de llegada de cada proceso antes de iniciar la ejecución.");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        subtitle.setForeground(new java.awt.Color(86, 101, 120));

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);
        root.add(header, java.awt.BorderLayout.NORTH);

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"ID", "Archivo", "Tiempo de llegada"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Integer.class : String.class;
            }
        };

        for (BCP bcp : procesos) {
            Integer tiempoGuardado = buscarTiempoLlegadaPorNombre(bcp.getNombreProceso());
            model.addRow(new Object[]{
                bcp.getIdProceso(),
                bcp.getNombreProceso(),
                tiempoGuardado == null ? bcp.getTiempoLlegada() : tiempoGuardado
            });
        }

        javax.swing.JTable table = new javax.swing.JTable(model);
        estilizarTabla(table);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(320);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);

        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(table);
        scroll.setPreferredSize(new java.awt.Dimension(620, 220));
        root.add(scroll, java.awt.BorderLayout.CENTER);

        javax.swing.JButton btnGuardar = new javax.swing.JButton("Guardar tiempos");
        javax.swing.JButton btnCancelar = new javax.swing.JButton("Dejar en 0");

        btnGuardar.setFocusPainted(false);
        btnCancelar.setFocusPainted(false);
        btnGuardar.setBackground(new java.awt.Color(30, 45, 65));
        btnGuardar.setForeground(java.awt.Color.WHITE);
        btnCancelar.setBackground(java.awt.Color.WHITE);
        btnCancelar.setForeground(new java.awt.Color(30, 45, 65));

        btnGuardar.addActionListener(e -> {
            try {
                for (int i = 0; i < procesos.size(); i++) {
                    Object valor = model.getValueAt(i, 2);
                    int llegada = Integer.parseInt(String.valueOf(valor).trim());

                    if (llegada < 0) {
                        throw new NumberFormatException("Tiempo negativo");
                    }

                    BCP bcp = procesos.get(i);
                    aplicarTiempoLlegadaABCP(bcp, llegada);

                    for (java.io.File archivo : archivosBasePlanificacion) {
                        if (archivo != null && archivo.getName().equals(bcp.getNombreProceso())) {
                            tiemposLlegadaPorArchivo.put(claveArchivo(archivo), llegada);
                            break;
                        }
                    }

                    dispatcher.actualizarBCP(bcp);
                    memoria.actualizarBCPPorId(bcp);
                    registrarProcesoBaseParaPlanificacion(bcp);
                }

                ultimaFirmaEstadisticas = "";
                dialog.dispose();
                imprimirTerminal("Tiempos de llegada actualizados.");
                mostrarToast("Tiempos guardados");
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(
                        dialog,
                        "Los tiempos de llegada deben ser números enteros mayores o iguales a 0.",
                        "Dato inválido",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        javax.swing.JPanel footer = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.add(btnCancelar);
        footer.add(btnGuardar);
        root.add(footer, java.awt.BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void prepararNuevaEjecucionDesdeArchivos(String mensaje) {
        if (ejecucionEnCurso || !sistemaIniciado || archivosBasePlanificacion.isEmpty()) {
            return;
        }

        java.util.List<java.io.File> archivos = new java.util.ArrayList<>(archivosBasePlanificacion);
        java.util.Map<String, Integer> tiempos = new java.util.LinkedHashMap<>(tiemposLlegadaPorArchivo);

        disco = new Disco(configVirtual, configDisco);
        memoria = new Memoria(configMemoria, configStrategy, disco);

        switch (configStrategy) {
            case "Pagination":
                memoria.Creation_Strategy(configPageSize, null);
                break;
            case "Partition_Equal":
                memoria.Creation_Strategy(configCountPartitions, null);
                break;
            case "Partition_Different":
                memoria.Creation_Strategy(0, configPartitionSizes);
                break;
            case "Best_Fit":
                memoria.Creation_Strategy(0, null);
                break;
            case "Default":
            default:
                memoria.Creation_Strategy(0, null);
                break;
        }

        cpus.clear();
        int cantidadCpus = obtenerCantidadCpusSeleccionada();
        for (int i = 0; i < cantidadCpus; i++) {
            cpus.add(new CPU(memoria, disco));
        }

        cpuSeleccionado = 0;
        cpu = cpus.isEmpty() ? null : cpus.get(0);
        dispatcher = new Dispatcher();
        parser = new Parser();
        procesosBasePlanificacion.clear();
        tiempoGlobal = 0;
        resetPlanificadorEjecucion();

        for (java.io.File archivo : archivos) {
            BCP bcp = parser.Leer(archivo, memoria, disco);
            if (bcp == null) {
                continue;
            }

            int llegada = tiempos.getOrDefault(claveArchivo(archivo), 0);
            aplicarTiempoLlegadaABCP(bcp, llegada);
            dispatcher.registrarProceso(bcp);
            registrarProcesoBaseParaPlanificacion(bcp);

            if (memoria.obtenerBCPPorId(bcp.getIdProceso()) == null && !memoria.lleno()) {
                memoria.agregarBCP(bcp);
            } else {
                memoria.actualizarBCPPorId(bcp);
            }
        }

        archivosBasePlanificacion = archivos;
        tiemposLlegadaPorArchivo = tiempos;
        actualizarSelectorCpu();
        actualizarBCP();
        actualizarTablaProcesos();
        actualizarTablaMemoria();
        actualizarTablaDisco();
        actualizarTablaVirtual();

        if (mensaje != null && !mensaje.isBlank()) {
            imprimirTerminal(mensaje);
            mostrarToast("Listo para ejecutar");
        }
    }

    private void guardarEstadisticasEjecucionReal() {
        if (dispatcher == null) {
            return;
        }

        String firmaActual = generarFirmaEstadisticasActual();

        // Evita duplicar resultados si se presiona Estadísticas o Ejecutar
        // sin cambiar algoritmo, quantum, CPUs o procesos cargados.
        if (firmasEstadisticasGuardadas.contains(firmaActual)) {
            return;
        }

        String algoritmo = String.valueOf(cmbAlgoritmo.getSelectedItem());
        int quantum = obtenerQuantumSeleccionado();
        int cantidadCpus = obtenerCantidadCpusSeleccionada();
        String etiquetaAlgoritmo = construirEtiquetaAlgoritmo(algoritmo, quantum, cantidadCpus);

        /*
         * IMPORTANTE:
         * Las estadísticas deben salir de la ejecución real, no de ejecutarAlgoritmo().
         * ejecutarAlgoritmo() calcula una planificación teórica de una sola CPU.
         * Por eso con CPUs=2 o CPUs=4 podía mostrar que P3 iniciaba en su llegada,
         * aunque en la simulación real la CPU todavía estuviera ocupada.
         *
         * Aquí se toman los BCP finales del dispatcher, que son los mismos que fueron
         * ejecutados por las CPUs visuales.
         */
        java.util.List<BCP> resultados = dispatcher.obtenerColaProcesos();

        if (resultados == null || resultados.isEmpty()) {
            return;
        }

        resultados.sort(java.util.Comparator
                .comparingInt((BCP bcp) -> bcp.getTiempoFinal() < 0 ? Integer.MAX_VALUE : bcp.getTiempoFinal())
                .thenComparing(BCP::getIdProceso));

        int numeroEjecucion = contadorEjecucionesEstadisticas + 1;
        java.util.List<ResultadoEstadistica> registrosEjecucion = new java.util.ArrayList<>();

        for (BCP bcp : resultados) {
            if (bcp == null) {
                continue;
            }

            boolean finalizado = "finalizado".equalsIgnoreCase(bcp.getEstado())
                    || bcp.getTiempoFinal() >= 0
                    || (bcp.getRafagaTotal() > 0 && bcp.getRafagaRestante() <= 0);

            if (!finalizado) {
                continue;
            }

            int rafaga = bcp.getRafagaTotal() > 0
                    ? bcp.getRafagaTotal()
                    : Math.max(1, bcp.getTiempoEmpleado());

            int llegada = Math.max(0, bcp.getTiempoLlegada());
            int inicioProceso = bcp.isIniciado()
                    ? bcp.getTiempoInicio()
                    : Math.max(llegada, bcp.getTiempoFinal() - rafaga);
            int finalProceso = bcp.getTiempoFinal();

            // Respaldo por si algún BCP finalizado no guardó tiempo final.
            if (finalProceso < 0) {
                finalProceso = tiempoGlobal > 0
                        ? tiempoGlobal
                        : inicioProceso + Math.max(rafaga, bcp.getTiempoEmpleado());
            }

            if (inicioProceso < llegada) {
                inicioProceso = llegada;
            }

            if (finalProceso < inicioProceso) {
                finalProceso = inicioProceso + Math.max(rafaga, bcp.getTiempoEmpleado());
            }

            int turnaround = Math.max(0, finalProceso - llegada);
            int espera = Math.max(0, turnaround - rafaga);
            double trTs = rafaga > 0 ? (double) turnaround / rafaga : 0.0;

            bcp.setTiempoInicio(inicioProceso);
            bcp.setTiempoFinal(finalProceso);
            bcp.setTiempoEspera(espera);
            bcp.setTurnaround(turnaround);
            bcp.setTrTs(trTs);

            registrosEjecucion.add(new ResultadoEstadistica(
                    numeroEjecucion,
                    etiquetaAlgoritmo,
                    bcp.getIdProceso(),
                    llegada,
                    rafaga,
                    inicioProceso,
                    finalProceso,
                    espera,
                    turnaround,
                    trTs
            ));
        }

        if (registrosEjecucion.isEmpty()) {
            return;
        }

        contadorEjecucionesEstadisticas = numeroEjecucion;
        historialEstadisticas.addAll(registrosEjecucion);

        firmasEstadisticasGuardadas.add(firmaActual);
        ultimaFirmaEstadisticas = firmaActual;
    }

    private void registrarProcesoBaseParaPlanificacion(BCP bcp) {
        if (bcp == null) {
            return;
        }

        // Si ya estaba registrado, se reemplaza para evitar duplicados.
        for (int i = 0; i < procesosBasePlanificacion.size(); i++) {
            if (procesosBasePlanificacion.get(i).getIdProceso().equals(bcp.getIdProceso())) {
                procesosBasePlanificacion.set(i, clonarBCPBase(bcp));
                return;
            }
        }

        procesosBasePlanificacion.add(clonarBCPBase(bcp));
    }

    private BCP clonarBCPBase(BCP original) {
        BCP copia = new BCP(
                original.getIdProceso(),
                original.getNombreProceso(),
                "nuevo",
                original.getBase(),
                original.getLimite(),
                original.getBase(),
                original.getPrioridad()
        );

        int rafaga = original.getRafagaTotal();

        if (rafaga <= 0 && original.getLimite() >= original.getBase()) {
            rafaga = original.getLimite() - original.getBase() + 1;
        }

        if (rafaga <= 0) {
            rafaga = Math.max(1, original.getTiempoEmpleado());
        }

        copia.setTiempoLlegada(original.getTiempoLlegada());
        copia.setRafagaTotal(rafaga);
        copia.setRafagaRestante(rafaga);
        copia.setTiempoInicio(0);
        copia.setTiempoFinal(-1);
        copia.setTiempoEmpleado(0);
        copia.setTiempoEspera(0);
        copia.setTurnaround(0);
        copia.setTrTs(0.0);
        copia.setTickets(Math.max(1, original.getTickets()));
        copia.setQuantumRestante(0);
        copia.setIniciado(false);
        copia.setCpuAsignado(-1);
        copia.setIr("");
        copia.setArchivosAbiertos(new java.util.ArrayList<>(original.getArchivosAbiertos()));

        return copia;
    }

    private java.util.List<BCP> crearCopiasParaPlanificacion() {
        java.util.List<BCP> copias = new java.util.ArrayList<>();

        for (BCP base : procesosBasePlanificacion) {
            copias.add(clonarBCPBase(base));
        }

        return copias;
    }

    private java.util.List<BCP> ejecutarAlgoritmo(
            String algoritmo,
            int quantum,
            java.util.List<BCP> procesos
    ) {
        if (procesos == null) {
            return new java.util.ArrayList<>();
        }

        switch (algoritmo) {
            case "FCFS":
                return new FCFS().schedule(procesos);

            case "SJF":
                return new SJF().schedule(procesos);

            case "SRT":
                return new SRT().schedule(procesos);

            case "RR":
                return new RR(quantum).schedule(procesos);

            case "SRR":
                return new SRR(
                        quantum,
                        Constants.SRR_NEW_PRIORITY_RATE,
                        Constants.SRR_ACCEPTED_PRIORITY_RATE
                ).schedule(procesos);

            case "HRRN":
                return new HRRN().schedule(procesos);

            case "Lottery":
                return new Lottery(quantum).schedule(procesos);

            default:
                return new FCFS().schedule(procesos);
        }
    }

    private void ejecutarPlanificacionSeleccionada() {
        String firmaActual = generarFirmaEstadisticasActual();

        if (!firmaActual.equals(ultimaFirmaEstadisticas) || historialEstadisticas.isEmpty()) {
            String algoritmo = String.valueOf(cmbAlgoritmo.getSelectedItem());
            int quantum = obtenerQuantumSeleccionado();
            int cantidadCpus = obtenerCantidadCpusSeleccionada();

            java.util.List<BCP> procesosParaEjecutar = crearCopiasParaPlanificacion();
            java.util.List<BCP> resultado = ejecutarAlgoritmo(algoritmo, quantum, procesosParaEjecutar);

            contadorEjecucionesEstadisticas++;

            String etiquetaAlgoritmo = construirEtiquetaAlgoritmo(algoritmo, quantum, cantidadCpus);

            for (BCP bcp : resultado) {
                historialEstadisticas.add(new ResultadoEstadistica(
                        contadorEjecucionesEstadisticas,
                        etiquetaAlgoritmo,
                        bcp.getIdProceso(),
                        bcp.getTiempoLlegada(),
                        bcp.getRafagaTotal(),
                        bcp.getTiempoInicio(),
                        bcp.getTiempoFinal(),
                        bcp.getTiempoEspera(),
                        bcp.getTurnaround(),
                        bcp.getTrTs()
                ));
            }

            ultimaFirmaEstadisticas = firmaActual;
        }

        mostrarEstadisticasAcumuladasModal();
    }

    private String construirEtiquetaAlgoritmo(String algoritmo, int quantum, int cantidadCpus) {
        String etiqueta = algoritmoUsaQuantum(algoritmo)
                ? algoritmo + " Q=" + quantum
                : algoritmo;

        return etiqueta + " | CPUs=" + cantidadCpus;
    }

    private String generarFirmaEstadisticasActual() {
        String algoritmo = String.valueOf(cmbAlgoritmo.getSelectedItem());
        int quantum = obtenerQuantumSeleccionado();
        int cantidadCpus = obtenerCantidadCpusSeleccionada();

        StringBuilder firma = new StringBuilder();
        firma.append(algoritmo).append("|");
        firma.append(algoritmoUsaQuantum(algoritmo) ? quantum : "-").append("|");
        firma.append(cantidadCpus).append("|");

        for (BCP bcp : procesosBasePlanificacion) {
            if (bcp == null) {
                continue;
            }

            firma.append(bcp.getIdProceso()).append(":")
                    .append(bcp.getTiempoLlegada()).append(":")
                    .append(bcp.getRafagaTotal()).append(":")
                    .append(bcp.getPrioridad()).append(":")
                    .append(bcp.getTickets()).append(";");
        }

        return firma.toString();
    }

    private java.util.Map<Integer, java.util.List<ResultadoEstadistica>> agruparEstadisticasPorEjecucion() {
        java.util.Map<Integer, java.util.List<ResultadoEstadistica>> grupos = new java.util.LinkedHashMap<>();

        for (ResultadoEstadistica resultado : historialEstadisticas) {
            grupos.computeIfAbsent(
                    resultado.numeroEjecucion,
                    k -> new java.util.ArrayList<>()
            ).add(resultado);
        }

        return grupos;
    }

    private void mostrarEstadisticasAcumuladasModal() {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(this::mostrarEstadisticasAcumuladasModal);
            return;
        }

        if (historialEstadisticas.isEmpty()) {
            mostrarToast("No hay estadísticas para mostrar");
            return;
        }

        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);

        javax.swing.JComponent glass = null;
        javax.swing.RootPaneContainer root = null;

        if (owner instanceof javax.swing.RootPaneContainer) {
            root = (javax.swing.RootPaneContainer) owner;
            glass = crearCapaOscura();
            root.setGlassPane(glass);
            glass.setVisible(true);
        }

        final javax.swing.RootPaneContainer rootFinal = root;
        final javax.swing.JComponent glassFinal = glass;

        javax.swing.JDialog dialog = new javax.swing.JDialog(owner, "Estadísticas acumuladas", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);

        javax.swing.JPanel main = new javax.swing.JPanel(new java.awt.BorderLayout(0, 12));
        main.setBackground(java.awt.Color.WHITE);
        main.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(210, 220, 235), 1),
                javax.swing.BorderFactory.createEmptyBorder(18, 18, 14, 18)
        ));

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);

        javax.swing.JPanel headerText = new javax.swing.JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new javax.swing.BoxLayout(headerText, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new javax.swing.JLabel("Estadísticas acumuladas");
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        title.setForeground(new java.awt.Color(35, 49, 66));

        javax.swing.JLabel subtitle = new javax.swing.JLabel("Comparación por algoritmo. Cada bloque muestra sus procesos y promedios.");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        subtitle.setForeground(new java.awt.Color(86, 101, 120));

        headerText.add(title);
        headerText.add(javax.swing.Box.createVerticalStrut(4));
        headerText.add(subtitle);

        javax.swing.JButton btnCerrarSuperior = new javax.swing.JButton("X");
        btnCerrarSuperior.setFocusPainted(false);
        btnCerrarSuperior.setBackground(new java.awt.Color(220, 53, 69));
        btnCerrarSuperior.setForeground(java.awt.Color.WHITE);
        btnCerrarSuperior.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        btnCerrarSuperior.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnCerrarSuperior.addActionListener(e -> dialog.dispose());

        header.add(headerText, java.awt.BorderLayout.CENTER);
        header.add(btnCerrarSuperior, java.awt.BorderLayout.EAST);

        javax.swing.JPanel content = new javax.swing.JPanel();
        content.setBackground(new java.awt.Color(246, 248, 251));
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        java.util.Map<Integer, java.util.List<ResultadoEstadistica>> grupos = agruparEstadisticasPorEjecucion();

        for (java.util.Map.Entry<Integer, java.util.List<ResultadoEstadistica>> entry : grupos.entrySet()) {
            content.add(crearPanelGrupoEstadistica(entry.getKey(), entry.getValue()));
            content.add(javax.swing.Box.createVerticalStrut(12));
        }

        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(content);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(225, 231, 240)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new java.awt.Dimension(1060, 480));

        javax.swing.JPanel footer = new javax.swing.JPanel(new java.awt.BorderLayout());
        footer.setOpaque(false);

        javax.swing.JLabel resumen = new javax.swing.JLabel(
                "Ejecuciones acumuladas: " + grupos.size()
                        + "  |  Registros: " + historialEstadisticas.size()
        );
        resumen.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        resumen.setForeground(new java.awt.Color(35, 49, 66));

        javax.swing.JButton btnCerrar = new javax.swing.JButton("Cerrar");
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBackground(new java.awt.Color(35, 49, 66));
        btnCerrar.setForeground(java.awt.Color.WHITE);
        btnCerrar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        btnCerrar.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCerrar.addActionListener(e -> dialog.dispose());

        footer.add(resumen, java.awt.BorderLayout.WEST);
        footer.add(btnCerrar, java.awt.BorderLayout.EAST);

        main.add(header, java.awt.BorderLayout.NORTH);
        main.add(scroll, java.awt.BorderLayout.CENTER);
        main.add(footer, java.awt.BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (glassFinal != null) {
                    glassFinal.setVisible(false);
                }
                if (rootFinal != null) {
                    rootFinal.setGlassPane(new javax.swing.JPanel());
                }
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private javax.swing.JComponent crearCapaOscura() {
        return new javax.swing.JPanel() {
            {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setColor(new java.awt.Color(0, 0, 0, 175));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    private javax.swing.JPanel crearPanelGrupoEstadistica(int numeroEjecucion, java.util.List<ResultadoEstadistica> registros) {
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(218, 226, 236)),
                javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        String algoritmo = registros.isEmpty() ? "Sin algoritmo" : registros.get(0).algoritmo;

        javax.swing.JLabel titulo = new javax.swing.JLabel("Ejecución " + numeroEjecucion + " • " + algoritmo);
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        titulo.setForeground(new java.awt.Color(35, 49, 66));

        String[] columnas = {"Proceso", "Llegada", "Rafaga", "Inicio", "Final", "Espera", "Turnaround", "Tr/Ts"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        double sumaTrTs = 0.0;
        double sumaTurnaround = 0.0;
        double sumaEspera = 0.0;

        for (ResultadoEstadistica r : registros) {
            sumaTrTs += r.trTs;
            sumaTurnaround += r.turnaround;
            sumaEspera += r.tiempoEspera;

            model.addRow(new Object[]{
                    r.proceso,
                    r.tiempoLlegada,
                    r.rafaga,
                    r.tiempoInicio,
                    r.tiempoFinal,
                    r.tiempoEspera,
                    r.turnaround,
                    String.format(java.util.Locale.US, "%.2f", r.trTs)
            });
        }

        javax.swing.JTable table = new javax.swing.JTable(model);
        table.setRowHeight(26);
        table.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(238, 242, 247));
        table.getTableHeader().setForeground(new java.awt.Color(50, 66, 85));
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setFillsViewportHeight(true);

        javax.swing.JScrollPane tableScroll = new javax.swing.JScrollPane(table);
        tableScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(232, 236, 244)));
        tableScroll.setPreferredSize(new java.awt.Dimension(1000, Math.min(220, 45 + registros.size() * 28)));

        int cantidad = Math.max(1, registros.size());
        double mediaTrTs = sumaTrTs / cantidad;
        double mediaTurnaround = sumaTurnaround / cantidad;
        double mediaEspera = sumaEspera / cantidad;

        javax.swing.JLabel promedio = new javax.swing.JLabel(
                "Media Tr/Ts: " + String.format(java.util.Locale.US, "%.2f", mediaTrTs)
                        + "   |   Media Espera: " + String.format(java.util.Locale.US, "%.2f", mediaEspera)
                        + "   |   Media Turnaround: " + String.format(java.util.Locale.US, "%.2f", mediaTurnaround)
                        + "   |   Procesos: " + registros.size()
        );
        promedio.setOpaque(true);
        promedio.setBackground(new java.awt.Color(232, 245, 233));
        promedio.setForeground(new java.awt.Color(35, 80, 45));
        promedio.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        promedio.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));

        panel.add(titulo, java.awt.BorderLayout.NORTH);
        panel.add(tableScroll, java.awt.BorderLayout.CENTER);
        panel.add(promedio, java.awt.BorderLayout.SOUTH);

        return panel;
    }

    private String abreviar(String texto, int max) {
        if (texto == null) {
            return "";
        }

        if (texto.length() <= max) {
            return texto;
        }

        return texto.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static class ResultadoEstadistica {
        int numeroEjecucion;
        String algoritmo;
        String proceso;
        int tiempoLlegada;
        int rafaga;
        int tiempoInicio;
        int tiempoFinal;
        int tiempoEspera;
        int turnaround;
        double trTs;

        ResultadoEstadistica(
                int numeroEjecucion,
                String algoritmo,
                String proceso,
                int tiempoLlegada,
                int rafaga,
                int tiempoInicio,
                int tiempoFinal,
                int tiempoEspera,
                int turnaround,
                double trTs
        ) {
            this.numeroEjecucion = numeroEjecucion;
            this.algoritmo = algoritmo;
            this.proceso = proceso;
            this.tiempoLlegada = tiempoLlegada;
            this.rafaga = rafaga;
            this.tiempoInicio = tiempoInicio;
            this.tiempoFinal = tiempoFinal;
            this.tiempoEspera = tiempoEspera;
            this.turnaround = turnaround;
            this.trTs = trTs;
        }
    }

    private boolean hayCpuActiva() {
        for (CPU cpuActual : cpus) {
            if (cpuActual != null && cpuActual.getBcp() != null) {
                return true;
            }
        }

        return false;
    }

    private void asignarProcesosACpusDisponibles() {
        for (int i = 0; i < cpus.size(); i++) {
            CPU cpuActual = cpus.get(i);

            if (cpuActual == null || !cpuActual.estaLibre()) {
                continue;
            }

            BCP bcp = obtenerSiguienteProcesoPreparado();

            if (bcp == null) {
                continue;
            }

            asignarTiempoInicioSiEsPrimeraVez(bcp);
            bcp.setIniciado(true);

            // Si el algoritmo usa quantum, inicializar quantum restante
            if (algoritmoUsaQuantum(String.valueOf(cmbAlgoritmo.getSelectedItem()))) {
                bcp.setQuantumRestante(obtenerQuantumSeleccionado());
            }

            bcp.setCpuAsignado(i);
            cpuActual.CargarBcp(bcp);
            memoria.actualizarBCPPorId(cpuActual.getBcp());
            dispatcher.actualizarBCP(cpuActual.getBcp());
        }
    }

    private BCP obtenerSiguienteProcesoPreparado() {
        if (memoria == null) {
            return null;
        }

        String algoritmo = (cmbAlgoritmo == null) ? "FCFS" : String.valueOf(cmbAlgoritmo.getSelectedItem());
        java.util.List<BCP> todos = memoria.obtenerTodosBCPsEnMemoria();
        asegurarPlanificadorEjecucion();

        switch (algoritmo) {
            case "SJF":
                return sjfRuntime.selectNext(todos, tiempoGlobal);

            case "SRT":
                return srtRuntime.selectNext(todos, tiempoGlobal);

            case "RR":
                return rrRuntime != null ? rrRuntime.selectNext(todos, tiempoGlobal) : null;

            case "SRR":
                BCP seleccionadoSRR = srrRuntime != null ? srrRuntime.selectNext(todos, tiempoGlobal) : null;
                guardarCambiosPlanificador(todos);
                return seleccionadoSRR;

            case "HRRN":
                return hrrnRuntime.selectNext(todos, tiempoGlobal);

            case "Lottery":
                return lotteryRuntime != null ? lotteryRuntime.selectNext(todos, tiempoGlobal) : null;

            case "FCFS":
            default:
                return fcfsRuntime.selectNext(todos, tiempoGlobal);
        }
    }

    private void guardarCambiosPlanificador(java.util.List<BCP> procesos) {
        if (procesos == null || memoria == null) {
            return;
        }

        for (BCP proceso : procesos) {
            if (proceso != null) {
                memoria.actualizarBCPPorId(proceso);
            }
        }
    }

    private void aplicarPreempcionSRT() {
        if (!"SRT".equals(String.valueOf(cmbAlgoritmo.getSelectedItem())) || memoria == null) {
            return;
        }

        asegurarPlanificadorEjecucion();

        for (int i = 0; i < cpus.size(); i++) {
            CPU cpuActual = cpus.get(i);

            if (cpuActual == null
                    || cpuActual.getBcp() == null
                    || cpuActual.isInterrupcion()
                    || cpuActual.isProcesoFinalizado()) {
                continue;
            }

            BCP procesoActual = cpuActual.getBcp();
            BCP candidato = srtRuntime.selectNext(memoria.obtenerTodosBCPsEnMemoria(), tiempoGlobal);

            if (candidato == null) {
                continue;
            }

            int restanteActual = obtenerRafagaRestantePlanificacion(procesoActual);
            int restanteCandidato = obtenerRafagaRestantePlanificacion(candidato);

            if (restanteCandidato >= restanteActual) {
                continue;
            }

            procesoActual.setEstado("preparado");
            procesoActual.setCpuAsignado(-1);
            memoria.actualizarBCPPorId(procesoActual);
            dispatcher.actualizarBCP(procesoActual);

            cpuActual.liberarBcp();

            asignarTiempoInicioSiEsPrimeraVez(candidato);
            candidato.setIniciado(true);
            candidato.setCpuAsignado(i);
            cpuActual.CargarBcp(candidato);
            memoria.actualizarBCPPorId(cpuActual.getBcp());
            dispatcher.actualizarBCP(cpuActual.getBcp());
        }
    }

    private int obtenerRafagaRestantePlanificacion(BCP bcp) {
        if (bcp == null) {
            return Integer.MAX_VALUE;
        }

        if (bcp.getRafagaRestante() > 0) {
            return bcp.getRafagaRestante();
        }

        if (bcp.getRafagaTotal() > 0) {
            return bcp.getRafagaTotal();
        }

        if (bcp.getBase() >= 0 && bcp.getLimite() >= bcp.getBase()) {
            return bcp.getLimite() - bcp.getBase() + 1;
        }

        return Integer.MAX_VALUE;
    }

    private boolean ejecutarCicloDosCPUs() throws Exception {
        boolean ejecutoAlMenosUnaCPU = false;

        for (int i = 0; i < cpus.size(); i++) {
            CPU cpuActual = cpus.get(i);

            if (cpuActual == null || cpuActual.getBcp() == null) {
                continue;
            }

            if (cpuActual.isInterrupcion()) {
                resolverInterrupcionSalidaCPU(cpuActual);
                continue;
            }

            if (cpuActual.isProcesoFinalizado()) {
                continue;
            }

            boolean ejecuto = cpuActual.ejecutar();

            if (ejecuto) {
                ejecutoAlMenosUnaCPU = true;
                BCP procesoEjecutado = cpuActual.getBcp();

                if (procesoEjecutado != null && procesoEjecutado.getRafagaRestante() > 0) {
                    procesoEjecutado.setRafagaRestante(procesoEjecutado.getRafagaRestante() - 1);
                }
            }

            BCP bcpActualizado = cpuActual.getBcp();
            bcpActualizado.setCpuAsignado(i);
            memoria.actualizarBCPPorId(bcpActualizado);
            dispatcher.actualizarBCP(bcpActualizado);

            // Manejo de quantum para algoritmos RR / SRR / Lottery
            if (algoritmoUsaQuantum(String.valueOf(cmbAlgoritmo.getSelectedItem()))) {
                BCP current = cpuActual.getBcp();

                if (current != null) {
                    int q = current.getQuantumRestante();
                    // Si no se inicializó al asignar, inicializar ahora
                    if (q <= 0) {
                        current.setQuantumRestante(obtenerQuantumSeleccionado());
                        q = current.getQuantumRestante();
                    }

                    // Decrementar quantum por ciclo ejecutado
                    current.setQuantumRestante(q - 1);

                    // Si gastó quantum y el proceso no terminó ni está en interrumpido, continuarlo
                    if (current.getQuantumRestante() <= 0 && !cpuActual.isProcesoFinalizado() && !cpuActual.isInterrupcion()) {
                        current.setEstado("preparado");
                        current.setCpuAsignado(-1);
                        memoria.actualizarBCPPorId(current);
                        dispatcher.actualizarBCP(current);

                        cpuActual.liberarBcp();
                    }
                }
            }
        }

        if (ejecutoAlMenosUnaCPU) {
            tiempoGlobal++;
        }

        return ejecutoAlMenosUnaCPU;
    }

    private void resolverInterrupcionSalidaCPU(CPU cpuActual) {
        if (cpuActual == null || cpuActual.getBcp() == null) {
            return;
        }

        if (!"10H".equals(cpuActual.getTipoInterrupcion())) {
            return;
        }

        final String valor = String.valueOf(cpuActual.getBcp().getDx());
        javax.swing.SwingUtilities.invokeLater(() -> imprimirTerminal(valor));

        cpuActual.resolverInterrupcion();
        memoria.actualizarBCPPorId(cpuActual.getBcp());
        dispatcher.actualizarBCP(cpuActual.getBcp());
    }

    private void finalizarProcesosTerminados() {
        for (CPU cpuActual : cpus) {
            if (cpuActual == null || cpuActual.getBcp() == null || !cpuActual.isProcesoFinalizado()) {
                continue;
            }

            BCP procesoFinalizado = cpuActual.getBcp();
            procesoFinalizado.setCpuAsignado(-1);
            procesoFinalizado.setRafagaRestante(0);

            if (procesoFinalizado.getTiempoFinal() < 0) {
                procesoFinalizado.setTiempoFinal(tiempoGlobal);
            }

            procesoFinalizado.setTurnaround(
                    procesoFinalizado.getTiempoFinal() - procesoFinalizado.getTiempoLlegada());
            procesoFinalizado.setTiempoEspera(
                    procesoFinalizado.getTurnaround() - procesoFinalizado.getRafagaTotal());
            procesoFinalizado.setTrTs(procesoFinalizado.getRafagaTotal() > 0
                    ? (double) procesoFinalizado.getTurnaround() / procesoFinalizado.getRafagaTotal()
                    : 0.0);

            dispatcher.MoverProceso(procesoFinalizado, memoria, disco);
            cpuActual.liberarBcp();
        }
    }

    private void btnEjecutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEjecutarActionPerformed
        if (!validarSistema()) 
            return;

        if (ejecucionEnCurso) {
            mostrarToast("Ya hay una ejecución en curso");
            return;
        }

        if (memoria.vacio()) {
            imprimirTerminal("No hay procesos listos. Cargue archivos o cambie de algoritmo para reiniciar.");
            mostrarToast("No hay procesos listos");
            return; 
        }

        ejecucionEnCurso = true;
        btnEjecutar.setEnabled(false);
        btnPasoPaso.setEnabled(false);
        cmbAlgoritmo.setEnabled(false);
        spnQuantum.setEnabled(false);
        cmbCantidadCpus.setEnabled(false);

        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!memoria.vacio() || hayCpuActiva()) {
                    finalizarProcesosTerminados();
                    asignarProcesosACpusDisponibles();
                    aplicarPreempcionSRT();

                    if (!hayCpuActiva()) {
                        break;
                    }

                    ejecutarCicloDosCPUs();
                    finalizarProcesosTerminados();

                    Thread.sleep(TIEMPO_ESPERA_MS);

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        actualizarBCP();
                        actualizarTablaProcesos();
                        actualizarTablaMemoria();
                        actualizarTablaDisco();
                        actualizarTablaVirtual();
                    });
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    guardarEstadisticasEjecucionReal();
                    imprimirTerminal("Todos los procesos finalizados. Estadísticas guardadas.");
                    mostrarToast("Ejecución finalizada");
                    ejecucionEnCurso = false;
                    btnEjecutar.setEnabled(true);
                    btnPasoPaso.setEnabled(true);
                    cmbAlgoritmo.setEnabled(true);
                    spnQuantum.setEnabled(true);
                    cmbCantidadCpus.setEnabled(true);
                    actualizarVisibilidadQuantum();
                });
                return null;
            }
        };
        worker.execute();
    }//GEN-LAST:event_btnEjecutarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        if (!sistemaIniciado) return;
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this, "¿Desea reiniciar el sistema?", "Limpiar",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;

        sistemaIniciado = false;
        memoria = null; disco = null; cpu = null; dispatcher = null;
        cpus.clear();
        cpuSeleccionado = 0;
        resetPlanificadorEjecucion();
        actualizarSelectorCpu();
        tiempoGlobal = 0;
        coloresPorBCP.clear();
        procesosBasePlanificacion.clear();
        archivosBasePlanificacion.clear();
        tiemposLlegadaPorArchivo.clear();
        historialEstadisticas.clear();
        firmasEstadisticasGuardadas.clear();
        contadorEjecucionesEstadisticas = 0;
        ultimaFirmaEstadisticas = "";
        ejecucionEnCurso = false;
        terminalArea.setText("");
        limpiarTabla(tablaMemoria);
        limpiarTabla(tablaMemoriaVirtual);
        limpiarTabla(tablaDisco);
        limpiarTabla(tablaProcesos);
        limpiarBCP();
        terminalInput.setEnabled(false);
        btnEnviar.setEnabled(false);
        cargarConfiguracionInicial();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnPasoPasoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasoPasoActionPerformed
        if (cpus != null) {
            if (!validarSistema()) {
                return;
            }

            if (memoria.vacio() && !hayCpuActiva()) {
                imprimirTerminal("No hay procesos en cola.");
                mostrarToast("No hay procesos para ejecutar");
                return;
            }

            try {
                finalizarProcesosTerminados();
                asignarProcesosACpusDisponibles();
                aplicarPreempcionSRT();

                if (!hayCpuActiva()) {
                    imprimirTerminal("Todos los procesos finalizados.");
                    mostrarToast("Ejecucion finalizada");
                } else {
                    ejecutarCicloDosCPUs();
                    finalizarProcesosTerminados();
                }

                actualizarBCP();
                actualizarTablaProcesos();
                actualizarTablaMemoria();
                actualizarTablaDisco();
                actualizarTablaVirtual();

            } catch (Exception e) {
                imprimirTerminal("Error en paso a paso: " + e.getMessage());
                mostrarToast("Error en paso a paso");
            }

            return;
        }

        if (!validarSistema()) return;
        if (memoria.vacio()) {
            imprimirTerminal("No hay procesos en cola.");
            mostrarToast("No hay procesos para ejecutar");
            return;
        }

        BCP bcp = memoria.obtenerPrimerBCP();
        if (bcp == null) return;

        if (cpu.getBcp() == null || !cpu.getBcp().getIdProceso().equals(bcp.getIdProceso())) {
            asignarTiempoInicioSiEsPrimeraVez(bcp);

            cpu.CargarBcp(bcp);
            memoria.actualizarPrimerBCP(cpu.getBcp());
            dispatcher.actualizarBCP(cpu.getBcp());
        }

        if (cpu.isInterrupcion()) {
            if (cpu.getTipoInterrupcion().equals("09H")) {
                imprimirTerminal("Esperando entrada de teclado...");
                mostrarToast("Entrada requerida");
                activarEntrada();
            } else {
                imprimirTerminal("Proceso en espera.");
                mostrarToast("Proceso en espera");
            }
            return;
        }

        if (cpu.isProcesoFinalizado()) {
            dispatcher.Mover(memoria, disco);
            imprimirTerminal("Proceso finalizado.");
            mostrarToast("Proceso finalizado");

            actualizarBCP();
            actualizarTablaProcesos();
            actualizarTablaMemoria();
            actualizarTablaDisco();
            actualizarTablaVirtual();

            return;
        } else {
            boolean ejecuto = cpu.ejecutar();
            if (ejecuto) {
                tiempoGlobal++;
            }

            if (cpu.isInterrupcion() && cpu.getTipoInterrupcion().equals("10H")) {
                imprimirTerminal(String.valueOf(cpu.getBcp().getDx()));
                cpu.resolverInterrupcion();

                memoria.actualizarPrimerBCP(cpu.getBcp());
                dispatcher.actualizarBCP(cpu.getBcp());
            }

            if (cpu.isInterrupcion() && cpu.getTipoInterrupcion().equals("09H")) {
                activarEntrada();
            }

            memoria.actualizarPrimerBCP(cpu.getBcp());
            dispatcher.actualizarBCP(cpu.getBcp());
        }

        actualizarBCP();
        actualizarTablaProcesos();
        actualizarTablaMemoria();
        actualizarTablaDisco();

    }//GEN-LAST:event_btnPasoPasoActionPerformed

    private void btnCargarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarConfigActionPerformed
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Seleccionar configuración JSON");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON", "json"));

        int resultado = fc.showOpenDialog(this);

        if (resultado != javax.swing.JFileChooser.APPROVE_OPTION) {
            // Si cancela usa valores por defecto.
            inicializarSistema(DEFAULT_MEMORIA, DEFAULT_VIRTUAL, DEFAULT_DISCO);
            imprimirTerminal("Usando valores por defecto.");
            mostrarToast("Configuracion por defecto aplicada");
            return;
        }

        java.io.File archivo = fc.getSelectedFile();

        try {
            String contenido = new String(java.nio.file.Files.readAllBytes(archivo.toPath()));

            int sizeMemoria = extraerValorJSON(contenido, "size_memoria", DEFAULT_MEMORIA);
            int sizeVirtual = extraerValorJSON(contenido, "size_memoriavirtual", DEFAULT_VIRTUAL);
            int sizeDisco   = extraerValorJSON(contenido, "size_disco", DEFAULT_DISCO);

            String strategy = extraerTextoJSON(contenido, "strategy", DEFAULT_STRATEGY);
            int pageSize = extraerValorJSON(contenido, "page_size", DEFAULT_PAGE_SIZE);
            int countPartitions = extraerValorJSON(contenido, "count_partitions", DEFAULT_COUNT_PARTITIONS);
            int[] partitionSizes = extraerArregloJSON(contenido, "partition_sizes", DEFAULT_PARTITION_SIZES);

            inicializarSistema(
                    sizeMemoria,
                    sizeVirtual,
                    sizeDisco,
                    strategy,
                    pageSize,
                    countPartitions,
                    partitionSizes
            );

            imprimirTerminal("Configuración cargada: " + archivo.getName());
            mostrarToast("Configuracion aplicada");

        } catch (Exception e) {
            inicializarSistema(DEFAULT_MEMORIA, DEFAULT_VIRTUAL, DEFAULT_DISCO);
            imprimirTerminal("Error al leer JSON, usando valores por defecto.");
            mostrarToast("Configuracion por defecto aplicada");
        }
    }//GEN-LAST:event_btnCargarConfigActionPerformed

    private void terminalInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_terminalInputActionPerformed
        procesarEntradaTeclado();
    }//GEN-LAST:event_terminalInputActionPerformed
    private void procesarEntradaTeclado() {
        if (cpu == null || !cpu.isInterrupcion() || !"09H".equals(cpu.getTipoInterrupcion())) {
            terminalInput.setText("");
            return;
        }

        try {
            int valor = Integer.parseInt(terminalInput.getText().trim());

            if (valor < 0 || valor > 255) {
                terminalInput.setText("");
                return;
            }

            cpu.recibirEntradaTeclado(valor);

            memoria.actualizarPrimerBCP(cpu.getBcp());
            dispatcher.actualizarBCP(cpu.getBcp());

            desactivarEntrada();

            actualizarBCP();
            actualizarTablaProcesos();
            actualizarTablaMemoria();
            actualizarTablaDisco();

        } catch (NumberFormatException e) {
            terminalInput.setText("");
        }
    }
    private void btnEstadisticasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEstadisticasActionPerformed
        if (!validarSistema()) return;

        if (historialEstadisticas.isEmpty()) {
            imprimirTerminal("No hay estadísticas guardadas. Ejecute primero los procesos.");
            mostrarToast("Ejecute primero");
            return;
        }

        mostrarEstadisticasAcumuladasModal();
    }//GEN-LAST:event_btnEstadisticasActionPerformed

    private void btnCargarArchivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarArchivosActionPerformed
        if (!validarSistema()) return;
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Seleccionar archivos .asm");
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ASM", "asm"));

        int resultado = fc.showOpenDialog(this);
        if (resultado != javax.swing.JFileChooser.APPROVE_OPTION) return;

        java.io.File[] archivos = fc.getSelectedFiles();
        int cargados = 0;
        java.util.List<BCP> procesosCargadosAhora = new java.util.ArrayList<>();

        for (java.io.File archivo : archivos) {
            BCP bcp = parser.Leer(archivo, memoria, disco);
            if (bcp != null) {

                // El BCP se guarda en la cola/lista del Dispatcher.
                dispatcher.registrarProceso(bcp);

                registrarArchivoBase(archivo);

                // Se guarda una copia base para poder comparar algoritmos sin volver a cargar archivos.
                registrarProcesoBaseParaPlanificacion(bcp);
                procesosCargadosAhora.add(bcp);

                // El BCP vive en memoria de kernel aunque las instrucciones esten en virtual.
                if (!memoria.lleno()) {
                    memoria.agregarBCP(bcp);
                }

                cargados++;
            }
        }

        if (cargados > 0) {
            mostrarModalTiemposLlegada(procesosCargadosAhora);
            ultimaFirmaEstadisticas = "";
            actualizarTablaProcesos();
            actualizarTablaMemoria();
            actualizarTablaDisco();
            actualizarTablaVirtual();
            actualizarBCP();
        }

        imprimirTerminal("Archivos cargados: " + cargados + "/" + archivos.length);
        mostrarToast(cargados + " archivo(s) cargado(s)");

    }//GEN-LAST:event_btnCargarArchivosActionPerformed

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        try {
            int valor = Integer.parseInt(terminalInput.getText().trim());
            if (valor < 0 || valor > 255) {
                imprimirTerminal("ERROR: valor fuera de rango (0-255)");
                mostrarToast("Entrada fuera de rango");
                return;
            }
            cpu.recibirEntradaTeclado(valor);
            desactivarEntrada();
            actualizarBCP();
        } catch (NumberFormatException e) {
            imprimirTerminal("ERROR: ingrese un número válido (0-255)");
            mostrarToast("Entrada invalida");
        }
    }//GEN-LAST:event_btnEnviarActionPerformed


    // Variables declaration - do not modify                     
    private javax.swing.JLabel ac;
    private javax.swing.JLabel al;
    private javax.swing.JLabel ax;
    private javax.swing.JButton btnCargarArchivos;
    private javax.swing.JButton btnCargarConfig;
    private javax.swing.JButton btnCpuAnterior;
    private javax.swing.JButton btnCpuSiguiente;
    private javax.swing.JButton btnEjecutar;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnEstadisticas;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnPasoPaso;
    private javax.swing.JLabel bx;
    private javax.swing.JLabel cx;
    private javax.swing.JLabel dx;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JLabel lblBcpArchivos;
    private javax.swing.JLabel lblBcpBase;
    private javax.swing.JLabel lblBcpCpu;
    private javax.swing.JLabel lblBcpEstado;
    private javax.swing.JLabel lblBcpId;
    private javax.swing.JLabel lblBcpLimite;
    private javax.swing.JLabel lblBcpNombre;
    private javax.swing.JLabel lblBcpPila;
    private javax.swing.JLabel lblBcpPrioridad;
    private javax.swing.JLabel lblBcpSiguiente;
    private javax.swing.JLabel lblBcpTiempoEmpleado;
    private javax.swing.JLabel lblBcpTiempoInicio;
    private javax.swing.JLabel lblCpuSeleccionado;
    private javax.swing.JPanel panelSelectorCpu;
    private javax.swing.JTable tablaDisco;
    private javax.swing.JTable tablaMemoria;
    private javax.swing.JTable tablaMemoriaVirtual;
    private javax.swing.JTable tablaProcesos;
    private javax.swing.JTextArea terminalArea;
    private javax.swing.JTextField terminalInput;
}
