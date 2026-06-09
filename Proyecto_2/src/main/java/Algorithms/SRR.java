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
public class SRR {

    private final int quantum;
    private final int newPriorityRate;
    private final int acceptedPriorityRate;
    private int lastSelectedIndex = -1;

    public SRR(int quantum, int newPriorityRate, int acceptedPriorityRate) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than zero");
        }

        this.quantum = quantum;
        this.newPriorityRate = newPriorityRate;
        this.acceptedPriorityRate = acceptedPriorityRate;
    }

    public BCP selectNext(List<BCP> processes, int currentTime) {
        if (processes == null || processes.isEmpty()) {
            return null;
        }

        increaseRuntimePriorities(processes, currentTime);

        int n = processes.size();
        int start = Math.floorMod(lastSelectedIndex + 1, n);
        BCP selected = null;
        int selectedIndex = -1;

        for (int i = 0; i < n; i++) {
            int index = (start + i) % n;
            BCP process = processes.get(index);

            if (!isReady(process, currentTime)) {
                continue;
            }

            if (selected == null || process.getPrioridad() > selected.getPrioridad()) {
                selected = process;
                selectedIndex = index;
            }
        }

        if (selectedIndex >= 0) {
            lastSelectedIndex = selectedIndex;
        }

        return selected;
    }

    public int getQuantum() {
        return quantum;
    }

    public List<BCP> schedule(List<BCP> processes) {
        List<BCP> pendingProcesses = new ArrayList<>();
        Queue<BCP> newQueue = new ArrayDeque<>();
        Queue<BCP> acceptedQueue = new ArrayDeque<>();
        int currentTime = 0;

        if (processes == null) {
            return pendingProcesses;
        }

        pendingProcesses.addAll(processes);
        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        while (!pendingProcesses.isEmpty() || !newQueue.isEmpty() || !acceptedQueue.isEmpty()) {
            addArrivedProcesses(pendingProcesses, newQueue, currentTime);

            if (acceptedQueue.isEmpty()) {
                acceptFirstNewProcess(newQueue, acceptedQueue);
            }

            increasePriorities(newQueue, acceptedQueue);
            moveEligibleProcesses(newQueue, acceptedQueue);

            if (acceptedQueue.isEmpty()) {
                currentTime++;
                continue;
            }

            BCP currentProcess = acceptedQueue.poll();

            if (!currentProcess.isIniciado()) {
                currentProcess.setTiempoInicio(currentTime);
                currentProcess.setIniciado(true);
            }

            int executionTime = Math.min(quantum, currentProcess.getRafagaRestante());

            for (int i = 0; i < executionTime; i++) {
                currentProcess.setRafagaRestante(currentProcess.getRafagaRestante() - 1);
                currentProcess.setTiempoEmpleado(currentProcess.getTiempoEmpleado() + 1);
                currentTime++;

                addArrivedProcesses(pendingProcesses, newQueue, currentTime);
                increasePriorities(newQueue, acceptedQueue);
                moveEligibleProcesses(newQueue, acceptedQueue);
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
                acceptedQueue.offer(currentProcess);
            }
        }

        return processes;
    }

    private void addArrivedProcesses(List<BCP> pendingProcesses, Queue<BCP> newQueue, int currentTime) {
        for (int i = 0; i < pendingProcesses.size(); ) {
            if (pendingProcesses.get(i).getTiempoLlegada() <= currentTime) {
                newQueue.offer(pendingProcesses.remove(i));
            } else {
                i++;
            }
        }
    }

    private void acceptFirstNewProcess(Queue<BCP> newQueue, Queue<BCP> acceptedQueue) {
        if (!newQueue.isEmpty()) {
            acceptedQueue.offer(newQueue.poll());
        }
    }

    private void increasePriorities(Queue<BCP> newQueue, Queue<BCP> acceptedQueue) {
        for (BCP process : newQueue) {
            process.setPrioridad(process.getPrioridad() + newPriorityRate);
        }
        for (BCP process : acceptedQueue) {
            process.setPrioridad(process.getPrioridad() + acceptedPriorityRate);
        }
    }

    private void moveEligibleProcesses(Queue<BCP> newQueue, Queue<BCP> acceptedQueue) {
        if (newQueue.isEmpty() || acceptedQueue.isEmpty()) {
            return;
        }

        int acceptedPriorityThreshold = getLowestAcceptedPriority(acceptedQueue);
        int size = newQueue.size();

        for (int i = 0; i < size; i++) {
            BCP process = newQueue.poll();

            if (process.getPrioridad() >= acceptedPriorityThreshold) {
                acceptedQueue.offer(process);
            } else {
                newQueue.offer(process);
            }
        }
    }

    private int getLowestAcceptedPriority(Queue<BCP> acceptedQueue) {
        int lowestPriority = Integer.MAX_VALUE;

        for (BCP process : acceptedQueue) {
            if (process.getPrioridad() < lowestPriority) {
                lowestPriority = process.getPrioridad();
            }
        }

        return lowestPriority;
    }

    private void increaseRuntimePriorities(List<BCP> processes, int currentTime) {
        for (BCP process : processes) {
            if (process == null || process.getTiempoLlegada() > currentTime) {
                continue;
            }

            if (!"preparado".equalsIgnoreCase(process.getEstado())) {
                continue;
            }

            if ("preparado".equalsIgnoreCase(process.getEstado())) {
                process.setPrioridad(process.getPrioridad() + newPriorityRate);
            } else if ("ejecución".equalsIgnoreCase(process.getEstado())
                    || "ejecucion".equalsIgnoreCase(process.getEstado())) {
                process.setPrioridad(process.getPrioridad() + acceptedPriorityRate);
            }
        }
    }

    private boolean isReady(BCP process, int currentTime) {
        return process != null
                && "preparado".equalsIgnoreCase(process.getEstado())
                && process.getTiempoLlegada() <= currentTime;
    }
}