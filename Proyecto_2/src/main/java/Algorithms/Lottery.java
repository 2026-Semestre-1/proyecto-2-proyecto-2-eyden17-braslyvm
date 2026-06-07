/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Algorithms;

import CPU.BCP;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eyden
 */
public class Lottery {

    private final int quantum;
    private final Random random;

    public Lottery(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than zero");
        }

        this.quantum = quantum;
        this.random = new Random();
    }

    public Lottery(int quantum, long seed) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than zero");
        }

        this.quantum = quantum;
        this.random = new Random(seed);
    }

    public List<BCP> schedule(List<BCP> processes) {
        List<BCP> pendingProcesses = new ArrayList<>();
        List<BCP> finishedProcesses = new ArrayList<>();
        int currentTime = 0;

        if (processes == null) {
            return finishedProcesses;
        }

        pendingProcesses.addAll(processes);
        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        while (!pendingProcesses.isEmpty()) {
            List<BCP> availableProcesses = getAvailableProcesses(pendingProcesses, currentTime);

            if (availableProcesses.isEmpty()) {
                currentTime = findNextArrivalTime(pendingProcesses, currentTime);
                continue;
            }

            BCP winningProcess = selectWinningProcess(availableProcesses);

            if (!winningProcess.isIniciado()) {
                winningProcess.setTiempoInicio(currentTime);
                winningProcess.setIniciado(true);
            }

            int executionTime = Math.min(quantum, winningProcess.getRafagaRestante());

            winningProcess.setEstado("ejecucion");

            for (int i = 0; i < executionTime; i++) {
                winningProcess.setRafagaRestante(winningProcess.getRafagaRestante() - 1);
                winningProcess.setTiempoEmpleado(winningProcess.getTiempoEmpleado() + 1);
                currentTime++;
            }

            if (winningProcess.getRafagaRestante() == 0) {
                winningProcess.setTiempoFinal(currentTime);
                winningProcess.setTurnaround(winningProcess.getTiempoFinal() - winningProcess.getTiempoLlegada());
                winningProcess.setTiempoEspera(winningProcess.getTurnaround() - winningProcess.getRafagaTotal());
                winningProcess.setTrTs(winningProcess.getRafagaTotal() > 0
                        ? (double) winningProcess.getTurnaround() / winningProcess.getRafagaTotal()
                        : 0.0);
                winningProcess.setEstado("finalizado");

                pendingProcesses.remove(winningProcess);
                finishedProcesses.add(winningProcess);
            } else {
                winningProcess.setEstado("listo");
            }
        }

        return finishedProcesses;
    }

    private List<BCP> getAvailableProcesses(List<BCP> pendingProcesses, int currentTime) {
        List<BCP> availableProcesses = new ArrayList<>();

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime && process.getRafagaRestante() > 0) {
                availableProcesses.add(process);
            }
        }

        return availableProcesses;
    }

    private BCP selectWinningProcess(List<BCP> availableProcesses) {
        int totalTickets = countTickets(availableProcesses);

        if (totalTickets <= 0) {
            return availableProcesses.get(0);
        }

        int winningTicket = random.nextInt(totalTickets) + 1;
        int accumulatedTickets = 0;

        for (BCP process : availableProcesses) {
            accumulatedTickets += getSafeTickets(process);

            if (accumulatedTickets >= winningTicket) {
                return process;
            }
        }

        return availableProcesses.get(0);
    }

    private int countTickets(List<BCP> processes) {
        int totalTickets = 0;

        for (BCP process : processes) {
            totalTickets += getSafeTickets(process);
        }

        return totalTickets;
    }

    private int getSafeTickets(BCP process) {
        return process.getTickets() > 0 ? process.getTickets() : 1;
    }

    private int findNextArrivalTime(List<BCP> pendingProcesses, int currentTime) {
        int nextArrivalTime = Integer.MAX_VALUE;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() > currentTime
                    && process.getTiempoLlegada() < nextArrivalTime) {
                nextArrivalTime = process.getTiempoLlegada();
            }
        }

        return nextArrivalTime;
    }
}
