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

    ProgramTemplate(String name, String filepath) {
        this.name = name;
        instructions = new ArrayList<>();
        min = new ArrayList<>();
        max = new ArrayList<>();

        try {

            BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
            String buffer;
            br.readLine();
            while ((buffer = br.readLine()) != null) {
                String[] tokens = getTokens(buffer);
                instructions.add(tokens[0]);
                min.add(Integer.parseInt(tokens[1]));
                max.add(Integer.parseInt(tokens[2]));
            }
        } catch (Exception e) {
            System.out.println("[!] Exception Occurred while reading process template file: " + filepath);
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public PCB get(){
        ArrayList<Integer> cycles = new ArrayList<>();
        for(int i=0; i<instructions.size(); i++)
            cycles.add( new Random().nextInt(max.get(i) - min.get(i)) + min.get(i));
        return new PCB(name, instructions, cycles);
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
