/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Memoria;

import CPU.BCP;
import Memoria.Partition;
import Memoria.BestFit;
import Memoria.Pagination;



/**
 *
 * @author brasl
 */
public class Memoria {
    private String[] memoria;
    private int tamaño;
    private static final int inicio_SO = 0;
    private static final int limite_SO = 149;
    private static final int tamaño_BCP = 30;
    private static final int n_Procesos = 5;

    private int punteroSO;
    private int inicioUsuario;
    private int punteroUsuario;
    
    // Best_Fit , Pagination , Partition_Equal, Partition_Different
    private String Strategy;
    private Partition partitio_Strategy;
    private BestFit BestFit_Strategy;
    
    private Pagination Pagination_Strategy;
    private Disco disco;
    

    /**
    * Inicializa la memoria del sistema y del usuario
    */
    public Memoria (int tamaño, String Strategy,Disco disco ){
        this.tamaño = tamaño;
        this.memoria = new String[tamaño];
        this.punteroSO = 0;
        this.inicioUsuario = limite_SO + 1;
        this.punteroUsuario = inicioUsuario;
        this.Strategy = Strategy;
        this.partitio_Strategy = null;
        this.Pagination_Strategy = null;
        this.BestFit_Strategy = null;
        this.disco = disco;
    }

    /**
     * Agrega un BCP a la memoria del sistema, asegurando que no se exceda el límite de procesos permitidos. Si la memoria del sistema está llena, retorna false.
     * @param bcp
     * @return
     */
    public boolean agregarBCP(BCP bcp) {
        if (lleno()) {
            return false;
        }
        if (punteroSO > inicio_SO) {
            int campoSiguienteAnterior = punteroSO - 1; 
            memoria[campoSiguienteAnterior] = bcp.getIdProceso();
        }
        guardarBCP(bcp, punteroSO);
        memoria[punteroSO + tamaño_BCP - 1] = "null";

        punteroSO += tamaño_BCP;
        return true;
    }

    /**
     * Elimina el primer BCP de la memoria del sistema.
     */
    public void eliminarPrimerBCP() {
        int cantidadBCPs = punteroSO / tamaño_BCP;
        for (int bcp = 1; bcp < cantidadBCPs; bcp++) {
            int origenBloque = inicio_SO + (bcp * tamaño_BCP);
            int destinoBloque = inicio_SO + ((bcp - 1) * tamaño_BCP);
            for (int campo = 0; campo < tamaño_BCP; campo++) {
                memoria[destinoBloque + campo] = memoria[origenBloque + campo];
            }
        }
        int inicioUltimo = punteroSO - tamaño_BCP;
        for (int i = inicioUltimo; i < punteroSO; i++) {
            memoria[i] = "";
        }
        punteroSO -= tamaño_BCP;
        reconstruirEnlacesBCP();
    }

    /**
     * Reconstruye los enlaces entre los BCPs en la memoria del sistema después de eliminar el primer BCP, asegurando que el campo de enlace del último BCP apunte a "null".
     */
    private void reconstruirEnlacesBCP() {
        int cantidadBCPs = punteroSO / tamaño_BCP;

        for (int i = 0; i < cantidadBCPs; i++) {
            int inicioActual = inicio_SO + (i * tamaño_BCP);
            int campoSiguiente = inicioActual + tamaño_BCP - 1;

            if (i < cantidadBCPs - 1) {
                int inicioSiguiente = inicio_SO + ((i + 1) * tamaño_BCP);
                memoria[campoSiguiente] = memoria[inicioSiguiente];
            } else {
                memoria[campoSiguiente] = "null";
            }
        }
    }

    /**
     * Lee el ID del primer BCP en la memoria del sistema, o retorna una cadena vacía si no hay ningún BCP presente.
     * @return
     */
    public String leerIdPrimerBCP() {
        if (punteroSO == inicio_SO) return "";
        return memoria[inicio_SO];
    }

