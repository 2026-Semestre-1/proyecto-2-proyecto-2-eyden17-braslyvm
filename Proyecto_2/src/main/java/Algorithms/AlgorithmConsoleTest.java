package Algorithms;

import CPU.BCP;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class AlgorithmConsoleTest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            int option = readInt(scanner, "Option: ");

            if (option == 0) {
                System.out.println("Exiting test.");
                return;
            }

            List<BCP> processes = createSampleProcesses();
            printInitialProcesses(processes);

            switch (option) {
                case 1:
                    runFCFS(processes);
                    break;
                case 2:
                    runRoundRobin(processes, readInt(scanner, "Quantum: "));
                    break;
                case 3:
                    int quantum = readInt(scanner, "Quantum: ");
                    int newRate = readInt(scanner, "New queue priority rate: ");
                    int acceptedRate = readInt(scanner, "Accepted queue priority rate: ");
                    runSelfishRoundRobin(processes, quantum, newRate, acceptedRate);
                    break;
                case 4:
                    runHRRN(processes);
                    break;
                case 5:
                    runLottery(processes, readInt(scanner, "Quantum: "));
                    break;
                case 6:
                    runSJF(processes);
                    break;
                case 7:
                    runSRT(processes);
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== CPU Algorithm Console Test =====");
        System.out.println("1. FCFS");
        System.out.println("2. Round Robin");
        System.out.println("3. Selfish Round Robin");
        System.out.println("4. HRRN");
        System.out.println("5. Lottery");
        System.out.println("6. SJF");
        System.out.println("7. SRT");
        System.out.println("0. Exit");
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            }

            scanner.nextLine();
            System.out.println("Enter a valid integer.");
        }
    }

    private static List<BCP> createSampleProcesses() {
        List<BCP> processes = new ArrayList<>();
        processes.add(createProcess("P1", 0, 5, 0));
        processes.add(createProcess("P2", 1, 4, 0));
        processes.add(createProcess("P3", 2, 2, 0));
        processes.add(createProcess("P4", 4, 3, 0));
        return processes;
    }

    private static BCP createProcess(String id, int arrivalTime, int burstTime, int priority) {
        BCP process = new BCP(id, id, "nuevo", -1, -1, -1, priority);
        process.setTiempoLlegada(arrivalTime);
        process.setRafagaTotal(burstTime);
        process.setRafagaRestante(burstTime);
        process.setTickets(burstTime);
        return process;
    }

    private static void printInitialProcesses(List<BCP> processes) {
        System.out.println();
        System.out.println("Processes:");
        System.out.printf("%-8s %-8s %-8s %-8s%n", "ID", "Arrival", "Burst", "Priority");

        for (BCP process : processes) {
            System.out.printf(
                    "%-8s %-8d %-8d %-8d%n",
                    process.getIdProceso(),
                    process.getTiempoLlegada(),
                    process.getRafagaTotal(),
                    process.getPrioridad()
            );
        }
    }

    private static void runFCFS(List<BCP> processes) {
        processes.sort(Comparator.comparingInt(BCP::getTiempoLlegada));
        int currentTime = 0;

        System.out.println();
        System.out.println("===== FCFS Flow =====");

        for (BCP process : processes) {
            if (currentTime < process.getTiempoLlegada()) {
                printIdle(currentTime, process.getTiempoLlegada());
                currentTime = process.getTiempoLlegada();
            }

            startProcessIfNeeded(process, currentTime);

            while (process.getRafagaRestante() > 0) {
                runOneTimeUnit(process, currentTime);
                currentTime++;
            }

            finishProcess(process, currentTime);
            printFinish(process);
        }

        printSummary(processes);
    }

    private static void runSJF(List<BCP> processes) {
        List<BCP> pendingProcesses = new ArrayList<>(processes);
        int currentTime = 0;

        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        System.out.println();
        System.out.println("===== SJF Flow =====");

        while (!pendingProcesses.isEmpty()) {
            BCP shortestProcess = findShortestAvailableProcess(pendingProcesses, currentTime);

            if (shortestProcess == null) {
                int nextArrival = findNextArrivalTime(pendingProcesses, currentTime);
                printIdle(currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            System.out.printf("t=%d -> selected %s | burst=%d%n",
                    currentTime,
                    shortestProcess.getIdProceso(),
                    shortestProcess.getRafagaRestante());

            startProcessIfNeeded(shortestProcess, currentTime);

            while (shortestProcess.getRafagaRestante() > 0) {
                runOneTimeUnit(shortestProcess, currentTime);
                currentTime++;
            }

            finishProcess(shortestProcess, currentTime);
            printFinish(shortestProcess);
            pendingProcesses.remove(shortestProcess);
        }

        printSummary(processes);
    }

    private static void runSRT(List<BCP> processes) {
        List<BCP> pendingProcesses = new ArrayList<>(processes);
        int currentTime = 0;

        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        System.out.println();
        System.out.println("===== SRT Flow =====");

        while (!pendingProcesses.isEmpty()) {
            BCP currentProcess = findShortestRemainingProcess(pendingProcesses, currentTime);

            if (currentProcess == null) {
                int nextArrival = findNextArrivalTime(pendingProcesses, currentTime);
                printIdle(currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            System.out.printf("t=%d -> selected %s | remaining=%d%n",
                    currentTime,
                    currentProcess.getIdProceso(),
                    currentProcess.getRafagaRestante());

            startProcessIfNeeded(currentProcess, currentTime);
            runOneTimeUnit(currentProcess, currentTime);
            currentTime++;

            if (currentProcess.getRafagaRestante() == 0) {
                finishProcess(currentProcess, currentTime);
                printFinish(currentProcess);
                pendingProcesses.remove(currentProcess);
            }
        }

        printSummary(processes);
    }

    private static void runLottery(List<BCP> processes, int quantum) {
        if (quantum <= 0) {
            System.out.println("Quantum must be greater than zero.");
            return;
        }

        List<BCP> pendingProcesses = new ArrayList<>(processes);
        Random random = new Random(7L);
        int currentTime = 0;

        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        System.out.println();
        System.out.println("===== Lottery Flow =====");

        while (!pendingProcesses.isEmpty()) {
            List<BCP> availableProcesses = getLotteryAvailableProcesses(pendingProcesses, currentTime);

            if (availableProcesses.isEmpty()) {
                int nextArrival = findNextArrivalTime(pendingProcesses, currentTime);
                printIdle(currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            BCP winningProcess = selectLotteryWinner(availableProcesses, random);
            System.out.printf(
                    "t=%d -> selected %s | tickets=%d | available=%s%n",
                    currentTime,
                    winningProcess.getIdProceso(),
                    getSafeTickets(winningProcess),
                    processIds(availableProcesses)
            );

            startProcessIfNeeded(winningProcess, currentTime);

            int executionTime = Math.min(quantum, winningProcess.getRafagaRestante());

            for (int i = 0; i < executionTime; i++) {
                runOneTimeUnit(winningProcess, currentTime);
                currentTime++;
            }

            if (winningProcess.getRafagaRestante() == 0) {
                finishProcess(winningProcess, currentTime);
                printFinish(winningProcess);
                pendingProcesses.remove(winningProcess);
            } else {
                winningProcess.setEstado("listo");
            }
        }

        printSummary(processes);
    }

    private static void runHRRN(List<BCP> processes) {
        List<BCP> pendingProcesses = new ArrayList<>(processes);
        int currentTime = 0;

        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        System.out.println();
        System.out.println("===== HRRN Flow =====");

        while (!pendingProcesses.isEmpty()) {
            BCP selectedProcess = findHighestResponseRatioProcess(pendingProcesses, currentTime);

            if (selectedProcess == null) {
                int nextArrival = findNextArrivalTime(pendingProcesses, currentTime);
                printIdle(currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            System.out.printf(
                    "t=%d -> selected %s | responseRatio=%.2f%n",
                    currentTime,
                    selectedProcess.getIdProceso(),
                    calculateResponseRatio(selectedProcess, currentTime)
            );

            startProcessIfNeeded(selectedProcess, currentTime);

            while (selectedProcess.getRafagaRestante() > 0) {
                runOneTimeUnit(selectedProcess, currentTime);
                currentTime++;
            }

            finishProcess(selectedProcess, currentTime);
            printFinish(selectedProcess);
            pendingProcesses.remove(selectedProcess);
        }

        printSummary(processes);
    }

    private static void runRoundRobin(List<BCP> processes, int quantum) {
        if (quantum <= 0) {
            System.out.println("Quantum must be greater than zero.");
            return;
        }

        List<BCP> pendingProcesses = new ArrayList<>(processes);
        Queue<BCP> readyQueue = new ArrayDeque<>();
        int currentTime = 0;

        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        System.out.println();
        System.out.println("===== Round Robin Flow =====");

        while (!pendingProcesses.isEmpty() || !readyQueue.isEmpty()) {
            addArrivedProcesses(pendingProcesses, readyQueue, currentTime, "ready");

            if (readyQueue.isEmpty()) {
                int nextArrival = pendingProcesses.get(0).getTiempoLlegada();
                printIdle(currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            BCP currentProcess = readyQueue.poll();
            startProcessIfNeeded(currentProcess, currentTime);

            int executionTime = Math.min(quantum, currentProcess.getRafagaRestante());

            for (int i = 0; i < executionTime; i++) {
                runOneTimeUnit(currentProcess, currentTime);
                currentTime++;
                addArrivedProcesses(pendingProcesses, readyQueue, currentTime, "ready");
            }

            if (currentProcess.getRafagaRestante() == 0) {
                finishProcess(currentProcess, currentTime);
                printFinish(currentProcess);
            } else {
                currentProcess.setEstado("listo");
                readyQueue.offer(currentProcess);
                System.out.printf(
                        "t=%d -> %s returns to ready queue | ready=%s%n",
                        currentTime,
                        currentProcess.getIdProceso(),
                        queueIds(readyQueue)
                );
            }
        }

        printSummary(processes);
    }

    private static void runSelfishRoundRobin(
            List<BCP> processes,
            int quantum,
            int newPriorityRate,
            int acceptedPriorityRate
    ) {
        if (quantum <= 0) {
            System.out.println("Quantum must be greater than zero.");
            return;
        }

        List<BCP> pendingProcesses = new ArrayList<>(processes);
        Queue<BCP> newQueue = new ArrayDeque<>();
        Queue<BCP> acceptedQueue = new ArrayDeque<>();
        int currentTime = 0;

        pendingProcesses.sort(Comparator.comparingInt(BCP::getTiempoLlegada));

        System.out.println();
        System.out.println("===== Selfish Round Robin Flow =====");

        while (!pendingProcesses.isEmpty() || !newQueue.isEmpty() || !acceptedQueue.isEmpty()) {
            addArrivedProcesses(pendingProcesses, newQueue, currentTime, "new");

            if (acceptedQueue.isEmpty()) {
                acceptFirstNewProcess(newQueue, acceptedQueue, currentTime);
            }

            increasePriorities(newQueue, newPriorityRate);
            increasePriorities(acceptedQueue, acceptedPriorityRate);
            moveEligibleProcesses(newQueue, acceptedQueue, currentTime);

            if (acceptedQueue.isEmpty()) {
                currentTime++;
                continue;
            }

            BCP currentProcess = acceptedQueue.poll();
            startProcessIfNeeded(currentProcess, currentTime);

            int executionTime = Math.min(quantum, currentProcess.getRafagaRestante());

            for (int i = 0; i < executionTime; i++) {
                runOneTimeUnit(currentProcess, currentTime);
                currentTime++;

                addArrivedProcesses(pendingProcesses, newQueue, currentTime, "new");
                increasePriorities(newQueue, newPriorityRate);
                increasePriorities(acceptedQueue, acceptedPriorityRate);
                moveEligibleProcesses(newQueue, acceptedQueue, currentTime);
            }

            if (currentProcess.getRafagaRestante() == 0) {
                finishProcess(currentProcess, currentTime);
                printFinish(currentProcess);
            } else {
                currentProcess.setEstado("listo");
                acceptedQueue.offer(currentProcess);
                System.out.printf(
                        "t=%d -> %s returns to accepted queue | accepted=%s | new=%s%n",
                        currentTime,
                        currentProcess.getIdProceso(),
                        queueIds(acceptedQueue),
                        queueIds(newQueue)
                );
            }
        }

        printSummary(processes);
    }

    private static List<BCP> getLotteryAvailableProcesses(List<BCP> pendingProcesses, int currentTime) {
        List<BCP> availableProcesses = new ArrayList<>();

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime && process.getRafagaRestante() > 0) {
                availableProcesses.add(process);
            }
        }

        return availableProcesses;
    }

    private static BCP selectLotteryWinner(List<BCP> availableProcesses, Random random) {
        int totalTickets = countLotteryTickets(availableProcesses);

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

    private static int countLotteryTickets(List<BCP> processes) {
        int totalTickets = 0;

        for (BCP process : processes) {
            totalTickets += getSafeTickets(process);
        }

        return totalTickets;
    }

    private static int getSafeTickets(BCP process) {
        return process.getTickets() > 0 ? process.getTickets() : 1;
    }

    private static String processIds(List<BCP> processes) {
        if (processes.isEmpty()) {
            return "[]";
        }

        StringBuilder ids = new StringBuilder("[");

        for (int i = 0; i < processes.size(); i++) {
            if (i > 0) {
                ids.append(", ");
            }

            ids.append(processes.get(i).getIdProceso());
        }

        ids.append("]");
        return ids.toString();
    }

    private static void addArrivedProcesses(
            List<BCP> pendingProcesses,
            Queue<BCP> targetQueue,
            int currentTime,
            String queueName
    ) {
        for (int i = 0; i < pendingProcesses.size(); ) {
            BCP process = pendingProcesses.get(i);

            if (process.getTiempoLlegada() <= currentTime) {
                process.setEstado(queueName);
                targetQueue.offer(pendingProcesses.remove(i));
                System.out.printf(
                        "t=%d -> %s arrives and enters %s queue%n",
                        currentTime,
                        process.getIdProceso(),
                        queueName
                );
            } else {
                i++;
            }
        }
    }

    private static void acceptFirstNewProcess(Queue<BCP> newQueue, Queue<BCP> acceptedQueue, int currentTime) {
        if (!newQueue.isEmpty()) {
            BCP process = newQueue.poll();
            process.setEstado("accepted");
            acceptedQueue.offer(process);
            System.out.printf("t=%d -> %s enters accepted queue%n", currentTime, process.getIdProceso());
        }
    }

    private static void increasePriorities(Queue<BCP> queue, int rate) {
        for (BCP process : queue) {
            process.setPrioridad(process.getPrioridad() + rate);
        }
    }

    private static void moveEligibleProcesses(Queue<BCP> newQueue, Queue<BCP> acceptedQueue, int currentTime) {
        if (newQueue.isEmpty() || acceptedQueue.isEmpty()) {
            return;
        }

        int threshold = getLowestPriority(acceptedQueue);
        int size = newQueue.size();

        for (int i = 0; i < size; i++) {
            BCP process = newQueue.poll();

            if (process.getPrioridad() >= threshold) {
                process.setEstado("accepted");
                acceptedQueue.offer(process);
                System.out.printf(
                        "t=%d -> %s moves from new to accepted | priority=%d threshold=%d%n",
                        currentTime,
                        process.getIdProceso(),
                        process.getPrioridad(),
                        threshold
                );
            } else {
                newQueue.offer(process);
            }
        }
    }

    private static int getLowestPriority(Queue<BCP> queue) {
        int lowestPriority = Integer.MAX_VALUE;

        for (BCP process : queue) {
            if (process.getPrioridad() < lowestPriority) {
                lowestPriority = process.getPrioridad();
            }
        }

        return lowestPriority;
    }

    private static BCP findHighestResponseRatioProcess(List<BCP> pendingProcesses, int currentTime) {
        BCP selectedProcess = null;
        double highestRatio = -1.0;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime) {
                double ratio = calculateResponseRatio(process, currentTime);

                if (selectedProcess == null || ratio > highestRatio) {
                    selectedProcess = process;
                    highestRatio = ratio;
                }
            }
        }

        return selectedProcess;
    }

    private static BCP findShortestAvailableProcess(List<BCP> pendingProcesses, int currentTime) {
        BCP shortestProcess = null;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime) {
                if (shortestProcess == null
                        || process.getRafagaRestante() < shortestProcess.getRafagaRestante()) {
                    shortestProcess = process;
                }
            }
        }

        return shortestProcess;
    }

    private static BCP findShortestRemainingProcess(List<BCP> pendingProcesses, int currentTime) {
        BCP shortestProcess = null;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() <= currentTime && process.getRafagaRestante() > 0) {
                if (shortestProcess == null
                        || process.getRafagaRestante() < shortestProcess.getRafagaRestante()) {
                    shortestProcess = process;
                }
            }
        }

        return shortestProcess;
    }

    private static double calculateResponseRatio(BCP process, int currentTime) {
        int burstTime = process.getRafagaRestante() > 0
                ? process.getRafagaRestante()
                : process.getRafagaTotal();

        if (burstTime <= 0) {
            return 0.0;
        }

        int waitingTime = currentTime - process.getTiempoLlegada();
        if (waitingTime < 0) {
            waitingTime = 0;
        }

        return (double) (waitingTime + burstTime) / burstTime;
    }

    private static int findNextArrivalTime(List<BCP> pendingProcesses, int currentTime) {
        int nextArrivalTime = Integer.MAX_VALUE;

        for (BCP process : pendingProcesses) {
            if (process.getTiempoLlegada() > currentTime
                    && process.getTiempoLlegada() < nextArrivalTime) {
                nextArrivalTime = process.getTiempoLlegada();
            }
        }

        return nextArrivalTime;
    }

    private static void startProcessIfNeeded(BCP process, int currentTime) {
        if (!process.isIniciado()) {
            process.setTiempoInicio(currentTime);
            process.setIniciado(true);
            System.out.printf("t=%d -> %s starts%n", currentTime, process.getIdProceso());
        }
    }

    private static void runOneTimeUnit(BCP process, int currentTime) {
        process.setEstado("ejecucion");
        process.setRafagaRestante(process.getRafagaRestante() - 1);
        process.setTiempoEmpleado(process.getTiempoEmpleado() + 1);
        System.out.printf(
                "t=%d..%d -> running %s | remaining=%d%n",
                currentTime,
                currentTime + 1,
                process.getIdProceso(),
                process.getRafagaRestante()
        );
    }

    private static void finishProcess(BCP process, int currentTime) {
        int turnaroundTime = currentTime - process.getTiempoLlegada();
        int waitingTime = turnaroundTime - process.getRafagaTotal();

        process.setEstado("finalizado");
        process.setTiempoFinal(currentTime);
        process.setTurnaround(turnaroundTime);
        process.setTiempoEspera(waitingTime);
        process.setTrTs(process.getRafagaTotal() > 0
                ? (double) turnaroundTime / process.getRafagaTotal()
                : 0.0);
    }

    private static void printIdle(int from, int to) {
        if (from < to) {
            System.out.printf("t=%d..%d -> CPU idle%n", from, to);
        }
    }

    private static void printFinish(BCP process) {
        System.out.printf(
                "t=%d -> %s finished | wait=%d turnaround=%d tr/ts=%.2f%n",
                process.getTiempoFinal(),
                process.getIdProceso(),
                process.getTiempoEspera(),
                process.getTurnaround(),
                process.getTrTs()
        );
    }

    private static void printSummary(List<BCP> processes) {
        System.out.println();
        System.out.println("Summary:");
        System.out.printf(
                "%-8s %-8s %-8s %-8s %-8s %-12s %-8s%n",
                "ID",
                "Arrival",
                "Burst",
                "Start",
                "Finish",
                "Waiting",
                "TR/TS"
        );

        for (BCP process : processes) {
            System.out.printf(
                    "%-8s %-8d %-8d %-8d %-8d %-12d %-8.2f%n",
                    process.getIdProceso(),
                    process.getTiempoLlegada(),
                    process.getRafagaTotal(),
                    process.getTiempoInicio(),
                    process.getTiempoFinal(),
                    process.getTiempoEspera(),
                    process.getTrTs()
            );
        }
    }

    private static String queueIds(Queue<BCP> queue) {
        if (queue.isEmpty()) {
            return "[]";
        }

        StringBuilder ids = new StringBuilder("[");
        boolean first = true;

        for (BCP process : queue) {
            if (!first) {
                ids.append(", ");
            }

            ids.append(process.getIdProceso());
            first = false;
        }

        ids.append("]");
        return ids.toString();
    }
}
