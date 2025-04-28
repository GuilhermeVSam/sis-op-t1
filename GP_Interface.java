public interface GP_Interface {
    int criaProcesso(String programName, Sistema.Word[] programa);

    void desalocaProcesso(int id);
}
