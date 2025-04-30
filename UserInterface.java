import java.util.Scanner;

public class UserInterface {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Sistema sistema = new Sistema(1024, 8);
        Programs programs = new Programs();
        Scheduler scheduler = new Scheduler(sistema);
        try{
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
                                int procId = sistema.so.gp.createProcess(programName, programa);
                                System.out.println("Program " + programName + " created");
                                System.out.println(procId);
                            } else {
                                throw new IndexOutOfBoundsException();
                            }
                        } catch (IndexOutOfBoundsException ex) {
                            System.err.println("Command Malformed: Try 'new <programName>'");
                        } catch (NullPointerException ex) {
                            System.err.println("Program not found");
                        } catch (Exception ex) {
                            System.err.println("Error: " + ex.getMessage());
                        }
                    }
                    case "rm" -> {
                        int id = Integer.parseInt(command[1]);
                        sistema.so.gp.kill(id);
                    }
                    case "ps" -> System.out.println(sistema.so.gp.listProcess());
                    case "dump" -> {
                        try{
                            if (command.length < 2) {
                                System.err.println("Error: ID not provided. Usage: dump <id>");
                                break;
                            }
                            int id = Integer.parseInt(command[1]);
                            sistema.so.gp.dumpID(id);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid ID format. Usage: dump <id>");
                        } catch (Exception e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                    }
                    case "dumpM" -> {
                        String[] startFinish = command[1].split(",");
                        int start = Integer.parseInt(startFinish[0]);
                        int finish = Integer.parseInt(startFinish[1]);
                        sistema.so.utils.dump(start, finish);
                    }
                    case "exec" -> {
                        if (command.length > 1) {
                            int id = Integer.parseInt(command[1]);
                            scheduler.exec(id);
                        } else {
                            scheduler.execAll();
                        }
                    }
                    case "traceOn" -> sistema.hw.cpu.setDebug(true);
                    case "traceOff" -> sistema.hw.cpu.setDebug(false);
                    case "fill" -> {
                        int id = Integer.parseInt(command[1]);
                        sistema.so.gm.fill(id);
                    }
                    case "exit" -> {
                        return;
                    }
                    case "help" -> {
                        System.out.println("Available commands:");
                        System.out.println("new <programName> - Create a new process with the specified program");
                        System.out.println("rm <id> - Remove a process with the specified ID");
                        System.out.println("ps - List all processes");
                        System.out.println("dump <id> - Dump memory of the process with the specified ID");
                        System.out.println("dumpM <start,end> - Dump memory from start to end");
                        System.out.println("exec - Execute all processes");
                        System.out.println("exec <id> - Execute a specific process or all processes if no ID is provided");
                        System.out.println("traceOn - Enable tracing");
                        System.out.println("traceOff - Disable tracing");
                        System.out.println("exit - Exit the program");
                    }
                    default -> System.err.println("Command not Found");
                }
            }
        } catch (Exception e) {
            System.out.println("Command Malformed");
        }
    }
}
