/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CPU;

import Settings.Constants;

/**
 * Representa el Bloque de Control de Proceso (BCP) de un programa cargado en memoria.
 * Esta clase almacena la información básica de cada proceso, como su identificador,
 * nombre, estado actual, dirección base, límite de memoria y contador de programa.
 * 
 * @author brasl
 */
public class BCP {
    private String idProceso;
    private String nombreProceso;
    private String estado;
    private int base;
    private int limite;
    private int pc;
    private int ultimaDireccionEjecutada;
    private String ir;  
    private int ac;   
    private int ax;
    private int bx;
    private int cx;
    private int dx;
    private int al;
    private int Ah;
    private int[] pila;
    private int puntero_pila;
    private static final int size_pila = Constants.STACK_SIZE;
    private int cpuAsignado;
    private int tiempoInicio;
    private int tiempoEmpleado;
    private java.util.List<String> archivosAbiertos;
    private java.util.List<Integer> contenidosArchivos;
    private String archivoAbierto;
    
    private BCP siguienteBCP;
    private int prioridad;
    private String irTexto;
    
    /* implementaciones nuevas    
    */
    private int tiempoLlegada;
    private int tiempoFinal;
    private int rafagaTotal;
    private int rafagaRestante;
    private int tiempoEspera;
    private int turnaround;
    private double trTs;
    private int tickets;
    private int quantumRestante;
    private boolean iniciado;
    
    
    

    /**
    * Constructor de la clase BCP.
    * Inicializa un nuevo bloque de control de proceso con su identificador,
    * nombre, estado, posición base, límite de memoria y contador de programa.
    *
    */ 
    public BCP(String idProceso, String nombreProceso, String estado, int base, int limite, int pc, int prioridad) {
        this.idProceso = idProceso;
        this.nombreProceso = nombreProceso;
        this.estado = estado;
        this.base = base;
        this.limite = limite;
        this.pc = pc;
        this.ultimaDireccionEjecutada = -1;
        this.prioridad = prioridad;
        this.ir = "";
        this.ac = 0;
        this.ax = 0;
        this.bx = 0;
        this.cx = 0;
        this.dx = 0;
        this.al = 0;
        this.Ah = 0;
        this.pila = new int[size_pila];
        this.puntero_pila = -1;
        this.cpuAsignado = -1;
        this.tiempoInicio = 0;
        this.tiempoEmpleado = 0;
        this.archivosAbiertos = new java.util.ArrayList<>();
        this.contenidosArchivos = new java.util.ArrayList<>();
        this.archivoAbierto = "";
        this.siguienteBCP = null;
        this.irTexto = "";
        this.tiempoLlegada = 0;
        this.tiempoFinal = -1;
        this.rafagaTotal = 0;
        this.rafagaRestante = 0;
        this.tiempoEspera = 0;
        this.turnaround = 0;
        this.trTs = 0.0;
        this.tickets = 1;
        this.quantumRestante = 0;
        this.iniciado = false;


        
    }
    /**
    *Agrega un un valor a la pila del bcp
    * se permiten 5 elementos
    */ 
    
