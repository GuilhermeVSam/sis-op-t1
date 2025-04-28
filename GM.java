import java.util.HashMap;
import java.util.Map;

public class GM implements GM_Interface {
    private static GM instance;

    private boolean[] frames;
    private int tamPg;
    private int tamMem;
    private Map<Integer, Map<Integer, Integer>> tabelasDePaginas; // processo -> (pagina logica -> frame fisico)

    private GM(int tamMem, int tamPg) {
        this.tamMem = tamMem;
        this.tamPg = tamPg;
        this.frames = new boolean[tamMem / tamPg];
        this.tabelasDePaginas = new HashMap<>();
    }

    public static GM getInstance(int tamMem, int tamPg) {
        if (instance == null) {
            instance = new GM(tamMem, tamPg);
        }
        return instance;
    }

    public static GM getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GM ainda não foi inicializado! Use getInstance(tamMem, tamPg) primeiro.");
        }
        return instance;
    }

    @Override
    public int[] aloca(int tamProcesso) {
        int numPaginas = (int) Math.ceil((double) tamProcesso / tamPg);
        int[] paginas = new int[numPaginas];
        int encontrado = 0;

        for (int i = 0; i < frames.length && encontrado < numPaginas; i++) {
            if (!frames[i]) {
                frames[i] = true;
                paginas[encontrado++] = i;
            }
        }

        if (encontrado < numPaginas) {
            for (int i = 0; i < encontrado; i++) {
                frames[paginas[i]] = false;
            }
            return null;
        }

        return paginas;
    }

    public void registraTabelaPaginas(int idProcesso, int[] paginas) {
        Map<Integer, Integer> tabela = new HashMap<>();
        for (int paginaLogica = 0; paginaLogica < paginas.length; paginaLogica++) {
            tabela.put(paginaLogica, paginas[paginaLogica]);
        }
        tabelasDePaginas.put(idProcesso, tabela);
    }

    @Override
    public void desaloca(int[] paginas) {
        for (int pagina : paginas) {
            frames[pagina] = false;
        }
    }

    public int traduzir(int idProcesso, int enderecoLogico) {
        int paginaLogica = enderecoLogico / tamPg;
        int deslocamento = enderecoLogico % tamPg;

        Map<Integer, Integer> tabela = tabelasDePaginas.get(idProcesso);
        if (tabela == null || !tabela.containsKey(paginaLogica)) {
            throw new IllegalStateException("Endereço inválido ou processo não encontrado.");
        }

        int frameFisico = tabela.get(paginaLogica);
        return frameFisico * tamPg + deslocamento;
    }

    public int getTamPg() {
        return tamPg;
    }

    public int getTamMem() {
        return tamMem;
    }
}
