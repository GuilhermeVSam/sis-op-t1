import java.util.Scanner;

public class UserInterface {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Sistema sistema = new Sistema(1024, 8);
        Programs programs = new Programs();

        while(true){
            clearScreen();
            System.out.printf("");
            String input = sc.nextLine();
            String[] command = input.split(" ");
            switch(command[0]){
                case "new" -> {
                    try {
                        if (command.length == 2) {
                            String programName = command[1];
                            Sistema.Word[] programa = programs.retrieveProgram(programName);
                            int procId = sistema.so.gp.criaProcesso(programa);
                            System.out.println(procId);
                        } else {
                            throw new IndexOutOfBoundsException();
                        }
                    } catch (IndexOutOfBoundsException ex){
                        System.err.println("Command Malformed: Try 'new <programName>'");
                    }
                }
                case "rm" -> {
                    int id = Integer.parseInt(command[1]);
                }
                case "ps" -> {
                    //Lista Processos
                }
                case "dump" -> {
                    int id = Integer.parseInt(command[1]);
                    // Puxa dados do programa
                    System.out.println(id);
                }
                case "dumpM" -> {
                    String[] inicioFim = command[1].split(",");
                    int inicio = Integer.parseInt(inicioFim[0]);
                    int fim = Integer.parseInt(inicioFim[1]);
                }
                case "exec" -> {
                    int id = Integer.parseInt(command[1]);

                }
                case "traceOn" -> {
                    //traceOn()
                }
                case "traceOff" -> {
                    //traceOff()
                }
                case "exit" -> {
                    return;
                }
                default -> {
                    System.err.println("Command not Found");
                }
            }
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
