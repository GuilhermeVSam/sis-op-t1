public class Scheduler {
    private final int DELTA = 5; // número de instruções por time slice
    private Sistema sistema;
    private PCB processoAtual;
    private int contadorInstrucoes;

    public Scheduler(Sistema sistema) {
        this.sistema = sistema;
        this.contadorInstrucoes = 0;
    }

    public void execAll() {
        while (!sistema.so.gp.prontos.isEmpty() || processoAtual != null) {
            if (processoAtual == null) {
                processoAtual = sistema.so.gp.loadNext();
                if (processoAtual == null) {
                    break;
                }
                restaurarContexto(processoAtual);
            }

            executarUmaInstrucao(processoAtual);

            contadorInstrucoes++;

            if (contadorInstrucoes >= DELTA) {
                tratadorInterrupcao();
            }
        }
    }

    private void executarUmaInstrucao(PCB processo) {
        if (sistema.hw.cpu.getPC() == -1) {
            return;
        }

        // Simula execução de uma única instrução
        sistema.hw.cpu.run(processo.processID, sistema.so.gm);

        // Verificar se o processo terminou (por exemplo, instrução STOP)
        if (sistema.hw.cpu.isCpuStop()) {
            System.out.println("Processo " + processo.processID + " terminou.");
            try {
                sistema.so.gp.desalocaProcesso(processo.processID);
            } catch (IllegalStateException e) {
                System.out.println("Processo já foi desalocado.");
            }
            processoAtual = null;
        }

    }

    private void tratadorInterrupcao() {
        try {
            System.out.println("Interrupção: salvando contexto do processo " + processoAtual.processID);

            salvarContexto(processoAtual);

            sistema.so.gp.unload(processoAtual.processID);

            processoAtual = sistema.so.gp.loadNext();

            if (processoAtual != null) {
                restaurarContexto(processoAtual);
            }

            contadorInstrucoes = 0;
        } catch (NullPointerException ignored) {
        }
    }

    private void salvarContexto(PCB processo) {
        processo.programCounter = sistema.hw.cpu.getPC();
        processo.registers = sistema.hw.cpu.getRegisters().clone();
    }

    private void restaurarContexto(PCB processo) {
        sistema.hw.cpu.setContext(processo.programCounter);
        sistema.hw.cpu.setRegisters(processo.registers.clone());
    }
}
