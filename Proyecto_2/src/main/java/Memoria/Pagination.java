package Memoria;

import CPU.BCP;
import Settings.Constants;

/**
 *
 * @author braslyvm
 */
public class Pagination {

    private int userStart;
    private int memorySize;
    private int pageSize;

    private int physicalFrameCount;
    private int virtualFrameCount;

    // Marcos de memoria física
    private boolean[] physicalFrameFree;
    private String[] physicalFrameProcess;
    private int[] physicalFramePage;

    // Marcos de memoria virtual
    private boolean[] virtualFrameFree;
    private String[] virtualFrameProcess;
    private int[] virtualFramePage;

    // Tabla de páginas
    private String[] processes;
    private int[][] pagePhysicalFrame;
    private int[][] pageVirtualFrame;
    private boolean[][] pageInPhysical;
    private int[] processPageCount;
    private int[] processInstructionCount;

    private int maxProcesses;
    private int maxPagesPerProcess;

    private int fifoPointer;

    private Disco disco;

    /**
     * Constructor de paginación.
     *
     * @param userStart inicio de la memoria de usuario en memoria física.
     * @param memorySize tamaño total de memoria física.
     * @param pageSize tamaño de página / marco.
     * @param disco objeto Disco, usado para memoria virtual.
     * @param maxProcesses cantidad máxima de procesos.
     */
    public Pagination(int userStart, int memorySize, int pageSize, Disco disco, int maxProcesses) {
        this.userStart = userStart;
        this.memorySize = memorySize;
        this.pageSize = Math.max(1, pageSize);
        this.disco = disco;
        this.maxProcesses = maxProcesses;
        this.maxPagesPerProcess = Constants.MAX_PAGES_PER_PROCESS;

        int userMemorySize = memorySize - userStart;
        this.physicalFrameCount = userMemorySize / this.pageSize;

        String[] virtualMemory = disco.getMemoriaVirtual();
        this.virtualFrameCount = virtualMemory.length / this.pageSize;

        physicalFrameFree = new boolean[physicalFrameCount];
        physicalFrameProcess = new String[physicalFrameCount];
        physicalFramePage = new int[physicalFrameCount];

        for (int i = 0; i < physicalFrameCount; i++) {
            physicalFrameFree[i] = true;
            physicalFrameProcess[i] = "";
            physicalFramePage[i] = -1;
        }

        virtualFrameFree = new boolean[virtualFrameCount];
        virtualFrameProcess = new String[virtualFrameCount];
        virtualFramePage = new int[virtualFrameCount];

        for (int i = 0; i < virtualFrameCount; i++) {
            virtualFrameFree[i] = true;
            virtualFrameProcess[i] = "";
            virtualFramePage[i] = -1;
        }

        processes = new String[maxProcesses];
        pagePhysicalFrame = new int[maxProcesses][maxPagesPerProcess];
        pageVirtualFrame = new int[maxProcesses][maxPagesPerProcess];
        pageInPhysical = new boolean[maxProcesses][maxPagesPerProcess];
        processPageCount = new int[maxProcesses];
        processInstructionCount = new int[maxProcesses];

        for (int i = 0; i < maxProcesses; i++) {
            processes[i] = "";
            processPageCount[i] = 0;
            processInstructionCount[i] = 0;

            for (int j = 0; j < maxPagesPerProcess; j++) {
                pagePhysicalFrame[i][j] = -1;
                pageVirtualFrame[i][j] = -1;
                pageInPhysical[i][j] = false;
            }
        }

        fifoPointer = 0;
    }

