package Sistema.Gerenciadores;

import Sistema.Hardware.Interrupts;
import Sistema.Sistema;
import Sistema.SistemaOperacional.PCB;

public class Scheduler extends Thread {
    private final int DELTA = 5; // número de instruções por time slice
    private Sistema sistema;
    private PCB processoAtual;
    private int contadorInstrucoes;

    public Scheduler(Sistema sistema) {
        this.sistema = sistema;
        this.contadorInstrucoes = 0;
    }

    public void run() {
        while (true) {
            // Aguarda processo na fila READY
            while ((sistema.so.gp.ready.isEmpty() && processoAtual == null) || (sistema.hw.cpu.currentProcess == null && sistema.so.gp.ready.isEmpty())) {
                processoAtual = null;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (processoAtual == null) {
                processoAtual = sistema.so.gp.loadNext();
            }

            restoreContext(processoAtual);
            sistema.hw.cpu.currentProcess = processoAtual;
            System.out.println("Scheduler: processo " + processoAtual.processID + " carregado");
            sistema.readCpuMutex.release(); // Libera CPU para executar 5 instruções
            sistema.writeCpuMutex.acquireUninterruptibly(); // Espera CPU terminar slice
            if(!interruptTreat()) {
                saveContext(processoAtual);

                // Verifica se há outro processo pronto
                if (!sistema.so.gp.ready.isEmpty()) {
                    sistema.so.gp.unload(processoAtual.processID); // volta atual para ready
                    processoAtual = null; // sinaliza troca
                }
            }
        }
    }

    private boolean interruptTreat() {
        if(sistema.hw.cpu.irpt == Interrupts.noInterrupt || sistema.hw.cpu.irpt == Interrupts.NONE) return false;

        System.out.println("Interrupção: " + sistema.hw.cpu.irpt + " salvando contexto do processo " + processoAtual.processID);

        saveContext(processoAtual);

        if (sistema.hw.cpu.irpt == Interrupts.intIO) {
            contadorInstrucoes = 0;
            return true;
        } else {
            try {
                sistema.so.gp.unload(processoAtual.processID);
                processoAtual = sistema.so.gp.loadNext();
                contadorInstrucoes = 0;
                return true;
            } catch (NullPointerException ignored) {
            }
        }
        return false;
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
