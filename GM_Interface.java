public interface GM_Interface {

    int[] aloca(int nroPalavras);
    // retorna true se consegue alocar ou falso caso negativo
    // cada posição i do vetor de saída “tabelaPaginas” informa em que frame a
    // página i deve ser hospedada

    void desaloca(int[] tabelaPaginas);
    // desaloca as páginas que estão alocadas nos frames indicados no vetor de
    // entrada “tabelaPaginas”
    // o vetor de entrada “tabelaPaginas” tem o mesmo tamanho do vetor de saída da
    // função aloca
}