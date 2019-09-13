package undeadborn.suikoden.tools;

import undeadborn.suikoden.tools.services.GSDUnpacker;

public class App {

    private static GSDUnpacker gsdUnpacker = new GSDUnpacker();

    public static void main(String[] args) throws Exception {
        gsdUnpacker.start("C:\\Users\\udb66\\Desktop\\SuikodenPSP\\hack\\gsd1.bin");
    }

}
