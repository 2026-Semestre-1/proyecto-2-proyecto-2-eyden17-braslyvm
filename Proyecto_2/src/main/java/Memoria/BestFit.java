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
public class BestFit {
    private int[] holeStart;
    private int[] holeEnd;
    private int holeCount;
    

    private int[] occupiedStart;
    private int[] occupiedEnd;
    private String[] occupiedProcess;
    private BCP[] occupiedBCP;
    private int occupiedCount;
    
    public BestFit(int userStart, int memorySize) {
        holeStart = new int[memorySize];
        holeEnd = new int[memorySize];

        occupiedStart = new int[memorySize];
        occupiedEnd = new int[memorySize];
        occupiedProcess = new String[memorySize];
        occupiedBCP = new BCP[memorySize];

        // Al inicio toda la memoria de usuario es un solo hueco libre.
        holeStart[0] = userStart;
        holeEnd[0] = memorySize;
        holeCount = 1;

        occupiedCount = 0;
    }

    /**
     * Asigna un proceso en memoria usando Best Fit.
     *
     * @param bcp BCP del proceso.
     * @param instructions instrucciones del proceso.
     * @param memory arreglo de memoria principal.
     * @return int[]{base, limit} si se pudo asignar, null si no cabe.
     */
    public int[] assign(BCP bcp, String[] instructions, String[] memory) {
        if (bcp == null || instructions == null || memory == null) {
            return null;
        }

        int processSize = instructions.length;
        int bestIndex = findBestHole(processSize);

        if (bestIndex == -1) {
            return null;
        }

        int base = holeStart[bestIndex];
        int processEnd = base + processSize;
        int limit = processEnd - 1;

        // Guardar instrucciones en memoria.
        for (int i = 0; i < instructions.length; i++) {
            memory[base + i] = instructions[i];
        }

        // Actualizar BCP.
        bcp.setBase(base);
        bcp.setLimite(limit);
        bcp.setPc(base);
        bcp.setEstado("preparado");

        // Registrar bloque ocupado.
        addOccupiedBlock(bcp, base, processEnd);

        // Actualizar hueco libre.
        if (processEnd == holeEnd[bestIndex]) {
            removeHole(bestIndex);
        } else {
            holeStart[bestIndex] = processEnd;
        }

        return new int[]{base, limit};
    }

    /**
     * Libera la memoria ocupada por un proceso.
     *
     * @param bcp BCP del proceso.
     * @param memory arreglo de memoria principal.
     */
    public void release(BCP bcp, String[] memory) {
        if (bcp == null || memory == null) {
            return;
        }

        int index = findOccupiedBlock(bcp.getIdProceso());

        if (index == -1) {
            return;
        }

        int start = occupiedStart[index];
        int end = occupiedEnd[index];

        // Limpiar instrucciones en memoria.
        for (int i = start; i < end; i++) {
            memory[i] = "";
        }

        // Agregar el bloque liberado como hueco.
        addHole(start, end);

        // Eliminar de ocupados.
        removeOccupiedBlock(index);

        // Ordenar y unir huecos vecinos.
        sortHoles();
        mergeAdjacentHoles();

        // Actualizar BCP.
        bcp.setBase(-1);
        bcp.setLimite(-1);
        bcp.setPc(-1);
    }

    /**
     * Verifica si existe algún hueco donde quepa el proceso.
     */
    public boolean hasSpace(String[] instructions) {
        if (instructions == null) {
            return false;
        }

        return findBestHole(instructions.length) != -1;
    }

    /**
     * Busca el hueco más pequeño donde quepa el proceso.
     */
    private int findBestHole(int processSize) {
        int bestIndex = -1;

        for (int i = 0; i < holeCount; i++) {
            int holeSize = holeEnd[i] - holeStart[i];

            if (processSize <= holeSize) {
                if (bestIndex == -1) {
                    bestIndex = i;
                } else {
                    int bestSize = holeEnd[bestIndex] - holeStart[bestIndex];

                    if (holeSize < bestSize) {
                        bestIndex = i;
                    }
                }
            }
        }

        return bestIndex;
    }

    /**
     * Agrega un hueco libre.
     */
    private void addHole(int start, int end) {
        holeStart[holeCount] = start;
        holeEnd[holeCount] = end;
        holeCount++;
    }

    /**
     * Elimina un hueco libre.
     */
    private void removeHole(int index) {
        for (int i = index; i < holeCount - 1; i++) {
            holeStart[i] = holeStart[i + 1];
            holeEnd[i] = holeEnd[i + 1];
        }

        holeStart[holeCount - 1] = 0;
        holeEnd[holeCount - 1] = 0;
        holeCount--;
    }

    /**
     * Registra un bloque ocupado.
     */
    private void addOccupiedBlock(BCP bcp, int start, int end) {
        occupiedStart[occupiedCount] = start;
        occupiedEnd[occupiedCount] = end;
        occupiedProcess[occupiedCount] = bcp.getIdProceso();
        occupiedBCP[occupiedCount] = bcp;
        occupiedCount++;
    }

    /**
     * Busca el bloque ocupado de un proceso.
     */
    private int findOccupiedBlock(String processId) {
        for (int i = 0; i < occupiedCount; i++) {
            if (processId.equals(occupiedProcess[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Elimina un bloque ocupado.
     */
    private void removeOccupiedBlock(int index) {
        for (int i = index; i < occupiedCount - 1; i++) {
            occupiedStart[i] = occupiedStart[i + 1];
            occupiedEnd[i] = occupiedEnd[i + 1];
            occupiedProcess[i] = occupiedProcess[i + 1];
            occupiedBCP[i] = occupiedBCP[i + 1];
        }

        int last = occupiedCount - 1;

        occupiedStart[last] = 0;
        occupiedEnd[last] = 0;
        occupiedProcess[last] = null;
        occupiedBCP[last] = null;

        occupiedCount--;
    }

    /**
     * Ordena los huecos por posición inicial.
     */
    private void sortHoles() {
        for (int i = 0; i < holeCount - 1; i++) {
            for (int j = 0; j < holeCount - i - 1; j++) {
                if (holeStart[j] > holeStart[j + 1]) {
                    int tempStart = holeStart[j];
                    holeStart[j] = holeStart[j + 1];
                    holeStart[j + 1] = tempStart;

                    int tempEnd = holeEnd[j];
                    holeEnd[j] = holeEnd[j + 1];
                    holeEnd[j + 1] = tempEnd;
                }
            }
        }
    }

    /**
     * Fusiona huecos vecinos.
     */
    private void mergeAdjacentHoles() {
        for (int i = 0; i < holeCount - 1; i++) {
            if (holeEnd[i] == holeStart[i + 1]) {
                holeEnd[i] = holeEnd[i + 1];
                removeHole(i + 1);
                i--;
            }
        }
    }
        public int[] getHoleStart() {
        return holeStart;
    }

    public int[] getHoleEnd() {
        return holeEnd;
    }

    public int getHoleCount() {
        return holeCount;
    }

    public int[] getOccupiedStart() {
        return occupiedStart;
    }

    public int[] getOccupiedEnd() {
        return occupiedEnd;
    }

    public String[] getOccupiedProcess() {
        return occupiedProcess;
    }

    public BCP[] getOccupiedBCP() {
        return occupiedBCP;
    }

    public int getOccupiedCount() {
        return occupiedCount;
    }
    
}