    public void AgregarPila (int valor){
        if (puntero_pila >= 5){
            javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "desbordamiento de pila" ,
                        "en proceso " + idProceso,
                        javax.swing.JOptionPane.ERROR_MESSAGE
                    );
        }
        else {
            pila[++puntero_pila] = valor;
        }
    }    

    /**
    *se eleimina un valor de la pila del bcp
    * se permiten 5 elementos
    */ 


    public int EliminarPila() throws Exception {
        if (puntero_pila < 0) {
            javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Error: pila vacía" ,
                        "en proceso " + idProceso,
                        javax.swing.JOptionPane.ERROR_MESSAGE
                    );
            throw new Exception("Error: pila vacía (Stack Underflow) en proceso " + idProceso);
        }
        else {
            return pila[puntero_pila--];
        }
    }
    
    
    
    

    public String getIdProceso() {
        return idProceso;
    }
    public String getNombreProceso() {
        return nombreProceso;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
    public int getBase() {
        return base;
    }
    public void setBase(int base) {
        this.base = base;
    }
    public int getLimite() {
        return limite;
    }
    public int getPc() {
        return pc;
    }
    public void setPc(int pc) {
        this.pc = pc;
    }
    public int getUltimaDireccionEjecutada() {
        return ultimaDireccionEjecutada;
    }
    public void setUltimaDireccionEjecutada(int ultimaDireccionEjecutada) {
        this.ultimaDireccionEjecutada = ultimaDireccionEjecutada;
    }
    public void setLimite(int limite) { 
        this.limite = limite; 
    }
    public String getIr() { 
        return ir; 
    }
    public void setIr(String ir) { 
        this.ir = ir; 
    }
    public int getAc() { 
        return ac; 
    }
    public void setAc(int ac) { 
        this.ac = ac; 
    }
    public int getAx() { 
        return ax; 
    }
    public void setAx(int ax) { 
        this.ax = ax; 
    }
    public int getBx() { 
        return bx; 
    }
    public void setBx(int bx) { 
        this.bx = bx; 
    }
    public int getCx() { 
        return cx; 
    }
    public void setCx(int cx) { 
        this.cx = cx; 
    }
    public int getDx() { 
        return dx; 
    }
    public void setDx(int dx) { 
        this.dx = dx; 
    }
    public int getCpuAsignado() { 
        return cpuAsignado; 
    }
    public void setCpuAsignado(int cpuAsignado) { 
        this.cpuAsignado = cpuAsignado; 
    }
    public int getTiempoInicio() { 
        return tiempoInicio; 
    }
    public void setTiempoInicio(int tiempoInicio) { 
        this.tiempoInicio = tiempoInicio; 
    }
    public int getTiempoEmpleado() { 
        return tiempoEmpleado; 
    }
    public void setTiempoEmpleado(int tiempoEmpleado) { 
        this.tiempoEmpleado = tiempoEmpleado; 
    }
    public BCP getSiguienteBCP() { 
        return siguienteBCP; 
    }
    public void setSiguienteBCP(BCP siguienteBCP) { 
        this.siguienteBCP = siguienteBCP; 
    }

    public int getPrioridad() { 
        return prioridad; 
    }
    public void setPrioridad(int prioridad) { 
        this.prioridad = prioridad; 
    }
    public int[] getPila() { 
        return pila; 
    }
    public int getPuntero_pila() { 
        return puntero_pila; 
    }
    public void setPilaDirecta(int[] pila, int puntero) {
        this.pila = pila;
        this.puntero_pila = puntero;
    }
    public int getAl() { 
    return al; 
    }
    public void setAl(int al) { 
        this.al = al & 0xFF;
    }
    public int getTiempoLlegada() {
    return tiempoLlegada;
    }

    public void setTiempoLlegada(int tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }

    public int getTiempoFinal() {
        return tiempoFinal;
    }

    public void setTiempoFinal(int tiempoFinal) {
        this.tiempoFinal = tiempoFinal;
    }

    public int getRafagaTotal() {
        return rafagaTotal;
    }

    public void setRafagaTotal(int rafagaTotal) {
        this.rafagaTotal = rafagaTotal;
        this.rafagaRestante = rafagaTotal;
    }

    public int getRafagaRestante() {
        return rafagaRestante;
    }

    public void setRafagaRestante(int rafagaRestante) {
        this.rafagaRestante = rafagaRestante;
    }

    public int getTiempoEspera() {
        return tiempoEspera;
    }

    public void setTiempoEspera(int tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public int getTurnaround() {
        return turnaround;
    }

    public void setTurnaround(int turnaround) {
        this.turnaround = turnaround;
    }

    public double getTrTs() {
        return trTs;
    }

    public void setTrTs(double trTs) {
        this.trTs = trTs;
    }

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }

    public int getQuantumRestante() {
        return quantumRestante;
    }

    public void setQuantumRestante(int quantumRestante) {
        this.quantumRestante = quantumRestante;
    }

    public boolean isIniciado() {
        return iniciado;
    }

    public void setIniciado(boolean iniciado) {
        this.iniciado = iniciado;
    }


    public String getIrTexto() { 
        return irTexto; }
    public void setIrTexto(String irTexto) { 
        this.irTexto = irTexto; }
    public int getAh() {
        return Ah;
    }

    public void setAh(int Ah) {
        this.Ah = Ah;
    }


    /**
    * devuele la lista de archivos que se guardaron en el bcp por la interrupcion de INt 20H
    * 
    */ 
    public java.util.List<String> getArchivosAbiertos() {
        return archivosAbiertos;
    }
     /**
    * Agrega un archivo a la lista por la interrupcion INt 20H
    * 
    */ 
    public void setArchivosAbiertos(java.util.List<String> archivos) {
        this.archivosAbiertos = archivos;

        this.contenidosArchivos = new java.util.ArrayList<>();

        for (int i = 0; i < archivos.size(); i++) {
            this.contenidosArchivos.add(0);
        }

        if (archivoAbierto == null) {
            archivoAbierto = "";
        }
    }

    /**
    * convierte el valor de dx a un formato de archivo
    * 
    */ 

    private String nombreArchivoDesdeDX() {
        return dx + ".asm";
    }

    /**
    * busca un archivo en la lista de archivos abiertos
    * 
    */ 
    private int buscarArchivo(String nombreArchivo) {
        return archivosAbiertos.indexOf(nombreArchivo);
    }

    /**
    * crea un archivo en la lista de archivos abiertos
    * 
    */ 
    public void crearArchivoDesdeDX() {
        String nombreArchivo = nombreArchivoDesdeDX();

        if (buscarArchivo(nombreArchivo) == -1) {
            archivosAbiertos.add(nombreArchivo);
            contenidosArchivos.add(0);
        }
    }
    
    /**
    * abre un archivo en la lista de archivos abiertos
    */  
    public void abrirArchivoDesdeDX() {
        String nombreArchivo = nombreArchivoDesdeDX();

        if (buscarArchivo(nombreArchivo) == -1) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "El archivo no existe: " + nombreArchivo,
                    "INT 21H",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            archivoAbierto = "";
            return;
        }

        archivoAbierto = nombreArchivo;
    }
    /**
    * lee un archivo en la lista de archivos abiertos    
    */ 
    public void leerArchivoDesdeDX() {
        String nombreArchivo = nombreArchivoDesdeDX();

        if (!nombreArchivo.equals(archivoAbierto)) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "No se puede leer. El archivo no está abierto: " + nombreArchivo,
                    "INT 21H",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int index = buscarArchivo(nombreArchivo);

        if (index == -1) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "No se puede leer. El archivo no existe: " + nombreArchivo,
                    "INT 21H",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        al = contenidosArchivos.get(index);
    }
    /**
    * escribe un archivo en la lista de archivos abiertos
    */
    public void escribirArchivoDesdeDX() {
        String nombreArchivo = nombreArchivoDesdeDX();

        if (!nombreArchivo.equals(archivoAbierto)) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "No se puede escribir. El archivo no está abierto: " + nombreArchivo,
                    "INT 21H",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int index = buscarArchivo(nombreArchivo);

        if (index == -1) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "No se puede escribir. El archivo no existe: " + nombreArchivo,
                    "INT 21H",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        contenidosArchivos.set(index, al);
    }
    /**
    * elimina un archivo en la lista de archivos abiertos
    */

    public void eliminarArchivoDesdeDX() {
        String nombreArchivo = nombreArchivoDesdeDX();
        int index = buscarArchivo(nombreArchivo);

        if (index != -1) {
            archivosAbiertos.remove(index);
            contenidosArchivos.remove(index);
        }

        if (nombreArchivo.equals(archivoAbierto)) {
            archivoAbierto = "";
        }
    }
    public boolean haLlegado(int tiempoActual) {
        return tiempoActual >= tiempoLlegada;
    }

    public boolean estaFinalizado() {
        return rafagaRestante <= 0 || "finalizado".equalsIgnoreCase(estado);
    }

    public void ejecutarUnSegundo() {
        if (rafagaRestante > 0) {
            tiempoEmpleado++;
            rafagaRestante--;
        }
    }

    public void marcarInicio(int tiempoActual) {
        if (!iniciado) {
            tiempoInicio = tiempoActual;
            iniciado = true;
        }
    }

    public void marcarFinalizado(int tiempoActual) {
        estado = "finalizado";
        tiempoFinal = tiempoActual;
        turnaround = tiempoFinal - tiempoLlegada;

        if (rafagaTotal > 0) {
            trTs = (double) turnaround / rafagaTotal;
        } else {
            trTs = 0.0;
        }

        cpuAsignado = -1;
    }

    public double calcularHRRN(int tiempoActual) {
        int espera = tiempoActual - tiempoLlegada - tiempoEmpleado;

        if (espera < 0) {
            espera = 0;
        }

        if (rafagaRestante <= 0) {
            return 0;
        }

        return (double) (espera + rafagaRestante) / rafagaRestante;
    }

}
