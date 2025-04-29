// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Estrutura deste código:
//    Todo código está dentro da classe *Sistema*
//    Dentro de Sistema, encontra-se acima a definição de HW:
//           Memory,  Word, 
//           CPU tem Opcodes (codigos de operacoes suportadas na cpu),
//               e Interrupcoes possíveis, define o que executa para cada instrucao
//           VM -  a máquina virtual é uma instanciação de CPU e Memória
//    Depois as definições de SW:
//           no momento são esqueletos (so estrutura) para
//					InterruptHandling    e
//					SysCallHandling 
//    A seguir temos utilitários para usar o sistema
//           carga, início de execução e dump de memória
//    Por último os programas existentes, que podem ser copiados em memória.
//           Isto representa programas armazenados.
//    Veja o main.  Ele instancia o Sistema com os elementos mencionados acima.
//           em seguida solicita a execução de algum programa com  loadAndExec

import java.util.Arrays;
import java.util.Scanner;

public class Sistema {

    // -------------------------------------------------------------------------------------------------------
    // --------------------- H A R D W A R E - definicoes de HW
    // ----------------------------------------------

    // -------------------------------------------------------------------------------------------------------
    // --------------------- M E M O R I A - definicoes de palavra de memoria,
    // memória ----------------------

    public HW hw;
    public SO so;

    // -------------------------------------------------------------------------------------------------------
    // --------------------- C P U - definicoes da CPU
    // -----------------------------------------------------
    public Programs progs;

    public Sistema(int tamMem, int tamPg) { // tamMem = 1024 palavras, tamPg = 16 palavras
        hw = new HW(tamMem);           // memoria do HW tem tamMem palavras
        so = new SO(hw, tamPg);       // cria o sistema operacional
        hw.cpu.setUtilities(so.utils); // permite cpu fazer dump de memoria ao avancar
        progs = new Programs();
    }

    public static void main(String args[]) {
        Sistema s = new Sistema(1024, 16);
    }
    // ------------------ C P U - fim
    // -----------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------

//    public void run() {
//        Scheduler scheduler = new Scheduler(this);
//
//        // Primeiro: cria dois processos iniciais
//        Word[] simpleProg1 = createSimpleProgram();
//        Word[] simpleProg2 = createSimpleProgram();
//
//        int id1 = so.gp.criaProcesso(simpleProg1);
//        System.out.println("Processo " + id1 + " criado.");
//
//        int id2 = so.gp.criaProcesso(simpleProg2);
//        System.out.println("Processo " + id2 + " criado.");
//
//        // Cria a thread que executa os processos
//        Thread schedulerThread = new Thread(() -> {
//            while (true) {
//                if (!so.gp.prontos.isEmpty()) {  // verifica se há processos prontos
//                    scheduler.execAll();         // só executa se houver processo
//                }
//                try {
//                    Thread.sleep(100);            // descanso de 100ms
//                } catch (InterruptedException e) {
//                    break; // se a thread for interrompida, para
//                }
//            }
//        });
//
//        schedulerThread.start();
//
//        // Loop de comandos do usuário
//        Scanner scanner = new Scanner(System.in);
//
//        while (true) {
//            System.out.println("Digite um comando (load / exit): ");
//            String comando = scanner.nextLine();
//
//            if (comando.equalsIgnoreCase("load")) {
//                Word[] novoProg = createSimpleProgram();
//                int id = so.gp.criaProcesso(novoProg);
//                System.out.println("Novo processo " + id + " criado e colocado na fila de prontos.");
//            } else if (comando.equalsIgnoreCase("exit")) {
//                System.out.println("Encerrando sistema...");
//                schedulerThread.interrupt();
//                break;
//            } else {
//                System.out.println("Comando inválido!");
//            }
//        }
//
//        scanner.close();
//    }
    // -------------------------------------------------------------------------------------------------------

    // --------------------H A R D W A R E - fim
    // -------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // ------------------- SW - inicio - Sistema Operacional
    // -------------------------------------------------

    // Também fora da função run(), a função para criar programas simples:
    public Word[] createSimpleProgram() {
        return new Word[]{
                new Word(Opcode.LDI, 0, -1, 0),
                new Word(Opcode.ADDI, 0, -1, 1),
                new Word(Opcode.ADDI, 0, -1, 1),
                new Word(Opcode.ADDI, 0, -1, 1),
                new Word(Opcode.ADDI, 0, -1, 1),
                new Word(Opcode.ADDI, 0, -1, 1),
                new Word(Opcode.STOP, -1, -1, -1)
        };
    }

