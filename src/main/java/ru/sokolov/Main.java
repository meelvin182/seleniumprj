package ru.sokolov;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static ru.sokolov.CoreKernelSupaClazz.APPDATA_PATH;

@Deprecated
public class Main {

    private static List<String> regions = loadRegions();

    public static void main(String[] args) throws Exception {
        System.out.println(CoreKernelSupaClazz.solveCapcha(new File(APPDATA_PATH + "\\images\\captcha.png")));
    }

    //CTRL CV TO TEST
    @SuppressWarnings("all")
    private static List<String> loadRegions() {
        List<String> values = new ArrayList<>();
        try {
            InputStream in = Main.class.getResourceAsStream("/regions.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            Scanner scanner = new Scanner(reader);       // create scanner to read
            while (scanner.hasNextLine()) {  // while there is a next line
                values.add(scanner.nextLine());
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return values;
    }
}
