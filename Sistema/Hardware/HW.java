package Sistema.Hardware;

// ------------------- HW - constituido de CPU e MEMORIA
// -----------------------------------------------
public class HW {
    public Memory mem;
    public CPU cpu;

    public HW(Memory mem, CPU cpu) {
        this.mem = mem;
        this.cpu = cpu;
    }
}