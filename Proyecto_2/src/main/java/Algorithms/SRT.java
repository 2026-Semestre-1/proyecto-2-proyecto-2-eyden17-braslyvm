/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Algorithms;

import CPU.BCP;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author eyden
 */
public class SRT {

    public BCP selectNext(List<BCP> processes, int currentTime) {
        if (processes == null) {
            return null;
        }

        BCP shortestProcess = null;

        for (BCP process : processes) {
            if (!isReady(process, currentTime)) {
                continue;
            }

            if (shortestProcess == null
                    || getRemainingTime(process) < getRemainingTime(shortestProcess)) {
                shortestProcess = process;
            }
        }

        return shortestProcess;
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
            BCP currentProcess = findShortestRemainingProcess(pendingProcesses, currentTime);

            if (currentProcess == null) {
                currentTime = findNextArrivalTime(pendingProcesses, currentTime);
                continue;
            }
            
            if (!currentProcess.isIniciado()) {
                currentProcess.setTiempoInicio(currentTime);
                currentProcess.setIniciado(true);
            }

            currentProcess.setRafagaRestante(currentProcess.getRafagaRestante() - 1);
            currentProcess.setTiempoEmpleado(currentProcess.getTiempoEmpleado() + 1);
            currentTime++;


            if (currentProcess.getRafagaRestante() == 0) {
                currentProcess.setTiempoFinal(currentTime);
                currentProcess.setTurnaround(currentProcess.getTiempoFinal() - currentProcess.getTiempoLlegada());
                currentProcess.setTiempoEspera(currentProcess.getTurnaround() - currentProcess.getRafagaTotal());
                currentProcess.setTrTs(currentProcess.getRafagaTotal() > 0 
                            ? (double) currentProcess.getTurnaround() / currentProcess.getRafagaTotal() 
                            : 0.0
                );
                currentProcess.setEstado("finalizado");

                pendingProcesses.remove(currentProcess);
                finishedProcesses.add(currentProcess);
            }
        }

        return finishedProcesses;
    }

    private BCP findShortestRemainingProcess(List<BCP> pendingProcesses, int currentTime) {
        BCP shortestProcess = null;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime && process.getRafagaRestante() > 0) {
                if (shortestProcess == null || process.getRafagaRestante() < shortestProcess.getRafagaRestante()) {
                    shortestProcess = process;
                }
            }
        }
        return shortestProcess;
    }

    private int findNextArrivalTime(List<BCP> pendingProcesses, int currentTime) {
        int nextArrivalTime = Integer.MAX_VALUE;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() > currentTime && process.getTiempoLlegada() < nextArrivalTime) {
                nextArrivalTime = process.getTiempoLlegada();
            }
        }

        return nextArrivalTime;
    }

    private int getRemainingTime(BCP process) {
        if (process.getRafagaRestante() > 0) {
            return process.getRafagaRestante();
        }

        if (process.getRafagaTotal() > 0) {
            return process.getRafagaTotal();
        }

        return Integer.MAX_VALUE;
    }

    private boolean isReady(BCP process, int currentTime) {
        return process != null
                && "preparado".equalsIgnoreCase(process.getEstado())
                && process.getTiempoLlegada() <= currentTime
                && getRemainingTime(process) > 0;
    }
}