    /**
     * Verifica si la memoria del sistema está llena, es decir, si el puntero de sistema ha alcanzado o superado el límite permitido para el número máximo de procesos.
     * @return
     */
    public boolean lleno() {
        return punteroSO >= inicio_SO + (n_Procesos * tamaño_BCP);
    }

    /**
     * Verifica si la memoria del sistema está vacía, es decir, si el puntero de sistema está en su posición inicial sin ningún BCP cargado.
     * @return
     */
    public boolean vacio() {
        return punteroSO == inicio_SO;
    }

    public void actualizarPrimerBCP(BCP bcp) {
        guardarBCP(bcp, inicio_SO);
    }

    /**
     * Obtiene el primer BCP de la memoria del sistema, o retorna null si no hay ningún BCP presente.
     * @return
     */
    public BCP obtenerPrimerBCP() {
        if (punteroSO == 0 ) 
            return null;
        int p = inicio_SO;
        String idProceso     = memoria[p++];
        String nombreProceso = memoria[p++];
        String estado        = memoria[p++];
        int base             = Integer.parseInt(memoria[p++]);
        int limite           = Integer.parseInt(memoria[p++]);
        int pc               = Integer.parseInt(memoria[p++]);
        String ir            = memoria[p++];
        int ac               = Integer.parseInt(memoria[p++]);
        int ax               = Integer.parseInt(memoria[p++]);
        int bx               = Integer.parseInt(memoria[p++]);
        int cx               = Integer.parseInt(memoria[p++]);
        int dx               = Integer.parseInt(memoria[p++]);
        int al               = Integer.parseInt(memoria[p++]);
        int ah               = Integer.parseInt(memoria[p++]);

        int prioridad        = Integer.parseInt(memoria[p++]);
        int tiempoInicio     = Integer.parseInt(memoria[p++]);
        int tiempoEmpleado   = Integer.parseInt(memoria[p++]);

        String pilaStr = memoria[p++];
        int[] pila = new int[5];
        int puntero_pila = -1;
        if (pilaStr != null && !pilaStr.isEmpty()) {
            String[] vals = pilaStr.split(",");
            for (int i = 0; i < vals.length; i++) {
                pila[i] = Integer.parseInt(vals[i]);
                puntero_pila = i;
            }
        }

        String archStr = memoria[p++];
        java.util.List<String> archivos = new java.util.ArrayList<>();
        if (archStr != null && !archStr.isEmpty()) {
            for (String arch : archStr.split(",")) {
                archivos.add(arch);
            }
        }

        // nuevos campos PY2
        int tiempoLlegada = Integer.parseInt(memoria[p++]);
        int tiempoFinal = Integer.parseInt(memoria[p++]);
        int rafagaTotal = Integer.parseInt(memoria[p++]);
        int rafagaRestante = Integer.parseInt(memoria[p++]);
        int tiempoEspera = Integer.parseInt(memoria[p++]);
        int turnaround = Integer.parseInt(memoria[p++]);
        double trTs = Double.parseDouble(memoria[p++]);
        int tickets = Integer.parseInt(memoria[p++]);
        int quantumRestante = Integer.parseInt(memoria[p++]);
        boolean iniciado = Boolean.parseBoolean(memoria[p++]);

        // saltar campo siguiente BCP
        p++;

        BCP bcp = new BCP(idProceso, nombreProceso, estado, base, limite, pc, prioridad);
        bcp.setIr(ir);
        bcp.setAc(ac);
        bcp.setAx(ax);
        bcp.setBx(bx);
        bcp.setCx(cx);
        bcp.setDx(dx);
        bcp.setAl(al);
        bcp.setAh(ah);
        bcp.setTiempoInicio(tiempoInicio);
        bcp.setTiempoEmpleado(tiempoEmpleado);
        bcp.setPilaDirecta(pila, puntero_pila);
        bcp.setArchivosAbiertos(archivos);
        bcp.setTiempoLlegada(tiempoLlegada);
        bcp.setTiempoFinal(tiempoFinal);
        bcp.setRafagaTotal(rafagaTotal);
        bcp.setRafagaRestante(rafagaRestante);
        bcp.setTiempoEspera(tiempoEspera);
        bcp.setTurnaround(turnaround);
        bcp.setTrTs(trTs);
        bcp.setTickets(tickets);
        bcp.setQuantumRestante(quantumRestante);
        bcp.setIniciado(iniciado);
        return bcp;
    }

