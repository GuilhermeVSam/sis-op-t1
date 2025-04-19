import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class GP implements GP_Interface {
    GM gm;
    Sistema.SO so;
    boolean[] processID;
    Queue<PCB> rodando;
    Queue<PCB> prontos;
    // Queue<PCB> bloqueados;

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
        int programCounter = paginasMemoria[0];
        PCB processControlBlock = new PCB(getProcessID(), programCounter, new int[10], paginasMemoria);
        prontos.add(processControlBlock);
        so.utils.loadProgram(programa, paginasMemoria, gm.getTamPg());
        return processControlBlock.processID;
    }

    @Override
    public void desalocaProcesso(int id) {
        if (processID[id]) {
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
        }
    }

    public void load(int id){
        for (PCB process : prontos) {
            if(process.processID == id){
                so.utils.execProgram(process.memPage, gm.getTamPg());
            }
        }
    }

    public int getProcessID() {
        for(int i = 0; i < processID.length; i++){
            if(!processID[i]){
                processID[i] = true;
                return i;
            }
        }
        return 0;
    }
}
