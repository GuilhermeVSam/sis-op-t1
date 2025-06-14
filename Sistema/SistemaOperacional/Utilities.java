package Sistema.SistemaOperacional;

import Sistema.Hardware.HW;
import Sistema.Hardware.Opcode;
import Sistema.Hardware.Word;

public class Utilities {
    private HW hw;

    public Utilities(HW _hw) {
        hw = _hw;
    }

    public void loadProgram(Word[] program, int[] allocatedPages, int pageSize) {
        Word[] physicalMemory = hw.mem.pos;
        int programCounter = 0;

        for (int pageFrame : allocatedPages) {
            if (pageFrame < 0 || pageFrame >= (hw.mem.pos.length / pageSize)) {
                throw new IllegalArgumentException("Invalid page frame: " + pageFrame);
            }

            int physicalStart = pageFrame * pageSize;
            int physicalEnd = physicalStart + pageSize;

            while (programCounter < program.length && physicalStart < physicalEnd) {
                Word source = program[programCounter];
                Word dest = physicalMemory[physicalStart];

                dest.opc = source.opc;
                dest.ra = source.ra;
                dest.rb = source.rb;
                dest.p = source.p;


                programCounter++;
                physicalStart++;
            }

            if (programCounter >= program.length) break;
        }

        if (programCounter < program.length) {
            throw new IllegalStateException("Sistema.SistemaOperacional.Program too large for allocated pages");
        }
    }

    // dump da memória
    public void dump(Word w) { // funcoes de DUMP nao existem em hardware - colocadas aqui para facilidade
        System.out.print("[ ");
        System.out.print(w.opc);
        System.out.print(", ");
        System.out.print(w.ra);
        System.out.print(", ");
        System.out.print(w.rb);
        System.out.print(", ");
        System.out.print(w.p);
        System.out.println("  ] ");
    }

    public void dump(int ini, int fim) {
        Word[] m = hw.mem.pos; // m[] é o array de posições memória do hw
        for (int i = ini; i < fim; i++) {
            System.out.print(i);
            System.out.print(":  ");
            dump(m[i]);
        }
    }

    public void clearMemory(int allocatedPages[], int pageSize) {
        for (int pageFrame : allocatedPages) {
            if (pageFrame < 0 || pageFrame >= (hw.mem.pos.length / pageSize)) {
                throw new IllegalArgumentException("Invalid page frame: " + pageFrame);
            }
            int physicalStart = pageFrame * pageSize;
            int physicalEnd = physicalStart + pageSize;

            for (int j = physicalStart; j < physicalEnd; j++) {
                hw.mem.pos[j].opc = Opcode.___;
                hw.mem.pos[j].ra = -1;
                hw.mem.pos[j].rb = -1;
                hw.mem.pos[j].p = -1;
            }
        }
    }
}