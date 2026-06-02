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
public class SJF {

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
            BCP shortestProcess = findShortestAvailableProcess(pendingProcesses, currentTime);

            if (shortestProcess == null) {
                currentTime = pendingProcesses.get(0).getTiempoLlegada();
                continue;
            }

            // TODO: Mark start time if this is the first time the process runs.
            if (!shortestProcess.isIniciado()) {
                shortestProcess.setTiempoInicio(currentTime);
                shortestProcess.setIniciado(true);
            }

            int burstTime = getBurstTime(shortestProcess);
            
            currentTime += burstTime;

            shortestProcess.setRafagaRestante(0);
            shortestProcess.setTiempoEmpleado(shortestProcess.getTiempoEmpleado() + burstTime);
            shortestProcess.setTiempoFinal(currentTime);
            shortestProcess.setTurnaround(
            shortestProcess.getTiempoFinal() - shortestProcess.getTiempoLlegada());
            shortestProcess.setTiempoEspera( shortestProcess.getTurnaround() - shortestProcess.getRafagaTotal());
            shortestProcess.setTrTs(
                    shortestProcess.getRafagaTotal() > 0
                            ? (double) shortestProcess.getTurnaround() / shortestProcess.getRafagaTotal()
                            : 0.0
            );
            shortestProcess.setEstado("finalizado");

            pendingProcesses.remove(shortestProcess);
            finishedProcesses.add(shortestProcess);
        }

        return finishedProcesses;
    }

    private BCP findShortestAvailableProcess(List<BCP> pendingProcesses, int currentTime) {
        BCP shortestProcess = null;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime) {
                if (shortestProcess == null || getBurstTime(process) < getBurstTime(shortestProcess)) {
                    shortestProcess = process;
                }
            }
        }

        return shortestProcess;
    }

    private int getBurstTime(BCP process) {
        if (process.getRafagaRestante() > 0) {
            return process.getRafagaRestante();
        }

        if (process.getRafagaTotal() > 0) {
            return process.getRafagaTotal();
        }

        return 0;
    }
}
