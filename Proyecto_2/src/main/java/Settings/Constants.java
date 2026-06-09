package Settings;

public final class Constants {
    private Constants() {
    }

    public static final int DEFAULT_MEMORIA = 512;
    public static final int DEFAULT_VIRTUAL = 64;
    public static final int DEFAULT_DISCO = 512;
    public static final int DEFAULT_CPU_COUNT = 4;
    public static final int MAX_CPU_COUNT = 4;
    public static final String[] CPU_OPTIONS = {"1", "2", "3", "4"};
    public static final String DEFAULT_SETTINGS_PATH = "src/main/java/Settings/Ajuste.json";

    public static final String DEFAULT_STRATEGY = "Best_Fit";
    public static final int DEFAULT_PAGE_SIZE = 4;
    public static final int DEFAULT_COUNT_PARTITIONS = 4;
    public static final int[] DEFAULT_PARTITION_SIZES = {20, 30, 50};

    public static final int INICIO_SO = 0;
    public static final int LIMITE_SO = 149;
    public static final int TAMANO_BCP = 30;
    public static final int MAX_PROCESOS = 5;
    public static final int STACK_SIZE = 5;
    public static final int MAX_PAGES_PER_PROCESS = 200;

    public static final int DEFAULT_QUANTUM = 2;
    public static final int MAX_QUANTUM = 100;
    public static final int SRR_NEW_PRIORITY_RATE = 1;
    public static final int SRR_ACCEPTED_PRIORITY_RATE = 3;
    public static final int TIEMPO_ESPERA_MS = 750;

    public static final java.awt.Color COLOR_EJECUCION = new java.awt.Color(255, 249, 196);
    public static final java.awt.Color COLOR_INSTRUCCION = new java.awt.Color(255, 236, 179);
    public static final java.awt.Color COLOR_TEXTO_BASE = new java.awt.Color(44, 57, 75);
    public static final java.awt.Color COLOR_TOAST = java.awt.Color.BLACK;
    public static final java.awt.Color COLOR_HUECO_LIBRE = new java.awt.Color(224, 224, 224);

    public static final String[] CAMPOS_BCP_KERNEL = {
        "idProceso",
        "nombreProceso",
        "estado",
        "base",
        "limite",
        "pc",
        "ir",
        "ac",
        "ax",
        "bx",
        "cx",
        "dx",
        "al",
        "ah",
        "prioridad",
        "tiempoInicio",
        "tiempoEmpleado",
        "pila",
        "archivosAbiertos",
        "tiempoLlegada",
        "tiempoFinal",
        "rafagaTotal",
        "rafagaRestante",
        "tiempoEspera",
        "turnaround",
        "trTs",
        "tickets",
        "quantumRestante",
        "iniciado",
        "siguienteBCP"
    };

    public static final java.awt.Color[] COLORES_BLOQUES = {
        new java.awt.Color(200, 230, 201),
        new java.awt.Color(187, 222, 251),
        new java.awt.Color(255, 224, 178),
        new java.awt.Color(225, 190, 231),
        new java.awt.Color(178, 235, 242),
        new java.awt.Color(255, 205, 210),
        new java.awt.Color(220, 237, 200),
        new java.awt.Color(209, 196, 233)
    };

    public static final java.awt.Color[] COLORES_BCP = {
        new java.awt.Color(21, 101, 192),
        new java.awt.Color(46, 125, 50),
        new java.awt.Color(173, 20, 87),
        new java.awt.Color(106, 27, 154),
        new java.awt.Color(0, 121, 107),
        new java.awt.Color(198, 40, 40),
        new java.awt.Color(239, 108, 0),
        new java.awt.Color(69, 90, 100),
        new java.awt.Color(40, 53, 147),
        new java.awt.Color(85, 139, 47),
        new java.awt.Color(0, 96, 100),
        new java.awt.Color(93, 64, 55)
    };
}
