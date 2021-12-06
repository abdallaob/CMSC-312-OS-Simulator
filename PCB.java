import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
public class PCB {
    private static int nextPID = 0;
    public int PID;
    public int state;
    public String name;
    public int program_counter;
    public String[] instructions;
    public int[] cycles;
    public int arrival;
    public int critStart;
    public int critEnd;
    public int parentPID;
    public int memory;
    public int total_cycles;
    public int priority;

    PCB(String name, ArrayList<String> instructions, ArrayList<Integer> cycles, int critStart, int critEnd, int mem) {
        PID = nextPID++;
        state = 0;
        program_counter = 0;

        this.name = name;

        this.instructions = new String[instructions.size()];
        for (int i = 0; i < instructions.size(); i++)
            this.instructions[i] = instructions.get(i);

        this.cycles = new int[cycles.size()];
        for (int i = 0; i < cycles.size(); i++)
            this.cycles[i] = cycles.get(i);

        this.critStart = critStart;
        this.critEnd = critEnd;
        this.memory = mem;
        this.total_cycles = getTotalCycles();
        this.priority = (new Random()).nextInt(5 - 1) + 1;
    }

    int getPriority(){
        return priority;
    }

    int getTotalCycles(){
        return Arrays.stream(cycles).sum();
    }
}
