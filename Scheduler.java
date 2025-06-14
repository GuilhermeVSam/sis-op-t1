public class Scheduler extends Thread {
    private final int DELTA = 5; // número de instruções por time slice
    private Sistema sistema;
    private PCB processoAtual;
    private int contadorInstrucoes;

    public Scheduler(Sistema sistema) {
        this.sistema = sistema;
        this.contadorInstrucoes = 0;
    }

    public void exec(int id){
        if(sistema.so.gp.processID[id]){
            processoAtual = sistema.so.gp.load(id);
            restoreContext(processoAtual);
            while (processoAtual != null) {
                execOnce(processoAtual);
                contadorInstrucoes++;
                if (contadorInstrucoes >= DELTA) {
                    interruptTreat();
                }
            }
        } else {
            System.out.println("Processo não encontrado.");
        }
    }

    public void run() {
        while (!sistema.so.gp.ready.isEmpty() || processoAtual != null) {
            if (processoAtual == null) {
                processoAtual = sistema.so.gp.loadNext();
                if (processoAtual == null) {
                    break;
                }
                restoreContext(processoAtual);
            }

            execOnce(processoAtual);

            try {
                // Aguarda 5000 milissegundos (5 segundos)
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // Trata a exceção caso a thread seja interrompida
                Thread.currentThread().interrupt(); // restaura o status de interrupção
                System.err.println("Thread interrompida durante o sleep");
            }

            contadorInstrucoes++;

            if (contadorInstrucoes >= DELTA) {
                interruptTreat();
            }
        }
    }

    private void execOnce(PCB processo) {
        if (sistema.hw.cpu.getPC() == -1) {
            return;
        }

        // Simula execução de uma única instrução
        sistema.hw.cpu.run(processo.processID, sistema.so.gm);

        // Verificar se o processo terminou (por exemplo, instrução STOP)
        if (sistema.hw.cpu.isCpuStop()) {
            System.out.println("Processo " + processo.processID + " terminou.");
            try {
                sistema.so.gp.removeProcess(processo.processID);
            } catch (IllegalStateException e) {
                System.out.println("Processo já foi desalocado.");
            }
            processoAtual = null;
        }

    }

    private void interruptTreat() {
        try {
            System.out.println("Interrupção: salvando contexto do processo " + processoAtual.processID);

            saveContext(processoAtual);

            sistema.so.gp.unload(processoAtual.processID);

            processoAtual = sistema.so.gp.loadNext();

            if (processoAtual != null) {
                restoreContext(processoAtual);
            }

            contadorInstrucoes = 0;
        } catch (NullPointerException ignored) {
        }
    }

    private void saveContext(PCB processo) {
        processo.programCounter = sistema.hw.cpu.getPC();
        processo.registers = sistema.hw.cpu.getRegisters().clone();
    }

    private void restoreContext(PCB processo) {
        sistema.hw.cpu.setContext(processo.programCounter);
        sistema.hw.cpu.setRegisters(processo.registers.clone());
    }
}
