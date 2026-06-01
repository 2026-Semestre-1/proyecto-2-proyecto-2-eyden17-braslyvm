/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Memoria;

import CPU.BCP;

/**
 *
 * @author braslyvm
 */
public class Partition {
    private int[] start;
    private int[] size;
    private boolean[] free_partition;
    private String[] procesoParticiones;
    /**
     * inicializa la particion fija del mismo size
     * @param start_user
     * @param size_memori
     * @param count_partitio 
     */
    public Partition(int start_user, int size_memori, int count_partitio) {
        start = new int[count_partitio];
        size = new int[count_partitio];
        free_partition = new boolean[count_partitio];
        procesoParticiones = new String[count_partitio];

        int size_user = size_memori - start_user;
        int size_partition = size_user / count_partitio;

        for (int i = 0; i < count_partitio; i++) {
            start[i] = start_user + (i * size_partition);
            size[i] = size_partition;
            free_partition[i] = true;
            procesoParticiones[i] = "";
        }
    }
    /**
     * Segundo cosntructor que crea la particion de diferentes size
     * @param start_user
     * @param size_memori
     * @param sizes_partition 
     */
    public Partition(int start_user, int size_memori, int[] sizes_partition) {
        start = new int[sizes_partition.length];
        size = new int[sizes_partition.length];
        free_partition = new boolean[sizes_partition.length];
        procesoParticiones = new String[sizes_partition.length];

        int current_start = start_user;

        for (int i = 0; i < sizes_partition.length; i++) {
            int partition_size = sizes_partition[i];

            if (partition_size <= 0) {
                throw new IllegalArgumentException("El tamaño de la partición debe ser mayor que 0.");
            }

            if (current_start + partition_size > size_memori) {
                throw new IllegalArgumentException("Las particiones superan el tamaño disponible de memoria.");
            }

            start[i] = current_start;
            size[i] = partition_size;
            free_partition[i] = true;
            procesoParticiones[i] = "";

            current_start += partition_size;
        }
    }
    /**
     * funcion que agrega las intrucciones en el espacio mas optimon, doden sobre lo menos posible de memoria
     * @param bcp
     * @param instrucciones
     * @param memoria
     * @return 
     */
    public int[] asignar (BCP bcp, String[] instrucciones , String[] memoria){
        int size_process = instrucciones.length;
        int best_index = -1;
        
        for(int i = 0 ; i < size.length; i++){
            if (free_partition[i]&&size_process<=size[i]){
                if (best_index == -1 || size[i] < size[best_index]){
                    best_index = i;
                }
            }
        } 
        if (best_index == -1) {
            return null;
        }
        int base = start[best_index];
        for (int i = 0; i < instrucciones.length; i++) {
            memoria[base + i] = instrucciones[i];
        }
        free_partition[best_index] = false;
        procesoParticiones[best_index] = bcp.getIdProceso();

        bcp.setBase(base);
        bcp.setLimite(base + instrucciones.length - 1);
        bcp.setPc(base);
        bcp.setEstado("preparado");
        return new int[]{bcp.getBase(), bcp.getLimite()};
    }
    /**
     * Libera el esapacio de la particion de memoria segun el bcp 
     * @param bcp
     * @param memoria 
     */
    public void liberar(BCP bcp, String[] memoria) {
        String id = bcp.getIdProceso();

        for (int i = 0; i < procesoParticiones.length; i++) {
            if (id.equals(procesoParticiones[i])) {

                int inicio = start[i];
                int fin = inicio + size[i];

                for (int j = inicio; j < fin; j++) {
                    memoria[j] = "";
                }

                free_partition[i] = true;
                procesoParticiones[i] = "";
                return;
            }
        }
    }
    /**
     * valida si la lista de instruccione cabe en alguna particion
     * @param instrucciones
     * @return 
     */
    public boolean hayEspacio(String[] instrucciones) {
        int tamañoProceso = instrucciones.length;

        for (int i = 0; i < size.length; i++) {
            if (free_partition[i] && tamañoProceso <= size[i]) {
                return true;
            }
        }

        return false;
    }

    
    
    
}
