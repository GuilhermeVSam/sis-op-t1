import java.util.HashMap;
import java.util.Map;

public class GM {
    private static GM instance;

    private boolean[] frames;
    private int pageSize;
    private int memSize;
    private Map<Integer, Map<Integer, Integer>> pageTable; // processo -> (pagina logica -> frame fisico)

    private GM(int tamMem, int tamPg) {
        this.memSize = tamMem;
        this.pageSize = tamPg;
        this.frames = new boolean[tamMem / tamPg];
        this.pageTable = new HashMap<>();
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

    public int[] malloc(int tamProcesso) {
        int numPaginas = (int) Math.ceil((double) tamProcesso / pageSize);
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

    public void registerPageTable(int idProcesso, int[] paginas) {
        Map<Integer, Integer> tabela = new HashMap<>();
        for (int paginaLogica = 0; paginaLogica < paginas.length; paginaLogica++) {
            tabela.put(paginaLogica, paginas[paginaLogica]);
        }
        pageTable.put(idProcesso, tabela);
    }

    public void deallocate(int[] paginas) {
        for (int pagina : paginas) {
            frames[pagina] = false;
        }
        // Remove as entradas da tabela de páginas
        for (Map<Integer, Integer> tabela : pageTable.values()) {
            for (Integer paginaLogica : tabela.keySet()) {
                if (tabela.get(paginaLogica) == paginas[0]) { // Verifica se o frame está na tabela de páginas
                    tabela.remove(paginaLogica);
                    break;
                }
            }
        }
    }

    public int translate(int idProcesso, int enderecoLogico) {
        int paginaLogica = enderecoLogico / pageSize;
        int deslocamento = enderecoLogico % pageSize;

        Map<Integer, Integer> tabela = pageTable.get(idProcesso);
        if (tabela == null || !tabela.containsKey(paginaLogica)) {
            throw new IllegalStateException("Endereço inválido ou processo não encontrado.");
        }

        int frameFisico = tabela.get(paginaLogica);
        return frameFisico * pageSize + deslocamento;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getMemSize() {
        return memSize;
    }
}
