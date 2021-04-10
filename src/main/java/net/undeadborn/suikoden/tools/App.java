package net.undeadborn.suikoden.tools;

import net.undeadborn.suikoden.tools.services.GSD2Unpacker;

public class App {

    private static GSD2Unpacker gsd2Unpacker = new GSD2Unpacker();

    public static void main(String[] args) throws Exception {
        try {
            gsd2Unpacker.start("D:\\HACK\\gsd2.bin", "_extract");
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("Error found. Exiting program.");
        }
    }

}
