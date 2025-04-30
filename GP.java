import java.util.LinkedList;
import java.util.Queue;

public class GP implements GP_Interface {
    GM gm;
    Sistema.SO so;
    boolean[] processID;
    Queue<PCB> rodando;
    Queue<PCB> prontos;

    public GP(GM gm, Sistema.SO so) {
        this.gm = gm;
        this.so = so;
        int maxProc = gm.getTamMem() / gm.getTamPg();
        processID = new boolean[maxProc];
        rodando = new LinkedList<>();
        prontos = new LinkedList<>();
    }

    @Override
    public int criaProcesso(String programName, Sistema.Word[] programa) {
        int tamanhoPrograma = programa.length;
        int[] paginasMemoria = gm.aloca(tamanhoPrograma);
        if (paginasMemoria == null) {
            throw new IllegalStateException("Insufficient memory to allocate process.");
        }

        int processID = getProcessID();

        gm.registraTabelaPaginas(processID, paginasMemoria);

        so.utils.loadProgram(programa, paginasMemoria, gm.getTamPg());
        int inicio = gm.traduzir(processID, 0);
        PCB processControlBlock = new PCB(programName, processID, inicio, new int[10], paginasMemoria);
        prontos.add(processControlBlock);

        return processControlBlock.processID;
    }


    @Override
    public void desalocaProcesso(int id) {
        if (id < 0 || id >= processID.length || !processID[id]) {
            throw new IllegalArgumentException("Invalid process ID.");
        }
        for (PCB process : rodando) {
            if (process.processID == id) {
                gm.desaloca(process.memPage);
                rodando.remove(process);
                processID[id] = false;
                return;
            }
        }
        for (PCB process : prontos) {
            if (process.processID == id) {
                gm.desaloca(process.memPage);
                prontos.remove(process);
                processID[id] = false;
                return;
            }
        }
        throw new IllegalStateException("Process not found.");
    }

    public PCB loadNext() {
        PCB process = prontos.poll();
        assert process != null;
        process.processState = PCB.ProcessState.RUNNING;
        rodando.add(process);
        return process;
    }

    public void load(int id) {
        for (PCB process : prontos) {
            if (process.processID == id) {
                prontos.remove(process);
                rodando.add(process);
                process.processState = PCB.ProcessState.RUNNING;
                return;
            }
        }
        throw new IllegalStateException("Process not found in ready queue.");
    }

    public void unload(int id) {
        for (PCB process : rodando) {
            if (process.processID == id) {
                rodando.remove(process);
                prontos.add(process);
                process.processState = PCB.ProcessState.READY;
                return;
            }
        }
        throw new IllegalStateException("Process not found in ready queue.");
    }

    public void kill(int id){
        for (PCB process : rodando) {
            if (process.processID == id) {
                rodando.remove(process);
                gm.desaloca(process.memPage);
                so.utils.clearMemory(process.memPage, gm.getTamPg());
                processID[id] = false;
                return;
            }
        }
        for (PCB process : prontos) {
            if (process.processID == id) {
                prontos.remove(process);
                gm.desaloca(process.memPage);
                so.utils.clearMemory(process.memPage, gm.getTamPg());
                processID[id] = false;
                return;
            }
        }
    }

    public String listProcess(){
        StringBuilder processes = new StringBuilder("ID" + "   " + "PID" + "    " + "NAME" + "     " + "STATE\n");
        for (PCB process : rodando) {
            processes.append(process).append("\n");
        }
        for (PCB process : prontos) {
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

        for (PCB process : rodando) {
            if (process.processID == id) {
                int inicio = gm.traduzir(id, 0);
                int fim = inicio + (process.memPage.length * gm.getTamPg()) - 1;
                System.out.println(process);
                System.out.println("Dumping memory from " + inicio + " to " + fim);
                so.utils.dump(inicio, fim);
                return;
            }
        }

        for (PCB process : prontos) {
            if (process.processID == id) {
                int inicio = gm.traduzir(id, 0);
                int fim = inicio + (process.memPage.length * gm.getTamPg()) - 1;
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