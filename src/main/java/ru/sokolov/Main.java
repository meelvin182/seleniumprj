package ru.sokolov;


import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import ru.sokolov.model.entities.RequestEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.sokolov.CoreKernelSupaClazz.TEST_CADASTRE_NUM;
import static ru.sokolov.CoreKernelSupaClazz.TEST_KEY;

@Deprecated
public class Main {

    private static List<String> regions = loadRegions();

    public static void main(String[] args) throws Exception {
        System.out.println(format("%08d", 123456789));
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
