import java.util.ArrayList;
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
    public int criaProcesso(Sistema.Word[] programa) {
        int tamanhoPrograma = programa.length;
        int[] paginasMemoria = gm.aloca(tamanhoPrograma);
        if (paginasMemoria == null) {
            throw new IllegalStateException("Insufficient memory to allocate process.");
        }
        int programCounter = paginasMemoria[0];
        PCB processControlBlock = new PCB(getProcessID(), programCounter, new int[10], paginasMemoria);
        prontos.add(processControlBlock);
        so.utils.loadProgram(programa, paginasMemoria, gm.getTamPg());
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

    public void load(int id) {
        for (PCB process : prontos) {
            if (process.processID == id) {
                prontos.remove(process);
                rodando.add(process);
                process.processState = PCB.ProcessState.RUNNING;
                so.utils.execProgram(process.memPage, gm.getTamPg());
                desalocaProcesso(id);
                return;
            }
        }
        throw new IllegalStateException("Process not found in ready queue.");
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