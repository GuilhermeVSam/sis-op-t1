package Sistema.Hardware;


import Sistema.Sistema;
import Sistema.SistemaOperacional.*;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class CPU extends Thread {
    private int maxInt; // valores maximo e minimo para inteiros nesta cpu
    private int minInt;
    // CONTEXTO da CPU ...
    public int pc;     // ... composto de program counter,
    private Word ir;    // instruction register,
    public int[] reg;  // registradores da CPU
    public Interrupts irpt; // durante instrucao, interrupcao pode ser sinalizada
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

    Sistema sistema;

    public PCB currentProcess;
    private int MAX_INSTRUCTIONS = 5;

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

    public void setDebug(boolean _debug) {
        debug = _debug;            // se true, print da instrucao em execucao
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

    public void run() {
        while (true) {
            sistema.readCpuMutex.acquireUninterruptibly(); // Espera sinal do scheduler

            if (currentProcess != null) {
                System.out.println("CPU: executando processo " + currentProcess.processID);

                for (int i = 0; i < MAX_INSTRUCTIONS; i++) {
                    execOnce(); // roda uma instrução
                    if( irpt == Interrupts.intIO ){
                        currentProcess = null;
                        break;
                    }
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (currentProcess == null) break; // processo terminou
                }
                sistema.writeCpuMutex.release(); // Libera scheduler para decidir o próximo passo
            }
        }
    }

    public void handleIOInterrupt(PCB process) throws InterruptedException {
        System.out.println("IO: processo " + process.processID + " pediu por IO");
        Semaphore IOReturn = new Semaphore(0);
        sysCall.handle(IOReturn);
        IOReturn.acquireUninterruptibly();
        Thread.sleep(2000);
        System.out.println("IO: processo " + process.processID + " teve retorno de IO");
        sistema.so.gp.unblock(process.processID);
    }

    private void execOnce() {
        PCB processo = currentProcess;
        if (pc == -1) {
            return;
        }

        // Simula execução de uma única instrução
        runInstruction(processo.processID, sistema.so.gm);

        // Verificar se o processo terminou (por exemplo, instrução STOP)
        if (isCpuStop()) {
            System.out.println("Processo " + processo.processID + " terminou.");
            try {
                sistema.so.gp.removeRunningProcess(processo.processID);
                currentProcess = null; // IMPORTANTE
            } catch (IllegalStateException e) {
                System.out.println("Processo já foi desalocado.");
            }
        }
    }

    public void runInstruction(int pid, GM gm) {                               // execucao da CPU supoe que o contexto da CPU, vide acima,
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
                    addrLDD = gm.translate(pid, ir.p); // traduzir endereco logico
                    if (legal(addrLDD)) {
                        reg[ir.ra] = m[addrLDD].p;
                        pc++;
                    }
                    break;
                case LDX: // RD <- [RS] // NOVA
                    if (legal(reg[ir.rb])) {
                        int memAddr = gm.translate(pid, reg[ir.rb]); // traduzir endereco logico
                        reg[ir.ra] = m[memAddr].p;
                        pc++;
                    }
                    break;
                case STD:
                    addrSTD = gm.translate(pid, ir.p);
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
                    addrJMP = gm.translate(pid, ir.p);
                    pc = addrJMP;
                    break;
                case JMPIM: // PC <- [A]
                    addrJMP = gm.translate(pid, ir.p);
                    pc = m[addrJMP].p;
                    break;
                case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                    if (reg[ir.rb] > 0) {
                        addrJMP = gm.translate(pid, ir.ra);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIGK: // If RC > 0 then PC <- k else PC++
                    if (reg[ir.rb] > 0) {
                        addrJMP = gm.translate(pid, ir.p);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPILK: // If RC < 0 then PC <- k else PC++
                    if (reg[ir.rb] < 0) {
                        addrJMP = gm.translate(pid, ir.p);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIEK: // If RC = 0 then PC <- k else PC++
                    if (reg[ir.rb] == 0) {
                        addrJMP = gm.translate(pid, ir.p);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                    if (reg[ir.rb] < 0) {
                        addrJMP = gm.translate(pid, reg[ir.ra]);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                    if (reg[ir.rb] == 0) {
                        addrJMP = gm.translate(pid, reg[ir.ra]);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIGM: // If RC > 0 then PC <- [A] else PC++
                    addrJMP = gm.translate(pid, ir.p);
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
                        addrJMP = gm.translate(pid, ir.p);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIEM: // If RC = 0 then PC <- k else PC++
                    if (reg[ir.rb] == 0) {
                        addrJMP = gm.translate(pid, ir.p);
                        pc = addrJMP;
                    } else {
                        pc++;
                    }
                    break;
                case JMPIGT: // If RS>RC then PC <- k else PC++
                    if (reg[ir.ra] > reg[ir.rb]) {
                        addrJMP = gm.translate(pid, ir.p);
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
                    PCB process = currentProcess;
                    new Thread(() -> {
                        try {
                            handleIOInterrupt(process);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start(); // <<<<< aqui desvia para rotina de chamada de sistema, no momento so
                    // temos IO
                    sistema.so.gp.block(currentProcess.processID);
                    irpt = Interrupts.intIO;
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

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }
}