    /**
     * Asigna un proceso usando paginación física / virtual.
     *
     * Primero intenta cargar páginas en memoria física.
     * Si no hay marcos físicos libres, carga las páginas en memoria virtual.
     *
     * @param bcp BCP del proceso.
     * @param instructions instrucciones del proceso.
     * @param memory memoria física.
     * @return {base, limite} lógico si se pudo asignar; null si no hay espacio.
     */
    public int[] assign(BCP bcp, String[] instructions, String[] memory) {
        if (bcp == null || instructions == null || memory == null || disco == null) {
            return null;
        }

        if (instructions.length == 0) {
            return null;
        }

        int pagesNeeded = (int) Math.ceil((double) instructions.length / pageSize);

        if (pagesNeeded > maxPagesPerProcess) {
            System.out.println("Error: el proceso tiene demasiadas páginas.");
            return null;
        }

        if (!hasSpace(instructions)) {
            System.out.println("Error: no hay espacio suficiente en memoria física/virtual.");
            return null;
        }

        int processIndex = registerProcess(bcp.getIdProceso());

        if (processIndex == -1) {
            System.out.println("Error: no se pudo registrar el proceso en la tabla de páginas.");
            return null;
        }

        processPageCount[processIndex] = pagesNeeded;
        processInstructionCount[processIndex] = instructions.length;

        for (int page = 0; page < pagesNeeded; page++) {
            String[] pageInstructions = getPageInstructions(instructions, page);

            int physicalFrame = findFreePhysicalFrame();

            if (physicalFrame != -1) {
                loadPageIntoPhysicalFrame(
                        bcp.getIdProceso(),
                        page,
                        pageInstructions,
                        physicalFrame,
                        memory
                );

                pageInPhysical[processIndex][page] = true;
                pagePhysicalFrame[processIndex][page] = physicalFrame;
                pageVirtualFrame[processIndex][page] = -1;

            } else {
                int virtualFrame = findFreeVirtualFrame();

                if (virtualFrame == -1) {
                    return null;
                }

                loadPageIntoVirtualFrame(
                        bcp.getIdProceso(),
                        page,
                        pageInstructions,
                        virtualFrame
                );

                pageInPhysical[processIndex][page] = false;
                pagePhysicalFrame[processIndex][page] = -1;
                pageVirtualFrame[processIndex][page] = virtualFrame;
            }
        }

        bcp.setBase(0);
        bcp.setLimite(instructions.length - 1);
        bcp.setPc(0);
        bcp.setEstado("preparado");

        return new int[]{bcp.getBase(), bcp.getLimite()};
    }

    /**
     * Libera todas las páginas del proceso, tanto de memoria física
     * como de memoria virtual.
     *
     * @param bcp proceso a liberar.
     * @param memory memoria física.
     */
    public void release(BCP bcp, String[] memory) {
        if (bcp == null || memory == null) {
            return;
        }

        int processIndex = findProcess(bcp.getIdProceso());

        if (processIndex == -1) {
            return;
        }

        for (int page = 0; page < processPageCount[processIndex]; page++) {
            if (pageInPhysical[processIndex][page]) {
                int physicalFrame = pagePhysicalFrame[processIndex][page];

                if (physicalFrame != -1) {
                    clearPhysicalFrame(physicalFrame, memory);
                }
            } else {
                int virtualFrame = pageVirtualFrame[processIndex][page];

                if (virtualFrame != -1) {
                    clearVirtualFrame(virtualFrame);
                }
            }

            pagePhysicalFrame[processIndex][page] = -1;
            pageVirtualFrame[processIndex][page] = -1;
            pageInPhysical[processIndex][page] = false;
        }

        processes[processIndex] = "";
        processPageCount[processIndex] = 0;
        processInstructionCount[processIndex] = 0;

        // No invalidar el BCP con -1.
        // El BCP es histórico/estado del proceso; la liberación solo limpia marcos.
        // Si el proceso ya terminó, su PC debe conservar un valor coherente para
        // estadísticas/interfaz, no una dirección inválida.
        if (bcp.getBase() < 0) {
            bcp.setBase(0);
        }
        if (bcp.getLimite() < 0) {
            bcp.setLimite(Math.max(0, processInstructionCount[processIndex] - 1));
        }
        if (bcp.getPc() < 0) {
            bcp.setPc(bcp.getBase());
        }
    }

