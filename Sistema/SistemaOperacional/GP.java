package Sistema.SistemaOperacional;

import Sistema.Hardware.Word;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class GP {
    GM gm;
    SO so;
    public boolean[] processID;
    List<PCB> programs;
    Queue<PCB> running;
    public Queue<PCB> ready;

    public GP(GM gm, SO so) {
        this.gm = gm;
        this.so = so;
        int maxProc = gm.getMemSize() / gm.getPageSize();
        processID = new boolean[maxProc];
        running = new LinkedList<>();
        ready = new LinkedList<>();
        programs = new ArrayList<>();
    }

    public int createProcess(String programName, Word[] programa) {
        int tamanhoPrograma = programa.length;
        int[] paginasMemoria = gm.malloc(tamanhoPrograma);
        if (paginasMemoria == null) {
            throw new IllegalStateException("Insufficient memory to allocate process.");
        }

        int processID = getProcessID();

        gm.registerPageTable(processID, paginasMemoria);

        so.utils.loadProgram(programa, paginasMemoria, gm.getPageSize());
        int inicio = gm.translate(processID, 0);
        PCB processControlBlock = new PCB(programName, processID, inicio, new int[10], paginasMemoria);
        ready.add(processControlBlock);
        programs.add(processControlBlock);

        return processControlBlock.processID;
    }

    public void removeProcess(int id) {
        if (id < 0 || id >= processID.length || !processID[id]) {
            throw new IllegalArgumentException("Invalid process ID.");
        }
        for (PCB process : running) {
            if (process.processID == id) {
                gm.deallocate(process.memPage);
                running.remove(process);
                processID[id] = false;
                return;
            }
        }
        for (PCB process : ready) {
            if (process.processID == id) {
                gm.deallocate(process.memPage);
                ready.remove(process);
                processID[id] = false;
                return;
            }
        }
        throw new IllegalStateException("Process not found.");
    }

    public PCB load(int id) {
        for (PCB process : ready) {
            if (process.processID == id) {
                ready.remove(process);
                running.add(process);
                process.processState = PCB.ProcessState.RUNNING;
                return process;
            }
        }
        throw new IllegalStateException("Process not found in ready queue.");
    }

    public PCB loadNext() {
        PCB process = ready.poll();
        assert process != null;
        process.processState = PCB.ProcessState.RUNNING;
        running.add(process);
        return process;
    }

    public void unload(int id) {
        for (PCB process : running) {
            if (process.processID == id) {
                running.remove(process);
                ready.add(process);
                process.processState = PCB.ProcessState.READY;
                return;
            }
        }
        throw new IllegalStateException("Process not found in ready queue.");
    }

    public void kill(int id){
        for (PCB process : running) {
            if (process.processID == id) {
                running.remove(process);
                gm.deallocate(process.memPage);
                so.utils.clearMemory(process.memPage, gm.getPageSize());
                processID[id] = false;
                return;
            }
        }
        for (PCB process : ready) {
            if (process.processID == id) {
                ready.remove(process);
                gm.deallocate(process.memPage);
                so.utils.clearMemory(process.memPage, gm.getPageSize());
                processID[id] = false;
                return;
            }
        }
        for (PCB process : programs){
            if(process.processID == id){
                programs.remove(process);
                gm.deallocate(process.memPage);
                so.utils.clearMemory(process.memPage, gm.getPageSize());
                processID[id] = false;
                return;
            }
        }
    }

    public String listProcess(){
        StringBuilder processes = new StringBuilder("ID" + "   " + "PID" + "    " + "NAME" + "     " + "STATE\n");
        for (PCB process : running) {
            processes.append(process).append("\n");
        }
        for (PCB process : ready) {
            processes.append(process).append("\n");
        }

        return processes.toString();
    }

    public void dumpID(int id) {

        if (id < 0 || id >= processID.length || !processID[id]) {
            throw new IllegalArgumentException("Invalid process ID.");
        }

        System.out.println("Dumping process with ID: " + id);
        System.out.println("----------------------");
        System.out.println("ID   PID   NAME   STATE");

        for (PCB process : running) {
            if (process.processID == id) {
                int inicio = gm.translate(id, 0);
                int fim = gm.translate(id, inicio + (process.memPage.length * gm.getPageSize()) - 1);
                System.out.println(process);
                System.out.println("Dumping memory from " + inicio + " to " + fim);
                so.utils.dump(inicio, fim);
                return;
            }
        }

        for (PCB process : ready) {
            if (process.processID == id) {
                int inicio = gm.translate(id, 0);
                int fim = gm.translate(id, process.memPage.length * gm.getPageSize() - 1);
                System.out.println("Dumping memory from " + inicio + " to " + fim);
                System.out.println(process);
                so.utils.dump(inicio, fim);
                return;
            }
        }

        throw new IllegalStateException("Process not found.");
    }

    public int getProcessID() {
        for (int i = 0; i < processID.length; i++) {
            if (!processID[i]) {
                processID[i] = true;
                return i;
            }
        }
        throw new IllegalStateException("No available process IDs.");
    }
}