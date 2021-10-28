import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class app {

    static int cycles = 0; // number of elapsed cycles
    static int refresh_rate = 10;
    static int quantum = 5; // round robin will switch process after quantum number of cycles
    static int total_processes = 20; // number of process that will be spawned by default
    static boolean isRunning = true; // state of the os, will turn false when all processes enter exit list
    static ArrayList<ProgramTemplate> generators;
    static ArrayDeque<PCB> new_queue;
    static ArrayDeque<PCB> ready_queue;
    static ArrayList<PCB> wait_queue;
    static ArrayList<PCB> exit_list;
    static PCB running;

    /*
     * Process Life Cycle 0 - New 1 - Ready 2 - Executing / Run 3 - Wait / Blocked
     * 4- Exit
     */

    private static void printDash(String dash) {
        for (int i = 0; i < 120; i++)
            System.out.print(dash);
        System.out.println();
    }

    private static void display() {
        // 24 characters per column 24 = | 20 |
        // clear the console.
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("\t\t\t\t\t\t\t OS-SIM Running");
        printDash("-");
        System.out.println(
                "|        new        |       ready       |       running      |       waiting      |        exited      |  CYCLES: "
                        + cycles + "   |");
        printDash("-");

        for (int i = 0; i < total_processes; i++) {
            StringBuilder sb = new StringBuilder();
            if (i < new_queue.size()) {
                PCB curr = (PCB) new_queue.toArray()[i];
                String s = new String("| " + Integer.toString(curr.PID) + ": " + curr.name);
                sb.append(s);
                for (int j = 0; j < 20 - s.length(); j++)
                    sb.append(" ");
                sb.append("|");
            } else
                sb.append("|                   |");

            if (i < ready_queue.size()) {
                PCB curr = (PCB) ready_queue.toArray()[i];
                String s = new String(" " + Integer.toString(curr.PID) + ": " + curr.name);
                sb.append(s);
                for (int j = 0; j < 20 - s.length(); j++)
                    sb.append(" ");
                sb.append("|");
            } else
                sb.append("                    |");

            if (running != null && i == 0) {
                String s = new String(" " + Integer.toString(running.PID) + ": " + running.name);
                sb.append(s);
                for (int j = 0; j < 20 - s.length(); j++)
                    sb.append(" ");
                sb.append("|");
            } else
                sb.append("                    |");

            if (i < wait_queue.size()) {
                PCB curr = wait_queue.get(i);
                String s = new String(" " + Integer.toString(curr.PID) + ": " + curr.name);
                sb.append(s);
                for (int j = 0; j < 20 - s.length(); j++)
                    sb.append(" ");
                sb.append("|");
            } else
                sb.append("                    |");

            if (i < exit_list.size()) {
                PCB curr = exit_list.get(i);
                String s = new String(" " + Integer.toString(curr.PID) + ": " + curr.name);
                sb.append(s);
                for (int j = 0; j < 20 - s.length(); j++)
                    sb.append(" ");
                sb.append("|");
            } else
                sb.append("                    |");

            System.out.println(sb.toString());
        }
        printDash("-");

    }

    public static void main(String[] args) {

        System.out.println("\t\t\tStarting OS Simulator");
        generators = new ArrayList<>();
        new_queue = new ArrayDeque<>();
        ready_queue = new ArrayDeque<>();
        wait_queue = new ArrayList<>();
        exit_list = new ArrayList<>();
        running = null;

        ProgramTemplate driverTemplate = new ProgramTemplate("driver", "./templates/driver.txt");
        generators.add(driverTemplate);
        System.out.println("[!] Loaded Driver Process Template");

        ProgramTemplate processTemplate = new ProgramTemplate("generic process", "./templates/process.txt");
        generators.add(processTemplate);
        System.out.println("[!] Loaded Generic Process Template");

        System.out.println("[!] Creating " + total_processes + " random processes.");
        for (int i = 0; i < total_processes; i++) {
            int rnd = new Random().nextInt(generators.size());
            PCB toAdd = generators.get(rnd).get();
            toAdd.arrival = i;
            new_queue.add(toAdd);
        }


        while (isRunning) {
            cycles += 1;

            if(cycles % refresh_rate == 0)
                display();

            // check if any process's arrival time is now, then add
            if (!new_queue.isEmpty()) {
                new_queue.stream().filter(x -> {
                    return x.arrival <= cycles;
                }).forEach(x -> {
                    x.state = 1;
                    ready_queue.add(x);
                    new_queue.remove(x);
                });
            }

            // poll waiting/blocked processes and move them to ready once they're done
            // waiting
            for (int i = 0; i < wait_queue.size();) {
                PCB curr = wait_queue.get(i);
                int instruction = curr.program_counter;
                curr.cycles[instruction]--;
                if (curr.cycles[instruction] <= 0) {
                    curr.program_counter++;
                    curr.state = 1;
                    ready_queue.add(curr);
                    wait_queue.remove(i);
                    continue;
                }
                i++;
            }

            if (running == null) {
                running = ready_queue.isEmpty() ? null : ready_queue.pop();
                if (running != null)
                    running.state = 2;

            } else {

                if (running.program_counter < running.instructions.length) {
                    switch (running.instructions[running.program_counter]) {
                    case "CALCULATE":
                        running.cycles[running.program_counter]--;
                        if (running.cycles[running.program_counter] <= 0)
                            running.program_counter++;
                        break;

                    case "I/O":
                        running.state = 3;
                        wait_queue.add(running);
                        running = ready_queue.isEmpty() ? null : ready_queue.pop();
                        if (running != null)
                            running.state = 2;
                        continue;

                    case "FORK":
                        break;

                    default:
                    }
                } else {
                    // move the process to
                    running.state = 4;
                    exit_list.add(running);
                    running = ready_queue.isEmpty() ? null : ready_queue.pop();
                    if (running != null)
                        running.state = 2;
                    continue;
                }
            }

            if (cycles % quantum == 0 && running != null) {
                running.state = 1;
                ready_queue.add(running);
                running = ready_queue.isEmpty() ? null : ready_queue.pop();
                if (running != null)
                    running.state = 2;

            }

            if (exit_list.size() == total_processes)
                isRunning = false;

            display();
            try{
                Thread.sleep(500);
            }
            catch(InterruptedException ex){
                continue;
            }
        }
        System.out.println("[!] OS-SIM finished running. Terminating.");

    }
}
