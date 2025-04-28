import java.util.Scanner;

public class UserInterface {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Sistema sistema = new Sistema(1024, 8);
        Programs programs = new Programs();
        Scheduler scheduler = new Scheduler(sistema);

        while (true) {
            String input = sc.nextLine();
            String[] command = input.split(" ");
            switch (command[0]) {
                case "new" -> {
                    try {
                        if (command.length == 2) {
                            String programName = command[1];
                            Sistema.Word[] programa = programs.retrieveProgram(programName);
                            System.out.println(programa.length);
                            int procId = sistema.so.gp.criaProcesso(programName, programa);
                            System.out.println("Program " + programName + " created");
                            System.out.println(procId);
                        } else {
                            throw new IndexOutOfBoundsException();
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        System.err.println("Command Malformed: Try 'new <programName>'");
                    }
                }
                case "rm" -> {
                    int id = Integer.parseInt(command[1]);
                    sistema.so.gp.kill(id);
                }
                case "ps" -> {
                    System.out.println(sistema.so.gp.listProcess());
                }
                case "dump" -> {
                    int id = Integer.parseInt(command[1]);
                    sistema.so.gp.dumpID(id);
                }
                case "dumpM" -> {
                    String[] inicioFim = command[1].split(",");
                    int inicio = Integer.parseInt(inicioFim[0]);
                    int fim = Integer.parseInt(inicioFim[1]);
                }
                case "exec" -> {
                    if (command.length > 1) {
                        int id = Integer.parseInt(command[1]);
                    } else {
                        scheduler.execAll();
                    }
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
}
