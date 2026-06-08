/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Memoria;

import CPU.BCP;

/**
 * Disco / almacenamiento secundario.
 *
 */
public class Disco {
    private String[] memoria;
    private String[] Texto_memoria;
    private int limiteIndice;
    private int punteroIndice;
    private int punteroProgramas;
    private int puntero_Ultimo;
    private String[] memoria_Virtual;
    private int tamaño_Virtual;
    private int puntero_Virtual;
    private java.util.List<String> nombresVirtuales;
    private java.util.List<String[]> instruccionesVirtuales;
    private java.util.Map<String, String[]> programas;
    private java.util.Map<String, Integer> basesProgramas;
    /**
     * Constructor del disco, inicializa las estructuras de datos y punteros.
     * @param size_memoria
     * @param size_disco
     */
    public Disco(int size_memoria, int size_disco) {
        this.memoria = new String[size_disco];
        this.Texto_memoria = new String[size_disco];
        this.limiteIndice = Math.min(20, Math.max(5, size_disco / 20));
        if (this.limiteIndice >= size_disco) {
            this.limiteIndice = Math.max(1, size_disco / 2);
        }

        this.punteroIndice = 0;
        this.punteroProgramas = this.limiteIndice;
        this.puntero_Ultimo = this.punteroProgramas;

        this.tamaño_Virtual = size_memoria;
        this.memoria_Virtual = new String[size_memoria];
        this.puntero_Virtual = 0;
        this.nombresVirtuales = new java.util.ArrayList<>();
        this.instruccionesVirtuales = new java.util.ArrayList<>();

        this.programas = new java.util.LinkedHashMap<>();
        this.basesProgramas = new java.util.LinkedHashMap<>();
    }

    /**
     * Guarda un programa en el disco, registrando su índice y asegurando que no se dupliquen programas con el mismo nombre.
     * @param nombre
     * @param instrucciones
     * @return
     */
    public boolean guardarPrograma(String nombre, String[] instrucciones) {
        if (nombre == null || instrucciones == null) {
            return false;
        }
        if (programas.containsKey(nombre)) {
            return true;
        }

        if (punteroIndice >= limiteIndice) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "No hay espacio en el índice del disco para: " + nombre,
                    "Disco lleno",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if ((punteroProgramas + instrucciones.length) > memoria.length) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "No hay espacio en disco para guardar el programa: " + nombre,
                    "Disco lleno",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        int base = punteroProgramas;
        Texto_memoria[punteroIndice] = "Índice: ";
        memoria[punteroIndice] = nombre + " -> " + base;
        punteroIndice++;
        for (int i = 0; i < instrucciones.length; i++) {
            Texto_memoria[punteroProgramas] = "Programa " + nombre + ": ";
            memoria[punteroProgramas] = instrucciones[i];
            punteroProgramas++;
        }

        programas.put(nombre, java.util.Arrays.copyOf(instrucciones, instrucciones.length));
        basesProgramas.put(nombre, base);
        puntero_Ultimo = Math.max(puntero_Ultimo, punteroProgramas);

        return true;
    }
    /**
     * Obtiene las instrucciones de un programa guardado en el disco por su nombre.
     * @param nombre
     * @return
     */
    public String[] obtenerPrograma(String nombre) {
        String[] instrucciones = programas.get(nombre);
        if (instrucciones == null) return null;
        return java.util.Arrays.copyOf(instrucciones, instrucciones.length);
    }
    /**
     * Carga programas desde memoria virtual a memoria principal, actualizando los BCPs correspondientes y eliminando los procesos de memoria virtual una vez que han sido cargados.
     * @param nombre
     * @return
     */

    public int obtenerBasePrograma(String nombre) {
        Integer base = basesProgramas.get(nombre);
        return base == null ? -1 : base;
    }

    /**
     * Carga un proceso desde memoria virtual a memoria principal, actualizando los BCPs correspondientes y eliminando el proceso de memoria virtual una vez que ha sido cargado.
     * @param nombre
     * @param instrucciones
     * @return
     */
    public int cargarProcesoEnVirtual(String nombre, String[] instrucciones) {
        if (!hayEspacioVirtual(instrucciones.length)) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "No hay espacio suficiente en memoria virtual para el proceso: " + nombre,
                    "Memoria virtual llena",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return -1;
        }