    /**
     * Verifica si hay espacio entre marcos físicos y marcos virtuales.
     *
     * @param instructions instrucciones del proceso.
     * @return true si hay espacio, false si no.
     */
    public boolean hasSpace(String[] instructions) {
        if (instructions == null) {
            return false;
        }

        int pagesNeeded = (int) Math.ceil((double) instructions.length / pageSize);

        if (pagesNeeded > maxPagesPerProcess) {
            return false;
        }

        int totalFreeFrames = countFreePhysicalFrames() + countFreeVirtualFrames();

        return totalFreeFrames >= pagesNeeded;
    }

    /**
     * Lee la instrucción actual del proceso.
     *
     * Si la página está en memoria física, traduce PC lógico a dirección física.
     * Si la página está en memoria virtual, genera page fault y trae la página.
     *
     * @param bcp BCP del proceso.
     * @param memory memoria física.
     * @return instrucción actual.
     */
    public String readInstruction(BCP bcp, String[] memory) {
        if (bcp == null || memory == null) {
            return null;
        }

        int processIndex = findProcess(bcp.getIdProceso());

        if (processIndex == -1) {
            return null;
        }

        int logicalPc = bcp.getPc();

        if (logicalPc < 0 || logicalPc > bcp.getLimite()) {
            return null;
        }

        int page = logicalPc / pageSize;
        int offset = logicalPc % pageSize;

        if (!pageInPhysical[processIndex][page]) {
            boolean loaded = pageFault(processIndex, page, memory);

            if (!loaded) {
                return null;
            }
        }

        int physicalFrame = pagePhysicalFrame[processIndex][page];

        if (physicalFrame == -1) {
            return null;
        }

        int physicalAddress = userStart + (physicalFrame * pageSize) + offset;

        return memory[physicalAddress];
    }


    /**
     * Traduce una dirección lógica del proceso a dirección física de memoria.
     *
     * Este método es para visualización/interfaz: no modifica el PC y no provoca
     * page fault. Si la página no está actualmente en memoria física, retorna -1.
     *
     * @param bcp proceso dueño de la dirección lógica.
     * @param logicalAddress dirección lógica dentro del proceso.
     * @param memory memoria física.
     * @return dirección física real, o -1 si no se puede traducir.
     */
    public int getPhysicalAddress(BCP bcp, int logicalAddress, String[] memory) {
        if (bcp == null || memory == null) {
            return -1;
        }

        int processIndex = findProcess(bcp.getIdProceso());

        if (processIndex == -1) {
            return -1;
        }

        if (logicalAddress < 0 || logicalAddress > bcp.getLimite()) {
            return -1;
        }

        int page = logicalAddress / pageSize;
        int offset = logicalAddress % pageSize;

        if (page < 0 || page >= processPageCount[processIndex]) {
            return -1;
        }

        if (!pageInPhysical[processIndex][page]) {
            return -1;
        }

        int physicalFrame = pagePhysicalFrame[processIndex][page];

        if (physicalFrame == -1) {
            return -1;
        }

        int physicalAddress = userStart + (physicalFrame * pageSize) + offset;

        if (physicalAddress < 0 || physicalAddress >= memory.length) {
            return -1;
        }

        return physicalAddress;
    }

    /**
     * Atiende un fallo de página.
     *
     * Si la página está en virtual, se trae a memoria física.
     * Si no hay marco físico libre, se reemplaza una página usando FIFO.
     *
     * @param processIndex índice del proceso.
     * @param page página requerida.
     * @param memory memoria física.
     * @return true si logró traer la página.
     */
    private boolean pageFault(int processIndex, int page, String[] memory) {
        int virtualFrame = pageVirtualFrame[processIndex][page];

        if (virtualFrame == -1) {
            return false;
        }

        String[] requiredPage = readVirtualFrame(virtualFrame);

        if (requiredPage == null) {
            return false;
        }

        int physicalFrame = findFreePhysicalFrame();

        if (physicalFrame == -1) {
            physicalFrame = selectVictimPhysicalFrame();
            swapOutVictimFrame(physicalFrame, memory);
        }

        String processId = processes[processIndex];

        loadPageIntoPhysicalFrame(
                processId,
                page,
                requiredPage,
                physicalFrame,
                memory
        );

        clearVirtualFrame(virtualFrame);

        pageInPhysical[processIndex][page] = true;
        pagePhysicalFrame[processIndex][page] = physicalFrame;
        pageVirtualFrame[processIndex][page] = -1;

        return true;
    }

