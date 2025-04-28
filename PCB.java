public class PCB {
    int processID;
    int parentProcessID;
    int programCounter;
    int[] registers;
    int[] memPage;
    ProcessState processState;
    public PCB(int processID, int programCounter, int[] registers, int[] memPage) {
        this.processID = processID;
        this.programCounter = programCounter;
        this.processState = ProcessState.READY;
        this.registers = registers;
        this.memPage = memPage;
    }


    public enum ProcessState {READY, RUNNING, BLOCKED}
}
