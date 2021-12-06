import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class app extends Thread {
    static final int THREADCOUNT = 2;
    static int cycles = 0; // number of elapsed cycles
    static int refresh_rate = 100;
    static int quantum = 5; // round robin will switch process after quantum number of cycles
    static int total_processes = 20; // number of process that will be spawned by default
    static boolean isRunning = true; // state of the os, will turn false when all processes enter exit list
    static ArrayList<ProgramTemplate> generators;
    static ArrayDeque<PCB> new_queue;
    static ArrayDeque<PCB> ready_queue;
    static ArrayList<PCB> wait_queue;
    static ArrayList<PCB> exit_list;
    static PCB[] running = new PCB[THREADCOUNT];
    static int critInUse;
    static int interrupt_counter = 0;
    static final int TOTAL_MEM = 1024;
    static int USED_MEM = 0;
    static String SCHEME = "SHORTEST"; // "SHORTEST" OR "ROUNDROBIN" or "PRIORITY"
    static ProgramTemplate childTemplate;
    static ProgramTemplate InterruptTemplate;

    /*
     * Process Life Cycle 0 - New 1 - Ready 2 - Executing / Run 3 - Wait / Blocked
     * 4- Exit
     */

    private static void printDash(String dash) {
        for (int i = 0; i < 120; i++)
            System.out.print(dash);
        System.out.println();
    }

    public static PCB RQNext() {
        switch (SCHEME) {
            case "PRIORITY":
                return ready_queue.stream().max(Comparator.comparing(PCB::getPriority)).orElse(null);

            case "SHORTEST":
                return ready_queue.stream().min(Comparator.comparing(PCB::getTotalCycles)).orElse(null);

            case "ROUNDROBIN":
            default:
                return ready_queue.pop();
        }
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

            if (i < running.length && running[i] != null) {
                String s = new String(" " + Integer.toString(running[i].PID) + ": " + running[i].name);
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
        System.out.print("Process in Critical Section: ");
        if (critInUse > -1) {
            System.out.print(critInUse);
        } else
            System.out.print("None");
        System.out.println(
                "\t\t\tInterrupts count: " + interrupt_counter + "\t\t\tMemory Used: " + USED_MEM + " / " + TOTAL_MEM);

    }

    public static void main(String[] args) {

        System.out.println("\t\t\tStarting OS Simulator");
        generators = new ArrayList<>();
        new_queue = new ArrayDeque<>();
        ready_queue = new ArrayDeque<>();
        wait_queue = new ArrayList<>();
        exit_list = new ArrayList<>();

        for (int i = 0; i < THREADCOUNT; i++)
            running[i] = null;
        critInUse = -1;

        ProgramTemplate driverTemplate = new ProgramTemplate("driver",
                "./templates/driver.txt");
        generators.add(driverTemplate);
        System.out.println("[!] Loaded Driver Process Template");

        ProgramTemplate processTemplate = new ProgramTemplate("generic process",
                "./templates/process.txt");
        generators.add(processTemplate);
        System.out.println("[!] Loaded Generic Process Template");

        childTemplate = new ProgramTemplate("child process",
                "./templates/child.txt");
        System.out.println("[!] Loaded child process Template");

        InterruptTemplate = new ProgramTemplate("interrupt handler",
                "./templates/interrupt.txt");
        System.out.println("[!] Loaded interrupt Template");

        System.out.println("[!] Creating " + total_processes + " random processes.");
        for (int i = 0; i < total_processes; i++) {
            int rnd = new Random().nextInt(generators.size());
            PCB toAdd = generators.get(rnd).get();
            toAdd.arrival = i;
            new_queue.add(toAdd);
        }

        app[] THREADS = new app[THREADCOUNT];
        for (int i = 0; i < THREADCOUNT; i++) {
            THREADS[i] = new app();
            THREADS[i].setName(Integer.toString(i));
        }

        for (int i = 0; i < THREADCOUNT; i++)
            THREADS[i].start();
        int main_cycles = 0;
        while (isRunning) {
            main_cycles += 1;
            if (main_cycles % refresh_rate == 0)
                display();

            if (exit_list.size() == total_processes)
                isRunning = false;
            // try{Thread.sleep(700);}
            // catch(Exception e){continue;}
        }
        System.out.println("[!] OS-SIM finished running. Terminating.");

    }

    @Override
    public void run() {
        int threadID = Integer.parseInt(Thread.currentThread().getName());
        while (isRunning) {
            try {
                cycles += 1;

                // Randomly occurring IO Interrupt: 5% chance
                if (running[threadID] != null && new Random().nextFloat() <= 0.01f) {
                    // execute an interrupt routine
                    interrupt_counter++;
                    System.out.println("External Interrupt Occurred.");
                    running[threadID].state = 1;
                    ready_queue.add(running[threadID]);
                    running[threadID] = InterruptTemplate.get();
                    if (running[threadID] != null)
                        running[threadID].state = 2;
                    continue;
                }

                // check if any process's arrival time is now, then add
                if (!new_queue.isEmpty()) {
                    new_queue.stream().filter(x -> {
                        return x.arrival <= cycles;
                    }).forEach(x -> {
                        if (x.memory <= TOTAL_MEM - USED_MEM) {
                            x.state = 1;
                            ready_queue.add(x);
                            new_queue.remove(x);
                            USED_MEM += x.memory;
                        }
                    });
                }

                // poll waiting/blocked processes and move them to ready once they're done
                // waiting
                for (int i = 0; i < wait_queue.size();) {
                    PCB curr = wait_queue.get(i);
                    if (curr.state != 1) {
                        int instruction = curr.program_counter;
                        curr.cycles[instruction]--;
                        if (curr.cycles[instruction] <= 0) {
                            curr.program_counter++;
                            curr.state = 1;
                            if (curr.memory <= TOTAL_MEM - USED_MEM) {
                                ready_queue.add(curr);
                                USED_MEM += curr.memory;
                                wait_queue.remove(i);
                                continue;
                            }
                        }
                    } else if (curr.memory <= TOTAL_MEM - USED_MEM) {
                        ready_queue.add(curr);
                        USED_MEM += curr.memory;
                        wait_queue.remove(i);
                    }
                    i++;
                }

                if (running[threadID] == null) {
                    running[threadID] = ready_queue.isEmpty() ? null : RQNext();
                    if (running[threadID] != null)
                        running[threadID].state = 2;

                } else {
                    if (running[threadID].program_counter < running[threadID].instructions.length) {

                        // check for critical section and defer execution if critical section in use
                        if (running[threadID].program_counter >= running[threadID].critStart
                                && running[threadID].program_counter <= running[threadID].critEnd) {
                            if (critInUse == -1)
                                critInUse = running[threadID].PID;

                            else if (critInUse != running[threadID].PID) {
                                // block process by moving it to the back of ready queue
                                running[threadID].state = 1;
                                ready_queue.add(running[threadID]);
                                running[threadID] = ready_queue.isEmpty() ? null : RQNext();
                                if (running[threadID] != null)
                                    running[threadID].state = 2;
                                continue;
                            } else {
                                // do nothing if current process is in critical section
                            }
                        }

                        if (critInUse == running[threadID].PID
                                && running[threadID].program_counter >= running[threadID].critEnd)
                            critInUse = -1;

                        // resume execution
                        switch (running[threadID].instructions[running[threadID].program_counter]) {
                            case "CALCULATE":
                                running[threadID].cycles[running[threadID].program_counter]--;
                                if (running[threadID].cycles[running[threadID].program_counter] <= 0)
                                    running[threadID].program_counter++;
                                break;

                            case "I/O":
                                running[threadID].state = 3;
                                wait_queue.add(running[threadID]);
                                USED_MEM -= running[threadID].memory;
                                running[threadID] = ready_queue.isEmpty() ? null : RQNext();
                                if (running[threadID] != null)
                                    running[threadID].state = 2;
                                continue;

                            case "FORK":
                                PCB toAdd = childTemplate.get();
                                toAdd.arrival = cycles += 10;
                                toAdd.parentPID = running[threadID].PID;
                                new_queue.add(toAdd);
                                System.out.println(running[threadID].PID + " spawned a child process.");
                                break;

                            default:
                        }
                    } else {
                        // move the process to exit list
                        running[threadID].state = 4;
                        exit_list.add(running[threadID]);
                        USED_MEM -= running[threadID].memory;
                        running[threadID] = ready_queue.isEmpty() ? null : RQNext();
                        if (running[threadID] != null)
                            running[threadID].state = 2;
                        continue;
                    }
                }

                if (cycles % quantum == 0 && running != null) {
                    running[threadID].state = 1;
                    ready_queue.add(running[threadID]);
                    running[threadID] = ready_queue.isEmpty() ? null : RQNext();
                    if (running[threadID] != null)
                        running[threadID].state = 2;

                }

                Thread.sleep(300);

            } catch (Exception e) {
                continue;
            }
        }
    }
}
