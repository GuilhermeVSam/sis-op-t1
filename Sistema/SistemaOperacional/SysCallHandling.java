package Sistema.SistemaOperacional;

import Sistema.Hardware.HW;
import Sistema.Sistema;

import java.util.concurrent.Semaphore;

public class SysCallHandling {
    private HW hw; // referencia ao hw se tiver que setar algo

    public SysCallHandling(HW _hw) {
        hw = _hw;
    }

    public void stop() { // chamada de sistema indicando final de programa
        // nesta versao cpu simplesmente pára
        System.out.println("                                               SYSCALL STOP");
    }

    public void handle(Semaphore ioReturn) { // chamada de sistema
        // suporta somente IO, com parametros
        // reg[8] = in ou out    e reg[9] endereco do inteiro
        System.out.println("SYSCALL pars:  " + hw.cpu.reg[8] + " / " + hw.cpu.reg[9]);

        if (hw.cpu.reg[8] == 1) {
            // leitura ...
            ioReturn.release();

        } else if (hw.cpu.reg[8] == 2) {
            // escrita - escreve o conteuodo da memoria na posicao dada em reg[9]
            System.out.println("OUT:   " + hw.mem.pos[hw.cpu.reg[9]].p);
            ioReturn.release();
        } else {
            System.out.println("  PARAMETRO INVALIDO");
        }
    }
}