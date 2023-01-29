package at.mlem.talkingenemies.zomboid;

import org.joml.Random;

import java.util.Scanner;

public class ColorParser {


    private static Random random = new Random();

    public static Color parseFromHex(String color) {
        if(color == null || !color.startsWith("#")) {
            return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        }
        long rLong = Long.parseLong(color.substring(1, 3), 16);
        long gLong = Long.parseLong(color.substring(3, 5), 16);
        long bLong = Long.parseLong(color.substring(5, 7), 16);
        float divisor = 255;
        return new Color((float)rLong/ divisor, (float)gLong/ divisor, (float)bLong/ divisor);
    }
}
