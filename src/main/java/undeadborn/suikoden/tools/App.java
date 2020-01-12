package undeadborn.suikoden.tools;

import undeadborn.suikoden.tools.services.GSD2Unpacker;

public class App {

    private static GSD2Unpacker gsd2Unpacker = new GSD2Unpacker();

    public static void main(String[] args) throws Exception {
        gsd2Unpacker.start("C:\\Users\\udb66\\Desktop\\SuikodenPSP\\hack\\gsd1.bin", "_extract");
    }

}