    /**
     * Guarda un BCP en la zona del sistema operativo.
     * El tamaño actual de cada BCP es de 30 posiciones.
     *
     * @param bcp BCP a guardar.
     * @param pos posición inicial donde se guarda el BCP.
     */
    private void guardarBCP(BCP bcp, int pos) {
        int p = pos;
        memoria[p++] = bcp.getIdProceso();
        memoria[p++] = bcp.getNombreProceso();
        memoria[p++] = bcp.getEstado();
        memoria[p++] = String.valueOf(bcp.getBase());
        memoria[p++] = String.valueOf(bcp.getLimite());
        memoria[p++] = String.valueOf(bcp.getPc());
        memoria[p++] = String.valueOf(bcp.getIr());
        memoria[p++] = String.valueOf(bcp.getAc());
        memoria[p++] = String.valueOf(bcp.getAx());
        memoria[p++] = String.valueOf(bcp.getBx());
        memoria[p++] = String.valueOf(bcp.getCx());
        memoria[p++] = String.valueOf(bcp.getDx());
        memoria[p++] = String.valueOf(bcp.getAl());
        memoria[p++] = String.valueOf(bcp.getAh());

        memoria[p++] = String.valueOf(bcp.getPrioridad());
        memoria[p++] = String.valueOf(bcp.getTiempoInicio());
        memoria[p++] = String.valueOf(bcp.getTiempoEmpleado());

        int[] pila = bcp.getPila();
        StringBuilder pilaStr = new StringBuilder();
        for (int i = 0; i <= bcp.getPuntero_pila(); i++) {
            if (i > 0) pilaStr.append(",");
            pilaStr.append(pila[i]);
        }
        memoria[p++] = pilaStr.toString();

        StringBuilder archStr = new StringBuilder();
        java.util.List<String> arch = bcp.getArchivosAbiertos();
        for (int i = 0; i < arch.size(); i++) {
            if (i > 0) archStr.append(",");
            archStr.append(arch.get(i));
        }
        memoria[p++] = archStr.toString();
      
        memoria[p++] = String.valueOf(bcp.getTiempoLlegada());
        memoria[p++] = String.valueOf(bcp.getTiempoFinal());
        memoria[p++] = String.valueOf(bcp.getRafagaTotal());
        memoria[p++] = String.valueOf(bcp.getRafagaRestante());
        memoria[p++] = String.valueOf(bcp.getTiempoEspera());
        memoria[p++] = String.valueOf(bcp.getTurnaround());
        memoria[p++] = String.valueOf(bcp.getTrTs());
        memoria[p++] = String.valueOf(bcp.getTickets());
        memoria[p++] = String.valueOf(bcp.getQuantumRestante());
        memoria[p++] = String.valueOf(bcp.isIniciado());

        memoria[p] = bcp.getSiguienteBCP() != null
                ? bcp.getSiguienteBCP().getIdProceso() : "null";
    }

    public boolean hayEspacioUsuario(int cantidadInstrucciones) {
        return (punteroUsuario + cantidadInstrucciones) <= tamaño;
    }

