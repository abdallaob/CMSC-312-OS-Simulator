import java.util.ArrayList;

public class PCB {
    private static int nextPID = 0;
    public int PID;
    public int state;
    public String name;
    public int program_counter;
    public String[] instructions;
    public int[] cycles;
    public int arrival;

    PCB(String name, ArrayList<String> instructions, ArrayList<Integer> cycles) {
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
    }
}