    public enum Opcode {
        DATA, ___,                      // se memoria nesta posicao tem um dado, usa DATA, se nao usada ee NULO ___
        JMP, JMPI, JMPIG, JMPIL, JMPIE, // desvios
        JMPIM, JMPIGM, JMPILM, JMPIEM,
        JMPIGK, JMPILK, JMPIEK, JMPIGT,
        ADDI, SUBI, ADD, SUB, MULT,    // matematicos
        LDI, LDD, STD, LDX, STX, MOVE, // movimentacao
        SYSCALL, STOP                  // chamada de sistema e parada
    }

    // ------------------ U T I L I T A R I O S D O S I S T E M A
    // -----------------------------------------
    // ------------------ load é invocado a partir de requisição do usuário

    public enum Interrupts {           // possiveis interrupcoes que esta CPU gera
        noInterrupt, intEnderecoInvalido, intInstrucaoInvalida, intOverflow, intSTOP, NONE;
    }

    public static class Word {    // cada posicao da memoria tem uma instrucao (ou um dado)
        public Opcode opc; //
        public int ra;     // indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
        public int rb;     // indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
        public int p;      // parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO

        public Word(Opcode _opc, int _ra, int _rb, int _p) { // vide definição da VM - colunas vermelhas da tabela
            opc = _opc;
            ra = _ra;
            rb = _rb;
            p = _p;
        }
    }
    // -------------------------------------------------------------------------------------------------------
    // ------------------- S I S T E M A
    // --------------------------------------------------------------------

    public class Memory {
        public Word[] pos; // pos[i] é a posição i da memória. cada posição é uma palavra.