    /**
     * Carga un conjunto de instrucciones en la zona de usuario de la memoria.
     * Este método pertenece a la estrategia vieja/default.
     *
     * Si se usa Best_Fit, Partition_Equal o Partition_Different, se recomienda
     * llamar asignarProceso().
     *
     * @param instrucciones instrucciones del proceso.
     * @return arreglo {base, limite} si se pudo cargar; null si no hay espacio.
     */
    public int[] cargarInstruccionesSiCabe(String[] instrucciones) {
        if (!hayEspacioUsuario(instrucciones.length)) {
            return null;
        }
        int base = punteroUsuario;
        for (String instruccion : instrucciones) {
            memoria[punteroUsuario++] = instruccion;
        }
        int limite = base + instrucciones.length - 1;
        return new int[]{base, limite};
    }

    /**
     * Libera el espacio ocupado por un proceso que se acaba de finalizar en la zona de usuario de la memoria.
     * Este método compacta la memoria y se mantiene para la estrategia Default.
     *
     * @param procesoTerminado proceso que terminó.
     */
    public void liberarYCompactarProceso(BCP procesoTerminado) {
        if (procesoTerminado == null) return;
        int baseLiberada = procesoTerminado.getBase();
        int limiteLiberado = procesoTerminado.getLimite();

        if (baseLiberada < inicioUsuario || limiteLiberado < baseLiberada) {
            return;
        }

        if (limiteLiberado >= tamaño) {
            limiteLiberado = tamaño - 1;
        }

        int cantidadLiberada = limiteLiberado - baseLiberada + 1;
        for (int i = baseLiberada; i <= limiteLiberado; i++) {
            memoria[i] = "";
        }

        for (int i = limiteLiberado + 1; i < punteroUsuario; i++) {
            memoria[i - cantidadLiberada] = memoria[i];
            memoria[i] = "";
        }

        punteroUsuario -= cantidadLiberada;
        if (punteroUsuario < inicioUsuario) {
            punteroUsuario = inicioUsuario;
        }
        actualizarDireccionesBCPDespuesDeCompactar(baseLiberada, cantidadLiberada);
    }

    /**
     * Actualiza las direcciones base, límite y PC de los BCPs en memoria del sistema después de compactar la zona de usuario.
     * @param baseLiberada
     * @param cantidadLiberada
     */
    private void actualizarDireccionesBCPDespuesDeCompactar(int baseLiberada, int cantidadLiberada) {
        int cantidadBCPs = punteroSO / tamaño_BCP;

        for (int i = 0; i < cantidadBCPs; i++) {
            int pos = inicio_SO + (i * tamaño_BCP);

            if (memoria[pos] == null || memoria[pos].isEmpty()) {
                continue;
            }

            int base = parseIntSeguro(memoria[pos + 3]);
            int limite = parseIntSeguro(memoria[pos + 4]);
            int pc = parseIntSeguro(memoria[pos + 5]);

            if (base > baseLiberada) {
                memoria[pos + 3] = String.valueOf(base - cantidadLiberada);
                memoria[pos + 4] = String.valueOf(limite - cantidadLiberada);
                memoria[pos + 5] = String.valueOf(pc - cantidadLiberada);
            }
        }
    }

