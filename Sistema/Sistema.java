package Sistema;

import Sistema.Hardware.HW;
import Sistema.SistemaOperacional.Programs;
import Sistema.SistemaOperacional.SO;

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
}