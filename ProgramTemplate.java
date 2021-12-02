import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public class ProgramTemplate {
    ArrayList<String> instructions;
    ArrayList<Integer> min;
    ArrayList<Integer> max;
    String name;
    int critStart;
    int critEnd;

    ProgramTemplate(String name, String filepath) {
        this.name = name;
        instructions = new ArrayList<>();
        min = new ArrayList<>();
        max = new ArrayList<>();
        critStart = critEnd = -1;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
            String buffer;
            br.readLine();
            int line = 0;
            while ((buffer = br.readLine()) != null) {
                String[] tokens = getTokens(buffer);
                instructions.add(tokens[0]);
                min.add(Integer.parseInt(tokens[1]));
                max.add(Integer.parseInt(tokens[2]));
                if (tokens.length == 4) {
                    // System.out.println(tokens[3]);
                    if (tokens[3].equalsIgnoreCase("start"))
                        critStart = line;
                    else if (tokens[3].equalsIgnoreCase("end"))
                        critEnd = line;
                }
                line++;
            }
        } catch (Exception e) {
            System.out.println("[!] Exception Occurred while reading process template file: " + filepath);
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public PCB get() {
        ArrayList<Integer> cycles = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++)
            cycles.add(new Random().nextInt(max.get(i) - min.get(i)) + min.get(i));
        int mem = new Random().nextInt(100 - 1) + 1;
        return new PCB(name, instructions, cycles, critStart, critEnd, mem);
    }

    private String[] getTokens(String str) {
        StringTokenizer st = new StringTokenizer(str);
        String[] arr = new String[st.countTokens()];
        int count = 0;

        while (st.hasMoreTokens())
            arr[count++] = st.nextToken();

        return arr;
    }
}
