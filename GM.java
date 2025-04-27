import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GM implements GM_Interface {
    private int tamMem;
    private int tamPg;
    private boolean[] pageTable;

    public GM(int tamMem, int tamPg) {
        this.tamMem = tamMem;
        this.tamPg = tamPg;
        this.pageTable = new boolean[tamMem / tamPg];
    }

    public int getTamMem() {
        return tamMem;
    }

    public void setTamMem(int tamMem) {
        this.tamMem = tamMem;
    }

    public int getTamPg() {
        return tamPg;
    }

    public void setTamPg(int tamPg) {
        this.tamPg = tamPg;
    }

    public boolean[] getPageTable() {
        return pageTable;
    }

    public void setPageTable(boolean[] pageTable) {
        this.pageTable = pageTable;
    }

    @Override
    public int[] aloca(int nroPalavras) {
        int partes = nroPalavras / tamPg;
        if (nroPalavras % tamPg != 0) {
            partes++;
        }
        if (partes > pageTable.length) {
            return null;
        }

        int[] framesLivres = getFramesLivres();

        if (partes > framesLivres.length) {
            return null;
        }

        int[] tabelaPaginas = new int[partes];
        for (int i = 0; i < partes; i++) {
            pageTable[framesLivres[i]] = true;
            tabelaPaginas[i] = framesLivres[i];
        }

        return tabelaPaginas;
    }

    @Override
    public void desaloca(int[] tabelaPaginas) {
        for (int tabelaPagina : tabelaPaginas) {
            pageTable[tabelaPagina] = false;
        }
    }

    private int[] getFramesLivres() {
        int[] framesLivres = new int[pageTable.length];
        int count = 0;
        for (int i = 0; i < pageTable.length; i++) {
            if (!pageTable[i]) {
                framesLivres[count] = i;
                count++;
            }
        }
        return framesLivres;
    }
}
