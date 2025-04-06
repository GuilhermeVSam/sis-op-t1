public class PCB {
    private int processID;
    private int programCounter;
    private int[] registers;
    private int[] paginasMemoria;

    public PCB(int processID, int programCounter, int[] registers, int[] paginasMemoria) {
        this.processID = processID;
        this.programCounter = programCounter;
        this.registers = registers;
        this.paginasMemoria = paginasMemoria;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public int[] getRegisters() {
        return registers;
    }

    public void setRegisters(int[] registers) {
        this.registers = registers;
    }

    public int[] getPaginasMemoria() {
        return paginasMemoria;
    }

    public void setPaginasMemoria(int[] paginasMemoria) {
        this.paginasMemoria = paginasMemoria;
    }
}
