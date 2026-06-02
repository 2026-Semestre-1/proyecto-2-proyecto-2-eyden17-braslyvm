/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Algorithms;

import CPU.BCP;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author eyden
 */
public class RR {

    private final int quantum;

    public RR(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than zero");
        }
        this.quantum = quantum;
    }

    public List<BCP> schedule(List<BCP> processes) {
        List<BCP> pendingProcesses = new ArrayList<>();
        Queue<BCP> readyQueue = new ArrayDeque<>();
        int currentTime = 0;

        if (processes == null) {
            return pendingProcesses;
        }

        pendingProcesses.addAll(processes);
        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        while (!pendingProcesses.isEmpty() || !readyQueue.isEmpty()) {
            addArrivedProcesses(pendingProcesses, readyQueue, currentTime);

            if (readyQueue.isEmpty()) {
                currentTime = pendingProcesses.get(0).getTiempoLlegada();
                continue;
            }

            BCP currentProcess = readyQueue.poll();

            if (!currentProcess.isIniciado()) {
                currentProcess.setTiempoInicio(currentTime);
                currentProcess.setIniciado(true);
            }

            int executionTime = Math.min(quantum, currentProcess.getRafagaRestante());

            while (executionTime > 0) {
                currentProcess.setRafagaRestante(currentProcess.getRafagaRestante() - 1);
                currentTime++;
                executionTime--;

                addArrivedProcesses(pendingProcesses, readyQueue, currentTime);
            }

            if (currentProcess.getRafagaRestante() == 0) {
                currentProcess.setTiempoFinal(currentTime);
                currentProcess.setTurnaround(currentProcess.getTiempoFinal() - currentProcess.getTiempoLlegada());
                currentProcess.setTiempoEspera(currentProcess.getTurnaround() - currentProcess.getRafagaTotal());
                currentProcess.setTrTs(currentProcess.getRafagaTotal() > 0
                        ? (double) currentProcess.getTurnaround() / currentProcess.getRafagaTotal()
                        : 0.0);
                currentProcess.setEstado("finalizado");
            } else {
                currentProcess.setEstado("listo");
                readyQueue.offer(currentProcess);
            }
        }

        return processes;
    }

    private void addArrivedProcesses(List<BCP> pendingProcesses, Queue<BCP> readyQueue, int currentTime) {
        for (int i = 0; i < pendingProcesses.size(); ) {
            if (pendingProcesses.get(i).getTiempoLlegada() <= currentTime) {
                readyQueue.offer(pendingProcesses.remove(i));
            } else {
                i++;
            }
        }

    }
}