        public Memory(int size) {
            pos = new Word[size];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = new Word(Opcode.___, -1, -1, -1);
            }
            ; // cada posicao da memoria inicializada
        }
    }

    public class CPU {
        private int maxInt; // valores maximo e minimo para inteiros nesta cpu
        private int minInt;
        // CONTEXTO da CPU ...
        private int pc;     // ... composto de program counter,
        private Word ir;    // instruction register,
        private int[] reg;  // registradores da CPU
        private Interrupts irpt; // durante instrucao, interrupcao pode ser sinalizada
        // FIM CONTEXTO DA CPU: tudo que precisa sobre o estado de um processo para
        // executa-lo
        // nas proximas versoes isto pode modificar

        private Word[] m;   // m é o array de memória "física", CPU tem uma ref a m para acessar

        private InterruptHandling ih;    // significa desvio para rotinas de tratamento de Int - se int ligada, desvia
        private SysCallHandling sysCall; // significa desvio para tratamento de chamadas de sistema

        private boolean cpuStop;    // flag para parar CPU - caso de interrupcao que acaba o processo, ou chamada stop -
        // nesta versao acaba o sistema no fim do prog

        // auxilio aa depuração
        private boolean debug;      // se true entao mostra cada instrucao em execucao
        private Utilities u;        // para debug (dump)

        public CPU(Memory _mem, boolean _debug) { // ref a MEMORIA passada na criacao da CPU
            maxInt = 32767;            // capacidade de representacao modelada
            minInt = -32767;           // se exceder deve gerar interrupcao de overflow
            m = _mem.pos;              // usa o atributo 'm' para acessar a memoria, só para ficar mais pratico
            reg = new int[10];         // aloca o espaço dos registradores - regs 8 e 9 usados somente para IO

            debug = _debug;            // se true, print da instrucao em execucao
        }

        public void setAddressOfHandlers(InterruptHandling _ih, SysCallHandling _sysCall) {
            ih = _ih;                  // aponta para rotinas de tratamento de int
            sysCall = _sysCall;        // aponta para rotinas de tratamento de chamadas de sistema
        }

        public void setUtilities(Utilities _u) {
            u = _u;                     // aponta para rotinas utilitárias - fazer dump da memória na tela
        }

        // verificação de enderecamento
        private boolean legal(int e) { // todo acesso a memoria tem que ser verificado se é válido -
            // aqui no caso se o endereco é um endereco valido em toda memoria
            if (e >= 0 && e < m.length) {
                return true;
            } else {
                irpt = Interrupts.intEnderecoInvalido;    // se nao for liga interrupcao no meio da exec da instrucao
                return false;
            }
        }

        private boolean testOverflow(int v) {             // toda operacao matematica deve avaliar se ocorre overflow
            if ((v < minInt) || (v > maxInt)) {
                irpt = Interrupts.intOverflow;            // se houver liga interrupcao no meio da exec da instrucao
                return false;
            }
            ;
            return true;
        }

        public void setContext(int _pc) {                 // usado para setar o contexto da cpu para rodar um processo
            // [ nesta versao é somente colocar o PC na posicao 0 ]
            pc = _pc;                                     // pc cfe endereco logico
            irpt = Interrupts.noInterrupt;                // reset da interrupcao registrada
        }

        public void resetRegisters() {
            Arrays.fill(reg, 0);  // Clear all general-purpose registers
            pc = -1;              // Invalidate PC
            irpt = Interrupts.NONE;  // Clear interrupts
        }


        public void run(int pid, GM gm) {                               // execucao da CPU supoe que o contexto da CPU, vide acima,
            // esta devidamente setado
            cpuStop = false;
            // --------------------------------------------------------------------------------------------------
            // FASE DE FETCH
            if (legal(pc)) { // pc valido
                ir = m[pc];  // <<<<<<<<<<<< AQUI faz FETCH - busca posicao da memoria apontada por pc, guarda em ir
                // resto é dump de debug
                if (debug) {
                    System.out.print("                                              regs: ");
                    for (int i = 0; i < 10; i++) {
                        System.out.print(" r[" + i + "]:" + reg[i]);
                    }
                    ;
                    System.out.println();
                }
                if (debug) {
                    System.out.print("                      pc: " + pc + "       exec: ");
                    u.dump(ir);
                }
                int addrJMP, addrLDD, addrSTD;
                // --------------------------------------------------------------------------------------------------
                // FASE DE EXECUCAO DA INSTRUCAO CARREGADA NO ir
                switch (ir.opc) {       // conforme o opcode (código de operação) executa

                    // Instrucoes de Busca e Armazenamento em Memoria
                    case LDI: // Rd ← k        veja a tabela de instrucoes do HW simulado para entender a semantica da instrucao
                        reg[ir.ra] = ir.p;
                        pc++;
                        break;
                    case LDD: // Rd <- [A]
                        addrLDD = gm.traduzir(pid, ir.p); // traduzir endereco logico
                        if (legal(addrLDD)) {
                            reg[ir.ra] = m[addrLDD].p;
                            pc++;
                        }
                        break;
                    case LDX: // RD <- [RS] // NOVA
                        if (legal(reg[ir.rb])) {
                            int memAddr = gm.traduzir(pid, reg[ir.rb]); // traduzir endereco logico
                            reg[ir.ra] = m[memAddr].p;
                            pc++;
                        }
                        break;
                    case STD:
                        addrSTD = gm.traduzir(pid, ir.p);
                        if (legal(addrSTD)) {
                            m[addrSTD].opc = Opcode.DATA;
                            m[addrSTD].p = reg[ir.ra];
                            pc++;
                            if (debug) {
                                System.out.print("                                  ");
                                u.dump(addrSTD, addrSTD + 1);
                            }
                        }
                        break;
                    case STX: // [Rd] ←Rs
                        if (legal(reg[ir.ra])) {
                            m[reg[ir.ra]].opc = Opcode.DATA;
                            m[reg[ir.ra]].p = reg[ir.rb];
                            pc++;
                        }
                        ;
                        break;
                    case MOVE: // RD <- RS
                        reg[ir.ra] = reg[ir.rb];
                        pc++;
                        break;
                    // Instrucoes Aritmeticas
                    case ADD: // Rd ← Rd + Rs
                        reg[ir.ra] = reg[ir.ra] + reg[ir.rb];
                        testOverflow(reg[ir.ra]);
                        pc++;
                        break;
                    case ADDI: // Rd ← Rd + k
                        reg[ir.ra] = reg[ir.ra] + ir.p;
                        testOverflow(reg[ir.ra]);
                        pc++;
                        break;
                    case SUB: // Rd ← Rd - Rs
                        reg[ir.ra] = reg[ir.ra] - reg[ir.rb];
                        testOverflow(reg[ir.ra]);
                        pc++;
                        break;
                    case SUBI: // RD <- RD - k // NOVA
                        reg[ir.ra] = reg[ir.ra] - ir.p;
                        testOverflow(reg[ir.ra]);
                        pc++;
                        break;
                    case MULT: // Rd <- Rd * Rs
                        reg[ir.ra] = reg[ir.ra] * reg[ir.rb];
                        testOverflow(reg[ir.ra]);
                        pc++;
                        break;

                    // Instrucoes JUMP
                    case JMP: // PC <- k
                        addrJMP = gm.traduzir(pid, ir.p);
                        pc = addrJMP;
                        break;
                    case JMPIM: // PC <- [A]
                        addrJMP = gm.traduzir(pid, ir.p);
                        pc = m[addrJMP].p;
                        break;
                    case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                        if (reg[ir.rb] > 0) {
                            addrJMP = gm.traduzir(pid, ir.ra);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIGK: // If RC > 0 then PC <- k else PC++
                        if (reg[ir.rb] > 0) {
                            addrJMP = gm.traduzir(pid, ir.p);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPILK: // If RC < 0 then PC <- k else PC++
                        if (reg[ir.rb] < 0) {
                            addrJMP = gm.traduzir(pid, ir.p);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIEK: // If RC = 0 then PC <- k else PC++
                        if (reg[ir.rb] == 0) {
                            addrJMP = gm.traduzir(pid, ir.p);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                        if (reg[ir.rb] < 0) {
                            addrJMP = gm.traduzir(pid, reg[ir.ra]);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                        if (reg[ir.rb] == 0) {
                            addrJMP = gm.traduzir(pid, reg[ir.ra]);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIGM: // If RC > 0 then PC <- [A] else PC++
                        addrJMP = gm.traduzir(pid, ir.p);
                        if (legal(addrJMP)) {
                            if (reg[ir.rb] > 0) {
                                pc = addrJMP;
                            } else {
                                pc++;
                            }
                        }
                        break;
                    case JMPILM: // If RC < 0 then PC <- k else PC++
                        if (reg[ir.rb] < 0) {
                            addrJMP = gm.traduzir(pid, ir.p);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIEM: // If RC = 0 then PC <- k else PC++
                        if (reg[ir.rb] == 0) {
                            addrJMP = gm.traduzir(pid, ir.p);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;
                    case JMPIGT: // If RS>RC then PC <- k else PC++
                        if (reg[ir.ra] > reg[ir.rb]) {
                            addrJMP = gm.traduzir(pid, ir.p);
                            pc = addrJMP;
                        } else {
                            pc++;
                        }
                        break;

                    case DATA: // pc está sobre área supostamente de dados
                        irpt = Interrupts.intInstrucaoInvalida;
                        cpuStop = true;
                        break;

                    // Chamadas de sistema
                    case SYSCALL:
                        sysCall.handle(); // <<<<< aqui desvia para rotina de chamada de sistema, no momento so
                        // temos IO
                        pc++;
                        break;

                    case STOP: // por enquanto, para execucao
                        sysCall.stop();
                        cpuStop = true;
                        resetRegisters();
                        break;

                    case ___:
                        // pc está sobre área supostamente de dados
                        irpt = Interrupts.intInstrucaoInvalida;
                        cpuStop = true;
                        break;
                    // Inexistente
                    default:
                        irpt = Interrupts.intInstrucaoInvalida;
                        break;
                }
            }
        }

        // GET e SET do pc
        public int getPC() {
            return pc;
        }

        public void setPC(int pc) {
            this.pc = pc;
        }

        // GET e SET dos registradores
        public int[] getRegisters() {
            return reg;
        }

        public void setRegisters(int[] reg) {
            this.reg = reg;
        }

        // GET do cpuStop
        public boolean isCpuStop() {
            return cpuStop;
        }

    }

    // ------------------- HW - constituido de CPU e MEMORIA
    // -----------------------------------------------
    public class HW {
        public Memory mem;
        public CPU cpu;

        public HW(int tamMem) {
            mem = new Memory(tamMem);
            cpu = new CPU(mem, true); // true liga debug
        }
    }

    // ------------------- I N T E R R U P C O E S - rotinas de tratamento
    // ----------------------------------
    public class InterruptHandling {
        private HW hw; // referencia ao hw se tiver que setar algo

        public InterruptHandling(HW _hw) {
            hw = _hw;
        }

        public void handle(Interrupts irpt) {
            // apenas avisa - todas interrupcoes neste momento finalizam o programa
            System.out.println(
                    "                                               Interrupcao " + irpt + "   pc: " + hw.cpu.pc);
        }
    }

    // ------------------- C H A M A D A S D E S I S T E M A - rotinas de tratamento
    // ----------------------
    public class SysCallHandling {
        private HW hw; // referencia ao hw se tiver que setar algo

        public SysCallHandling(HW _hw) {
            hw = _hw;
        }

        public void stop() { // chamada de sistema indicando final de programa
            // nesta versao cpu simplesmente pára
            System.out.println("                                               SYSCALL STOP");
        }

        public void handle() { // chamada de sistema
            // suporta somente IO, com parametros
            // reg[8] = in ou out    e reg[9] endereco do inteiro
            System.out.println("SYSCALL pars:  " + hw.cpu.reg[8] + " / " + hw.cpu.reg[9]);

            if (hw.cpu.reg[8] == 1) {
                // leitura ...

            } else if (hw.cpu.reg[8] == 2) {
                // escrita - escreve o conteuodo da memoria na posicao dada em reg[9]
                System.out.println("OUT:   " + hw.mem.pos[hw.cpu.reg[9]].p);
            } else {
                System.out.println("  PARAMETRO INVALIDO");
            }
        }
    }

    // carga na memória
    public class Utilities {
        private HW hw;
        private GM gm;

        public Utilities(HW _hw) {
            hw = _hw;
            gm = GM.getInstance();
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
                throw new IllegalStateException("Program too large for allocated pages");
            }
        }

        // Helper methods
        private boolean isInstruction(Opcode opc) {
            return opc != Opcode.DATA && opc != Opcode.___;
        }

        public void execProgram(PCB pcb, int[] posMemoria, int tamPagina) {
            System.out.println("---------------------------------- programa carregado na memoria");
            dump(posMemoria[0] * tamPagina, (posMemoria[posMemoria.length - 1] + 1) * tamPagina);

            // Define o contexto inicial (primeira página)
            int paginaAtual = 0;
            int programCounter = posMemoria[paginaAtual] * tamPagina;
            hw.cpu.setContext(programCounter);

            // Loop principal de execução
            while (true) {
                // Verifica se o pc está dentro da página atual
                if (hw.cpu.pc == -1) break;
                int paginaDoPc = hw.cpu.pc / tamPagina;
                boolean paginaValida = false;

                // Verifica se o pc está em alguma das páginas alocadas
                for (int pagina : posMemoria) {
                    if (pagina == paginaDoPc) {
                        paginaValida = true;
                        break;
                    }
                }

                // Se o pc saiu das páginas alocadas, termina a execução
                if (!paginaValida) {
                    System.out.println("Execution stopped: PC left allocated memory area.");
                    break;
                }

                // Se atingiu um DATA ou área não executável, para
                if (hw.mem.pos[hw.cpu.pc].opc == Opcode.DATA || hw.mem.pos[hw.cpu.pc].opc == Opcode.___) {
                    System.out.println("Execution stopped: Attempted to execute non-instruction area.");
                    break;
                }

                // Se encontrou um STOP, termina
                if (hw.mem.pos[hw.cpu.pc].opc == Opcode.STOP) {
                    System.out.println("Execution stopped: STOP instruction encountered.");
                    break;
                }
                // Executa UMA instrução (a CPU atualiza o pc internamente)
                hw.cpu.run(pcb.processID, gm);
            }

            System.out.println("---------------------------------- memoria após execucao ");
            dump(posMemoria[0] * tamPagina, (posMemoria[posMemoria.length - 1] + 1) * tamPagina);
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

/*		private void loadAndExec(Word[] p) {
			loadProgram(p, ); // carga do programa na memoria
			System.out.println("---------------------------------- programa carregado na memoria");
			dump(0, p.length); // dump da memoria nestas posicoes
			hw.cpu.setContext(0); // seta pc para endereço 0 - ponto de entrada dos programas
			System.out.println("---------------------------------- inicia execucao ");
			hw.cpu.run(); // cpu roda programa ate parar
			System.out.println("---------------------------------- memoria após execucao ");
			dump(0, p.length); // dump da memoria com resultado
		}*/
    }

    public class SO {
        public InterruptHandling ih;
        public SysCallHandling sc;
        public Utilities utils;
        public GM gm;
        public GP gp;

        public SO(HW hw, int tamPg) {
            ih = new InterruptHandling(hw); // rotinas de tratamento de int
            sc = new SysCallHandling(hw); // chamadas de sistema
            gm = GM.getInstance(hw.mem.pos.length, tamPg); // gerenciador de memoria
            gp = new GP(gm, this);
            hw.cpu.setAddressOfHandlers(ih, sc);
            utils = new Utilities(hw);
        }
    }

}