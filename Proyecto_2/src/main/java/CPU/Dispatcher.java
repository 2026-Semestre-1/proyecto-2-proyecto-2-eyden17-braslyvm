/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CPU;

import CPU.BCP;
import Memoria.Memoria;
import Memoria.Disco;

/**
 * Dispatcher FCFS.
 *
 */
public class Dispatcher {
    private java.util.List<BCP> colaProcesos;

    public Dispatcher() {
        this.colaProcesos = new java.util.ArrayList<>();
    }
    /**
     * Registra un proceso en la cola del dispatcher, actualizando su BCP si ya existe o agregándolo al final de la cola si es nuevo. Luego reconstruye los enlaces entre los BCPs para mantener el orden correcto.
     * @param bcp
     */
    public void registrarProceso(BCP bcp) {
        if (bcp == null) return;

        for (int i = 0; i < colaProcesos.size(); i++) {
            if (colaProcesos.get(i).getIdProceso().equals(bcp.getIdProceso())) {
                colaProcesos.set(i, bcp);
                reconstruirEnlacesCola();
                return;
            }
        }

        colaProcesos.add(bcp);
        reconstruirEnlacesCola();
    }
    /**
     * Actualiza el BCP de un proceso en la cola del dispatcher, buscando por su ID. Si el proceso no existe en la cola, lo registra como nuevo. Luego reconstruye los enlaces entre los BCPs para mantener el orden correcto.
     * @param bcpActualizado
     */

    public void actualizarBCP(BCP bcpActualizado) {
        if (bcpActualizado == null) return;

        for (int i = 0; i < colaProcesos.size(); i++) {
            if (colaProcesos.get(i).getIdProceso().equals(bcpActualizado.getIdProceso())) {
                colaProcesos.set(i, bcpActualizado);
                reconstruirEnlacesCola();
                return;
            }
        }

        registrarProceso(bcpActualizado);
    }

    public java.util.List<BCP> obtenerColaProcesos() {
        return new java.util.ArrayList<>(colaProcesos);
    }

    public boolean hayProcesos() {
        return !colaProcesos.isEmpty();
    }

    /**
     * Mueve un proceso de memoria principal a memoria virtual si ha finalizado.
     * @param memoria
     * @param disco
     */
    public void Mover(Memoria memoria, Disco disco) {
        BCP actual = memoria.obtenerPrimerBCP();
        if (actual == null) return;

        if (actual.getEstado().equalsIgnoreCase("finalizado")) {
            actualizarBCP(actual);
            memoria.liberarYCompactarProceso(actual);
            memoria.eliminarPrimerBCP();
            actualizarBCPsDeMemoriaEnDispatcher(memoria);
            cargarDesdeMemoriaVirtual(memoria, disco);
        }
    }

    /**
     * Carga procesos desde memoria virtual a memoria principal, actualizando los BCPs correspondientes y eliminando los procesos de memoria virtual una vez que han sido cargados. Se llama cada vez que se agrega un nuevo proceso a la cola del dispatcher para intentar subirlo a memoria principal lo antes posible.
     * @param disco
     * @param memoria
     */
    public void AgregarCola(Disco disco, Memoria memoria) {
        cargarDesdeMemoriaVirtual(memoria, disco);
    }
    /**
     * Carga procesos desde memoria virtual a memoria principal, actualizando los BCPs correspondientes y eliminando los procesos de memoria virtual una vez que han sido cargados. Se llama cada vez que se finaliza un proceso para intentar subir el siguiente proceso de memoria virtual a memoria principal lo antes posible.
     * @param memoria
     * @param disco
     */
    private void cargarDesdeMemoriaVirtual(Memoria memoria, Disco disco) {
        boolean cargoAlMenosUno = true;

        while (cargoAlMenosUno && !memoria.lleno()) {
            cargoAlMenosUno = false;

            int indice = disco.obtenerIndiceProcesoVirtualQueQuepa(memoria);
            if (indice == -1) {
                return;
            }

            String nombre = disco.getNombreVirtual(indice);
            String[] instrucciones = disco.getInstruccionesVirtuales(indice);

            if (nombre == null || instrucciones == null) {
                return;
            }

            int[] resultado = memoria.cargarInstruccionesSiCabe(instrucciones);
            if (resultado == null) {
                return;
            }

            int base = resultado[0];
            int limite = resultado[1];

            BCP bcp = buscarBCP(nombre);
            if (bcp == null) {
                bcp = new BCP(nombre, nombre, "preparado", base, limite, base, 0);
            } else {
                bcp.setEstado("preparado");
                bcp.setBase(base);
                bcp.setLimite(limite);
                bcp.setPc(base);
                bcp.setIr("");
            }

            memoria.agregarBCP(bcp);
            actualizarBCP(bcp);
            disco.eliminarProcesoVirtual(indice);
            cargoAlMenosUno = true;
        }
    }
    /**
     * Busca un BCP en la cola del dispatcher por su ID o nombre de proceso. Retorna el BCP si se encuentra, o null si no existe en la cola.
     * @param nombre
     * @return
     */

    private BCP buscarBCP(String nombre) {
        for (BCP bcp : colaProcesos) {
            if (bcp.getIdProceso().equals(nombre) || bcp.getNombreProceso().equals(nombre)) {
                return bcp;
            }
        }
        return null;
    }
    /**
     * Actualiza el BCP del proceso que se acaba de finalizar, marcándolo como finalizado y registrando su tiempo de finalización.
     * @param memoria
     */

    private void actualizarBCPsDeMemoriaEnDispatcher(Memoria memoria) {
        for (BCP bcp : memoria.obtenerTodosBCPsEnMemoria()) {
            actualizarBCP(bcp);
        }
    }
    /**
     * Reconstruye los enlaces entre los BCPs en la cola del dispatcher para mantener el orden correcto después de agregar o actualizar un proceso. Se llama cada vez que se registra o actualiza un proceso en la cola del dispatcher.
     */
    private void reconstruirEnlacesCola() {
        for (int i = 0; i < colaProcesos.size(); i++) {
            BCP actual = colaProcesos.get(i);
            BCP siguiente = (i < colaProcesos.size() - 1) ? colaProcesos.get(i + 1) : null;
            actual.setSiguienteBCP(siguiente);
        }
    }
}