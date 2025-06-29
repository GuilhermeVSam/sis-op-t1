package Sistema.SistemaOperacional;

public class PCB {
    String programName;
    public int processID;
    int parentProcessID;
    public int programCounter;
    public int[] registers;
    int[] memPage;
    ProcessState processState;
    public PCB(String programName, int processID, int programCounter, int[] registers, int[] memPage) {
        this.programName = programName;
        this.processID = processID;
        this.programCounter = programCounter;
        this.processState = ProcessState.READY;
        this.registers = registers;
        this.memPage = memPage;
    }


    public enum ProcessState {READY, RUNNING, BLOCKED}

    @Override
    public String toString() {
        return processID + "     " +
                parentProcessID + "    " +
                programName + "    " +
                processState + "    ";
    }
}
