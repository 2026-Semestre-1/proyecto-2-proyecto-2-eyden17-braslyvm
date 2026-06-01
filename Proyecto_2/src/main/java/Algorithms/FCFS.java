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
 * Planificador First Come First Served.
 * Ejecuta los procesos completos en orden de llegada, sin expropiacion.
 *
 * @author eyden
 */
public class FCFS {

    public List<BCP> schedule(List<BCP> processes) {
        List<BCP> readyQueue = new ArrayList<>();

        if (processes == null) {
            return readyQueue;
        }

        for (BCP process : processes) {
            if (process != null) {
                readyQueue.add(process);
            }
        }

        readyQueue.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        int currentTime = 0;

        for (BCP process : readyQueue) {
            int burstTime = getBurstTime(process);

            if (process.getRafagaTotal() <= 0) {
                process.setRafagaTotal(burstTime);
            }

            if (currentTime < process.getTiempoLlegada()) {
                currentTime = process.getTiempoLlegada();
            }

            int startTime = currentTime;
            int finishTime = startTime + burstTime;
            int turnaroundTime = finishTime - process.getTiempoLlegada();
            int waitingTime = startTime - process.getTiempoLlegada();

            process.setEstado("finalizado");
            process.setTiempoInicio(startTime);
            process.setTiempoFinal(finishTime);
            process.setTiempoEmpleado(burstTime);
            process.setRafagaRestante(0);
            process.setTiempoEspera(waitingTime);
            process.setTurnaround(turnaroundTime);
            process.setTrTs(burstTime > 0 ? (double) turnaroundTime / burstTime : 0.0);
            process.setCpuAsignado(-1);
            process.setIniciado(true);

            currentTime = finishTime;
        }

        return readyQueue;
    }

    private int getBurstTime(BCP process) {
        if (process.getRafagaRestante() > 0) {
            return process.getRafagaRestante();
        }

        if (process.getRafagaTotal() > 0) {
            return process.getRafagaTotal();
        }

        if (process.getBase() >= 0 && process.getLimite() >= process.getBase()) {
            return process.getLimite() - process.getBase() + 1;
        }

        return 0;
    }
}
