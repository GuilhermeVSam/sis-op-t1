package Sistema;

import Sistema.Hardware.CPU;
import Sistema.Hardware.HW;
import Sistema.Hardware.Memory;
import Sistema.SistemaOperacional.Programs;
import Sistema.SistemaOperacional.SO;

import java.util.concurrent.Semaphore;

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

    public Semaphore readCpuMutex = new Semaphore(0);
    public Semaphore writeCpuMutex = new Semaphore(0);
    public static Semaphore readMemMutex = new Semaphore(0);
    public Semaphore dispatcherSemaphore = new Semaphore(0);

    public Sistema(int tamMem, int tamPg) { // tamMem = 1024 palavras, tamPg = 16 palavras
        Memory mem = new Memory(tamMem);
        CPU cpu = new CPU(mem, true);
        hw = new HW(mem, cpu);           // memoria do HW tem tamMem palavras
        so = new SO(hw, tamPg);       // cria o sistema operacional
        hw.cpu.setUtilities(so.utils); // permite cpu fazer dump de memoria ao avancar
        hw.cpu.setSistema(this);
        progs = new Programs();
        hw.cpu.start();
    }
}