    /**
     * Convertir una cadena a entero de forma segura, retornando 0 si la cadena es nula, vacía o no es un número válido.
     * @return
     */
    private int parseIntSeguro(String valor) {
        if (valor == null || valor.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Obtiene una lista de todos los BCPs actualmente almacenados en la memoria del sistema.
     *
     * IMPORTANTE:
     * Este método también lee los campos nuevos del PY2 para que no se pierdan:
     * tiempoLlegada, tiempoFinal, rafagaTotal, rafagaRestante, tiempoEspera,
     * turnaround, trTs, tickets, quantumRestante e iniciado.
     *
     * @return lista de BCPs en memoria.
     */
    public java.util.List<BCP> obtenerTodosBCPsEnMemoria() {
        java.util.List<BCP> lista = new java.util.ArrayList<>();
        int cantidadBCPs = punteroSO / tamaño_BCP;

        for (int i = 0; i < cantidadBCPs; i++) {
            int pos = inicio_SO + (i * tamaño_BCP);
            try {
                if (memoria[pos] == null || memoria[pos].isEmpty()) continue;

                int p = pos;
                String idProceso = memoria[p++];
                String nombreProceso = memoria[p++];
                String estado = memoria[p++];
                int base = Integer.parseInt(memoria[p++]);
                int limite = Integer.parseInt(memoria[p++]);
                int pc = Integer.parseInt(memoria[p++]);
                String ir = memoria[p++];
                int ac = Integer.parseInt(memoria[p++]);
                int ax = Integer.parseInt(memoria[p++]);
                int bx = Integer.parseInt(memoria[p++]);
                int cx = Integer.parseInt(memoria[p++]);
                int dx = Integer.parseInt(memoria[p++]);
                int al = Integer.parseInt(memoria[p++]);
                int ah = Integer.parseInt(memoria[p++]);
                int prioridad = Integer.parseInt(memoria[p++]);
                int tiempoInicio = Integer.parseInt(memoria[p++]);
                int tiempoEmpleado = Integer.parseInt(memoria[p++]);

                String pilaStr = memoria[p++];
                int[] pila = new int[5];
                int punteroPila = -1;
                if (pilaStr != null && !pilaStr.isEmpty()) {
                    String[] vals = pilaStr.split(",");
                    for (int j = 0; j < vals.length; j++) {
                        pila[j] = Integer.parseInt(vals[j]);
                        punteroPila = j;
                    }
                }

                String archStr = memoria[p++];
                java.util.List<String> archivos = new java.util.ArrayList<>();
                if (archStr != null && !archStr.isEmpty()) {
                    for (String arch : archStr.split(",")) {
                        archivos.add(arch);
                    }
                }

                // nuevos campos PY2
                int tiempoLlegada = Integer.parseInt(memoria[p++]);
                int tiempoFinal = Integer.parseInt(memoria[p++]);
                int rafagaTotal = Integer.parseInt(memoria[p++]);
                int rafagaRestante = Integer.parseInt(memoria[p++]);
                int tiempoEspera = Integer.parseInt(memoria[p++]);
                int turnaround = Integer.parseInt(memoria[p++]);
                double trTs = Double.parseDouble(memoria[p++]);
                int tickets = Integer.parseInt(memoria[p++]);
                int quantumRestante = Integer.parseInt(memoria[p++]);
                boolean iniciado = Boolean.parseBoolean(memoria[p++]);

                // saltar siguiente BCP
                p++;

                BCP bcp = new BCP(idProceso, nombreProceso, estado, base, limite, pc, prioridad);
                bcp.setIr(ir);
                bcp.setAc(ac);
                bcp.setAx(ax);
                bcp.setBx(bx);
                bcp.setCx(cx);
                bcp.setDx(dx);
                bcp.setAl(al);
                bcp.setAh(ah);
                bcp.setTiempoInicio(tiempoInicio);
                bcp.setTiempoEmpleado(tiempoEmpleado);
                bcp.setPilaDirecta(pila, punteroPila);
                bcp.setArchivosAbiertos(archivos);

                bcp.setTiempoLlegada(tiempoLlegada);
                bcp.setTiempoFinal(tiempoFinal);
                bcp.setRafagaTotal(rafagaTotal);
                bcp.setRafagaRestante(rafagaRestante);
                bcp.setTiempoEspera(tiempoEspera);
                bcp.setTurnaround(turnaround);
                bcp.setTrTs(trTs);
                bcp.setTickets(tickets);
                bcp.setQuantumRestante(quantumRestante);
                bcp.setIniciado(iniciado);

                lista.add(bcp);
            } catch (Exception e) {
                System.out.println("Error leyendo BCP en memoria: " + e.getMessage());
            }
        }

        return lista;
    }

    /**
     * Crea la estrategia de memoria seleccionada en Strategy.
     *
     * Estrategias aceptadas:
     * - Best_Fit
     * - Pagination
     * - Partition_Equal
     * - Partition_Different
     *
     * Para Best_Fit:
     *      No necesita parámetros. Puede recibir 0 y null.
     *
     * Para Partition_Equal:
     *      countPartitions debe ser mayor a 0.
     *
     * Para Partition_Different:
     *      partitionSizes debe ser un arreglo con los tamaños de partición.
     *
     * Para Pagination:
     *      Queda pendiente hasta crear la clase Pagination.
     */
    public void Creation_Strategy(int countPartitions, int[] partitionSizes) {
        switch (Strategy) {
            case "Best_Fit":
                this.BestFit_Strategy = new BestFit(inicioUsuario, tamaño);
                this.partitio_Strategy = null;
                this.Pagination_Strategy = null;
                break;

            case "Pagination":
                
                this.Pagination_Strategy = new Pagination(
                    inicioUsuario,
                    tamaño,
                    countPartitions,
                    disco,
                    n_Procesos
            );
                this.BestFit_Strategy = null;
                this.partitio_Strategy = null;
                System.out.println("Pagination todavía no está implementado.");
                break;

            case "Partition_Equal":
                if (countPartitions <= 0) {
                    System.out.println("Error: debe indicar una cantidad válida de particiones.");
                    return;
                }
                this.partitio_Strategy = new Partition(inicioUsuario, tamaño, countPartitions);
                this.BestFit_Strategy = null;
                this.Pagination_Strategy = null;
                break;

            case "Partition_Different":
                if (partitionSizes == null || partitionSizes.length == 0) {
                    System.out.println("Error: debe indicar los tamaños de las particiones.");
                    return;
                }
                this.partitio_Strategy = new Partition(inicioUsuario, tamaño, partitionSizes);
                this.BestFit_Strategy = null;
                this.Pagination_Strategy = null;
                break;

            default:
                System.out.println("Error: estrategia no reconocida. Se usará memoria normal.");
                this.Strategy = "Default";
                this.partitio_Strategy = null;
                this.BestFit_Strategy = null;
                this.Pagination_Strategy = null;
                break;
        }
    }

    /**
     * Asigna un proceso en memoria según la estrategia seleccionada.
     *
     * Si Strategy es:
     * - Best_Fit: usa BestFit_Strategy.
     * - Partition_Equal o Partition_Different: usa partitio_Strategy.
     * - Pagination: queda pendiente.
     * - Default: usa la carga normal con punteroUsuario.
     *
     * @param bcp BCP del proceso.
     * @param instrucciones instrucciones del proceso.
     * @return arreglo {base, limite} si se asignó correctamente; null si no hay espacio.
     */
    public int[] asignarProceso(BCP bcp, String[] instrucciones) {
        if (bcp == null || instrucciones == null) {
            return null;
        }

        switch (Strategy) {
            case "Best_Fit":
                if (BestFit_Strategy == null) {
                    BestFit_Strategy = new BestFit(inicioUsuario, tamaño);
                }
                return BestFit_Strategy.assign(bcp, instrucciones, memoria);

            case "Partition_Equal":
            case "Partition_Different":
                if (partitio_Strategy == null) {
                    System.out.println("Error: la estrategia de partición no ha sido creada.");
                    return null;
                }
                return partitio_Strategy.asignar(bcp, instrucciones, memoria);

            case "Pagination":
                return Pagination_Strategy.assign(bcp, instrucciones, memoria);

            case "Default":
            default:
                int[] resultado = cargarInstruccionesSiCabe(instrucciones);

                if (resultado != null) {
                    bcp.setBase(resultado[0]);
                    bcp.setLimite(resultado[1]);
                    bcp.setPc(resultado[0]);
                    bcp.setEstado("preparado");
                }

                return resultado;
        }
    }

    /**
     * Libera la memoria usada por un proceso según la estrategia seleccionada.
     *
     * Si Strategy es:
     * - Best_Fit: libera el bloque ocupado y fusiona huecos.
     * - Partition_Equal o Partition_Different: libera la partición completa.
     * - Pagination: queda pendiente.
     * - Default: libera y compacta como en el proyecto anterior.
     *
     * @param bcp proceso a liberar.
     */
    public void liberarProceso(BCP bcp) {
        if (bcp == null) {
            return;
        }

        switch (Strategy) {
            case "Best_Fit":
                if (BestFit_Strategy != null) {
                    BestFit_Strategy.release(bcp, memoria);
                }
                break;

            case "Partition_Equal":
            case "Partition_Different":
                if (partitio_Strategy != null) {
                    partitio_Strategy.liberar(bcp, memoria);
                }
                break;

            case "Pagination":
                if (Pagination_Strategy != null) {
                    Pagination_Strategy.release(bcp, memoria);
                }
                break;

            case "Default":
            default:
                liberarYCompactarProceso(bcp);
                break;
        }
    }

    /**
     * Verifica si hay espacio para un proceso según la estrategia seleccionada.
     *
     * @param instrucciones instrucciones del proceso.
     * @return true si hay espacio, false si no.
     */
    public boolean hayEspacioParaProceso(String[] instrucciones) {
        if (instrucciones == null) {
            return false;
        }

        switch (Strategy) {
            case "Best_Fit":
                if (BestFit_Strategy == null) {
                    BestFit_Strategy = new BestFit(inicioUsuario, tamaño);
                }
                return BestFit_Strategy.hasSpace(instrucciones);

            case "Partition_Equal":
            case "Partition_Different":
                return partitio_Strategy != null && partitio_Strategy.hayEspacio(instrucciones);

            case "Pagination":
                return Pagination_Strategy != null && Pagination_Strategy.hasSpace(instrucciones);

            case "Default":
            default:
                return hayEspacioUsuario(instrucciones.length);
        }
    }

    /**
     * Lee la instrucción actual de un proceso.
     *
     * En Best_Fit y Partition, el PC del BCP es una dirección física.
     * En Pagination, el PC será lógico y debe traducirse con tabla de páginas.
     *
     * @param bcp proceso actual.
     * @return instrucción actual o null.
     */
    public String leerInstruccion(BCP bcp) {
        if (bcp == null) {
            return null;
        }

        switch (Strategy) {
            case "Pagination":
                if (Pagination_Strategy != null) {
                    return Pagination_Strategy.readInstruction(bcp, memoria);
                }
                return null;

            default:
                return leerMemoria(bcp.getPc());
        }
    }

    public String leerMemoria(int posicion) {
        if (posicion < 0 || posicion >= tamaño) return null;
        return memoria[posicion];
    }

    /**
     * Verifica si hay espacio para N instrucciones en zona usuario.
     */
    public boolean hayEspacio(int cantidad) {
        return (punteroUsuario + cantidad) <= tamaño;
    }

    public String[] getMemoria(){ 
        return memoria; 
    }

    public int gettamaño(){ 
        return tamaño; 
    }

    public int getPunteroSO(){ 
        return punteroSO; 
    }

    public int getPunteroUsuario(){ 
        return punteroUsuario; 
    }

    public int getInicioUsuario(){ 
        return inicioUsuario; 
    }

    public int getLimiteSO(){ 
        return limite_SO; 
    }

    public int getMaxProcesos(){ 
        return n_Procesos; 
    }

    public int gettamañoBCP(){ 
        return tamaño_BCP; 
    }

    public String getStrategy() {
        return Strategy;
    }

    public Partition getPartitio_Strategy() {
        return partitio_Strategy;
    }

    public BestFit getBestFit_Strategy() {
        return BestFit_Strategy;
    }
    public Pagination getPagination_Strategy() {
        return Pagination_Strategy;
    }

    public Disco getDisco() {
        return disco;
    }
}