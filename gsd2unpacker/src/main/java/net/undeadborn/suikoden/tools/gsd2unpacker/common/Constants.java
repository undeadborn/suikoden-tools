package net.undeadborn.suikoden.tools.gsd2unpacker.common;

public final class Constants {

    public final static class GZIP {
        public final static byte[] SIGNATURE = {(byte) 0x1F, (byte) 0x8B, (byte) 0x08};
        public final static String EXTENSION = "gz";
    }

    public final static class GSD2 {
        public final static byte[] SIGNATURE = {(byte) 0x47, (byte) 0x53, (byte) 0x44, (byte) 0x32};
    }

}