        int base = puntero_Virtual;
        nombresVirtuales.add(nombre);
        instruccionesVirtuales.add(java.util.Arrays.copyOf(instrucciones, instrucciones.length));
        for (String instruccion : instrucciones) {
            memoria_Virtual[puntero_Virtual++] = nombre + ": " + instruccion;
        }
        return base;
    }
    /**
     * Busca en memoria virtual el índice del primer proceso que pueda caber en memoria principal, o devuelve -1 si no hay ninguno que quepa.
     * @param memoriaPrincipal
     * @return
     */
    public int obtenerIndiceProcesoVirtualQueQuepa(Memoria memoriaPrincipal) {
        for (int i = 0; i < instruccionesVirtuales.size(); i++) {
            String[] instrucciones = instruccionesVirtuales.get(i);

            
            if (memoriaPrincipal.hayEspacioParaProceso(instrucciones)) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Obtiene el nombre del proceso virtual en el índice especificado, o null si el índice no es válido.
     * @param indice
     * @return
     */

    public String getNombreVirtual(int indice) {
        if (indice < 0 || indice >= nombresVirtuales.size()) return null;
        return nombresVirtuales.get(indice);
    }
    /**
     * Obtiene las instrucciones del proceso virtual en el índice especificado, o null si el índice no es válido.
     * @param indice
     * @return
     */

    public String[] getInstruccionesVirtuales(int indice) {
        if (indice < 0 || indice >= instruccionesVirtuales.size()) return null;
        String[] instrucciones = instruccionesVirtuales.get(indice);
        return java.util.Arrays.copyOf(instrucciones, instrucciones.length);
    }
    /**
     * Elimina el proceso virtual en el índice especificado, liberando su espacio en memoria virtual y eliminándolo de las listas de procesos virtuales. 
     */

    public void eliminarProcesoVirtual(int indice) {
        if (indice < 0 || indice >= nombresVirtuales.size()) return;
        nombresVirtuales.remove(indice);
        instruccionesVirtuales.remove(indice);
        reconstruirMemoriaVirtual();
    }
    /**
     * Actualiza el BCP del proceso que se acaba de finalizar, marcándolo como finalizado y registrando su tiempo de finalización.
     */
    private void reconstruirMemoriaVirtual() {
        for (int i = 0; i < memoria_Virtual.length; i++) {
            memoria_Virtual[i] = "";
        }

        puntero_Virtual = 0;

        for (int i = 0; i < nombresVirtuales.size(); i++) {
            String nombre = nombresVirtuales.get(i);
            String[] instrucciones = instruccionesVirtuales.get(i);

            for (String instruccion : instrucciones) {
                if (puntero_Virtual < memoria_Virtual.length) {
                    memoria_Virtual[puntero_Virtual++] = nombre + ": " + instruccion;
                }
            }
        }
    }

    public boolean hayEspacioVirtual(int cantidad) {
        return (puntero_Virtual + cantidad) <= tamaño_Virtual;
    }

    // Compatibilidad con versiones anteriores del código.
    public boolean hayEspacio(int cantidad) {
        return hayEspacioVirtual(cantidad);
    }


    public String leerMemoria(int posicion) {
        if (posicion < 0 || posicion >= tamaño_Virtual) return null;
        return memoria_Virtual[posicion];
    }

    public String[] getMemoriaVirtual() {
        return memoria_Virtual;
    }

    public int getTamanoVirtual() {
        return tamaño_Virtual;
    }

    public int getPunteroVirtual() {
        return puntero_Virtual;
    }

    public String[] getMemoria() {
        return memoria;
    }

    public String[] getTextoMemoria() {
        return Texto_memoria;
    }

    public int getPunteroUltimo() {
        return puntero_Ultimo;
    }

    public java.util.List<String> getNombresProgramas() {
        return new java.util.ArrayList<>(programas.keySet());
    }

    public java.util.List<BCP> obtenerTodosBCPs() {
        return new java.util.ArrayList<>();
    }
}