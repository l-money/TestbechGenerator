package xyz.raymark.retiproject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TestBenchGenerator {

    static File out = new File("testbench.vhd");

    public static void main(String[] args) {
        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        List<Integer> original = imageGen(width, height);
        List<Integer> equalized = equalizeImage(original);
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

    public static void writeOut(List<Integer> ram) {
        try {
            FileWriter fw = new FileWriter(out);
            //fw.write()
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
