import java.util.ArrayList;
import java.util.Queue;

public class GP implements GP_Interface {
    GM gm;
    ArrayList<Boolean> processID;
    Queue<PCB> rodando;
    Queue<PCB> prontos;
    // PCB[] bloqueados;

    public GP(GM gm) {
        this.gm = gm;
        processID = new ArrayList<>();
    }

    @Override
    public boolean criaProcesso(Sistema.Word[] programa) {
        int tamanhoPrograma = programa.length;
        int[] paginasMemoria = gm.aloca(tamanhoPrograma);
        int programCounter = paginasMemoria[0];
        PCB processControlBlock = new PCB(getProcessID(), programCounter, new int[10], paginasMemoria);
        prontos.add(processControlBlock);
        return true;

    }

    @Override
    public void desalocaProcesso(int id) {
        if (processID.get(id)) {
            for (PCB process : rodando) {
                if (process.processID == id) {
                    gm.desaloca(process.memPage);
                    rodando.remove(process);
                    processID.set(id, false);
                    return;
                }
            }
            for (PCB process : prontos) {
                if (process.processID == id) {
                    gm.desaloca(process.memPage);
                    prontos.remove(process);
                    processID.set(id, false);
                    return;
                }
            }
        }
    }

    public int getProcessID() {
        for (int i = 0; i < processID.size(); i++) {
            if (!processID.get(i)) {
                processID.set(i, true);
                return i;
            }
        }
        return 0;
    }
}