    /**
     * Saca una página de memoria física y la manda a un marco virtual.
     *
     * @param physicalFrame marco físico víctima.
     * @param memory memoria física.
     */
    private void swapOutVictimFrame(int physicalFrame, String[] memory) {
        String victimProcess = physicalFrameProcess[physicalFrame];
        int victimPage = physicalFramePage[physicalFrame];

        int victimProcessIndex = findProcess(victimProcess);

        if (victimProcessIndex == -1) {
            return;
        }

        String[] victimInstructions = readPhysicalFrame(physicalFrame, memory);

        int virtualFrame = findFreeVirtualFrame();

        if (virtualFrame == -1) {
            return;
        }

        loadPageIntoVirtualFrame(
                victimProcess,
                victimPage,
                victimInstructions,
                virtualFrame
        );

        pageInPhysical[victimProcessIndex][victimPage] = false;
        pagePhysicalFrame[victimProcessIndex][victimPage] = -1;
        pageVirtualFrame[victimProcessIndex][victimPage] = virtualFrame;

        clearPhysicalFrame(physicalFrame, memory);
    }

    /**
     * Carga una página dentro de un marco físico.
     */
    private void loadPageIntoPhysicalFrame(
            String processId,
            int page,
            String[] pageInstructions,
            int physicalFrame,
            String[] memory
    ) {
        int physicalBase = userStart + (physicalFrame * pageSize);

        for (int i = 0; i < pageSize; i++) {
            if (i < pageInstructions.length) {
                memory[physicalBase + i] = pageInstructions[i];
            } else {
                memory[physicalBase + i] = "";
            }
        }

        physicalFrameFree[physicalFrame] = false;
        physicalFrameProcess[physicalFrame] = processId;
        physicalFramePage[physicalFrame] = page;
    }

    /**
     * Carga una página dentro de un marco virtual.
     */
    private void loadPageIntoVirtualFrame(
            String processId,
            int page,
            String[] pageInstructions,
            int virtualFrame
    ) {
        String[] virtualMemory = disco.getMemoriaVirtual();
        int virtualBase = virtualFrame * pageSize;

        for (int i = 0; i < pageSize; i++) {
            if (i < pageInstructions.length) {
                virtualMemory[virtualBase + i] = pageInstructions[i];
            } else {
                virtualMemory[virtualBase + i] = "";
            }
        }

        virtualFrameFree[virtualFrame] = false;
        virtualFrameProcess[virtualFrame] = processId;
        virtualFramePage[virtualFrame] = page;
    }

    /**
     * Lee el contenido completo de un marco físico.
     */
    private String[] readPhysicalFrame(int physicalFrame, String[] memory) {
        String[] content = new String[pageSize];
        int physicalBase = userStart + (physicalFrame * pageSize);

        for (int i = 0; i < pageSize; i++) {
            content[i] = memory[physicalBase + i];
        }

        return content;
    }

    /**
     * Lee el contenido completo de un marco virtual.
     */
    private String[] readVirtualFrame(int virtualFrame) {
        if (virtualFrame < 0 || virtualFrame >= virtualFrameCount) {
            return null;
        }

        String[] virtualMemory = disco.getMemoriaVirtual();
        String[] content = new String[pageSize];
        int virtualBase = virtualFrame * pageSize;

        for (int i = 0; i < pageSize; i++) {
            content[i] = virtualMemory[virtualBase + i];
        }

        return content;
    }

    /**
     * Limpia un marco físico.
     */
    private void clearPhysicalFrame(int physicalFrame, String[] memory) {
        if (physicalFrame < 0 || physicalFrame >= physicalFrameCount) {
            return;
        }

        int physicalBase = userStart + (physicalFrame * pageSize);

        for (int i = 0; i < pageSize; i++) {
            memory[physicalBase + i] = "";
        }

        physicalFrameFree[physicalFrame] = true;
        physicalFrameProcess[physicalFrame] = "";
        physicalFramePage[physicalFrame] = -1;
    }

