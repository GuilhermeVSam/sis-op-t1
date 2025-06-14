package Sistema.SistemaOperacional;

import Sistema.Hardware.HW;

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