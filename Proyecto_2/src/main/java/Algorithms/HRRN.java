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
public class HRRN {

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
            BCP selectedProcess = findHighestResponseRatioProcess(pendingProcesses, currentTime);

            if (selectedProcess == null) {
                currentTime = findNextArrivalTime(pendingProcesses, currentTime);
                continue;
            }

            if (!selectedProcess.isIniciado()) {
                selectedProcess.setTiempoInicio(currentTime);
                selectedProcess.setIniciado(true);
            }

            int burstTime = getBurstTime(selectedProcess);

            selectedProcess.setEstado("ejecucion");
            currentTime += burstTime;

            selectedProcess.setRafagaRestante(0);
            selectedProcess.setTiempoEmpleado(selectedProcess.getTiempoEmpleado() + burstTime);
            selectedProcess.setTiempoFinal(currentTime);
            selectedProcess.setTurnaround(selectedProcess.getTiempoFinal() - selectedProcess.getTiempoLlegada());
            selectedProcess.setTiempoEspera(selectedProcess.getTurnaround() - selectedProcess.getRafagaTotal());
            selectedProcess.setTrTs(selectedProcess.getRafagaTotal() > 0
                    ? (double) selectedProcess.getTurnaround() / selectedProcess.getRafagaTotal()
                    : 0.0);
            selectedProcess.setEstado("finalizado");

            pendingProcesses.remove(selectedProcess);
            finishedProcesses.add(selectedProcess);
        }

        return finishedProcesses;
    }

    private BCP findHighestResponseRatioProcess(List<BCP> pendingProcesses, int currentTime) {
        BCP selectedProcess = null;
        double highestRatio = -1.0;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime) {
                double responseRatio = calculateResponseRatio(process, currentTime);

                if (selectedProcess == null || responseRatio > highestRatio) {
                    selectedProcess = process;
                    highestRatio = responseRatio;
                }
            }
        }

        return selectedProcess;
    }

    private double calculateResponseRatio(BCP process, int currentTime) {
        int burstTime = getBurstTime(process);

        if (burstTime <= 0) {
            return 0.0;
        }

        int waitingTime = currentTime - process.getTiempoLlegada();
        if (waitingTime < 0) {
            waitingTime = 0;
        }

        return (double) (waitingTime + burstTime) / burstTime;
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
