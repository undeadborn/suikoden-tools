package undeadborn.suikoden.tools.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Constants {

    public final static class GZIP {
        public final static String EXTENSION = "gz";
        public final static Charset CHARSET = StandardCharsets.ISO_8859_1;
        public final static byte[] SIGNATURE = {(byte) 0x1F, (byte) 0x8B, (byte) 0x08};
    }

}