    /**
     * Limpia un marco virtual.
     */
    private void clearVirtualFrame(int virtualFrame) {
        if (virtualFrame < 0 || virtualFrame >= virtualFrameCount) {
            return;
        }

        String[] virtualMemory = disco.getMemoriaVirtual();
        int virtualBase = virtualFrame * pageSize;

        for (int i = 0; i < pageSize; i++) {
            virtualMemory[virtualBase + i] = "";
        }

        virtualFrameFree[virtualFrame] = true;
        virtualFrameProcess[virtualFrame] = "";
        virtualFramePage[virtualFrame] = -1;
    }

    /**
     * Busca el primer marco físico libre.
     */
    private int findFreePhysicalFrame() {
        for (int i = 0; i < physicalFrameCount; i++) {
            if (physicalFrameFree[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Busca el primer marco virtual libre.
     */
    private int findFreeVirtualFrame() {
        for (int i = 0; i < virtualFrameCount; i++) {
            if (virtualFrameFree[i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Cuenta marcos físicos libres.
     */
    private int countFreePhysicalFrames() {
        int count = 0;

        for (int i = 0; i < physicalFrameCount; i++) {
            if (physicalFrameFree[i]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Cuenta marcos virtuales libres.
     */
    private int countFreeVirtualFrames() {
        int count = 0;

        for (int i = 0; i < virtualFrameCount; i++) {
            if (virtualFrameFree[i]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Selecciona un marco físico víctima usando FIFO.
     */
    private int selectVictimPhysicalFrame() {
        int victim = fifoPointer;
        fifoPointer = (fifoPointer + 1) % physicalFrameCount;
        return victim;
    }

    /**
     * Extrae las instrucciones de una página desde el arreglo completo.
     */
    private String[] getPageInstructions(String[] instructions, int page) {
        int start = page * pageSize;
        int end = Math.min(start + pageSize, instructions.length);
        int size = end - start;

        String[] pageInstructions = new String[size];

        for (int i = 0; i < size; i++) {
            pageInstructions[i] = instructions[start + i];
        }

        return pageInstructions;
    }

    /**
     * Registra un proceso en la tabla interna.
     */
    private int registerProcess(String processId) {
        int existing = findProcess(processId);

        if (existing != -1) {
            return existing;
        }

        for (int i = 0; i < maxProcesses; i++) {
            if (processes[i] == null || processes[i].isEmpty()) {
                processes[i] = processId;
                return i;
            }
        }

        return -1;
    }

    /**
     * Busca un proceso registrado.
     */
    private int findProcess(String processId) {
        if (processId == null) {
            return -1;
        }

        for (int i = 0; i < maxProcesses; i++) {
            if (processId.equals(processes[i])) {
                return i;
            }
        }

        return -1;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPhysicalFrameCount() {
        return physicalFrameCount;
    }

    public int getVirtualFrameCount() {
        return virtualFrameCount;
    }

    public boolean[] getPhysicalFrameFree() {
        return physicalFrameFree;
    }

    public String[] getPhysicalFrameProcess() {
        return physicalFrameProcess;
    }

    public int[] getPhysicalFramePage() {
        return physicalFramePage;
    }

    public boolean[] getVirtualFrameFree() {
        return virtualFrameFree;
    }

    public String[] getVirtualFrameProcess() {
        return virtualFrameProcess;
    }

    public int[] getVirtualFramePage() {
        return virtualFramePage;
    }

    public String[] getProcesses() {
        return processes;
    }

    public int[][] getPagePhysicalFrame() {
        return pagePhysicalFrame;
    }

    public int[][] getPageVirtualFrame() {
        return pageVirtualFrame;
    }

    public boolean[][] getPageInPhysical() {
        return pageInPhysical;
    }

    public int[] getProcessPageCount() {
        return processPageCount;
    }

    public int[] getProcessInstructionCount() {
        return processInstructionCount;
    }
}
