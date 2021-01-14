package xyz.raymark.retiproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TestBenchGenerator {


    public static void main(String[] args) throws Exception{
        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        List<Integer> original = imageGen(width, height);
        List<Integer> equalized = equalizeImage(original);
        System.out.println(System.getProperty("user.home") + "/TestBench.vhdl");
        writeOut(generateRAM(original, width, height), generateAssertion(equalized));
    }

    public static List<Integer> imageGen(int w, int h) {
        List<Integer> image = new ArrayList<>();
        for (int i = 0; i < h * w; i++) {
            image.add((int) ((Math.random() * 1000) % 256));
        }
        return image;
    }

    public static List<Integer> equalizeImage(List<Integer> image) {
        List<Integer> eqImage = new ArrayList<>();
        int max = image
                .stream()
                .mapToInt(v -> v)
                .max().orElseThrow(NoSuchElementException::new);
        int min = image
                .stream()
                .mapToInt(v -> v)
                .min().orElseThrow(NoSuchElementException::new);
        int floorlog = (int) Math.floor(Math.log(max - min + 1) / Math.log(2));
        for (int n : image) {
            int tmp_pix = (n - min) << (8 - floorlog);
            eqImage.add(Math.min(255, tmp_pix));
        }
        return eqImage;
    }

    public static String generateRAM(List<Integer> image, int w, int h){
        StringBuilder out = new StringBuilder("signal RAM: ram_type := (");
        out.append("0 => std_logic_vector(to_unsigned(  ").append(w).append("  , 8)),\n");
        out.append("1 => std_logic_vector(to_unsigned(  ").append(h).append("  , 8)),\n");
        int counter = 2;
        for (int p : image){
            out.append(counter).append(" => std_logic_vector(to_unsigned(  ").append(p).append("  , 8)),\n");
            counter++;
        }
        out.append("others => (others =>'0'));\n");
        return out.toString();
    }

    public static String generateAssertion(List<Integer> eqImage){
        int addr = eqImage.size()+2;
        StringBuilder out = new StringBuilder();
        for(int ignored : eqImage){
            out.append("report \"").append(addr).append(") \" & integer'image(to_integer(unsigned(RAM(").append(addr).append("))));\n");
            addr++;
        }
        addr = eqImage.size()+2;
        for(int pix : eqImage){
            out.append("assert RAM(").append(addr).append(") = std_logic_vector(to_unsigned( ").append(pix).append(" , 8)) report \"TEST FALLITO (WORKING ZONE). Expected  ").append(pix).append("  found \" & integer'image(to_integer(unsigned(RAM(").append(addr).append("))))  severity failure;\n");
            addr++;
        }
        return out.toString();
    }

    public static void writeOut(String ram, String assertions) throws IOException {
        StringBuilder output = new StringBuilder();
        InputStream is = TestBenchGenerator.class.getClassLoader().getResourceAsStream("head.vhdl");
        assert is != null;
        BufferedReader isr = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = isr.readLine()) != null) {
            System.out.println(line);
            output.append(line).append("\n");
        }
        isr.close();
        output.append(ram).append("\n");
        is = TestBenchGenerator.class.getClassLoader().getResourceAsStream("body.vhdl");
        assert is != null;
        isr = new BufferedReader(new InputStreamReader(is));
        while ((line = isr.readLine()) != null) {
            System.out.println(line);
            output.append(line).append("\n");
        }
        isr.close();
        output.append(assertions).append("\n");
        is = TestBenchGenerator.class.getClassLoader().getResourceAsStream("end.vhdl");
        assert is != null;
        isr = new BufferedReader(new InputStreamReader(is));
        while ((line = isr.readLine()) != null) {
            System.out.println(line + "\n");
            output.append(line);
        }
        isr.close();
        File f = new File(System.getProperty("user.home") + "/TestBench.vhd");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(output.toString());
        bw.close();
    }
}
