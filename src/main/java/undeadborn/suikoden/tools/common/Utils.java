package undeadborn.suikoden.tools.common;

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInputStream;
import java.io.IOException;

public final class Utils {

    /**
     * Get bytes from DataInputStream
     *
     * @param dis The DataInputStream
     * @param len The quantity of bytes to get
     * @return The resulting set of bytes
     * @throws IOException
     */
    public final static byte[] getBytes(DataInputStream dis, int len) throws IOException {
        return getBytes(dis, len, false);
    }

    /**
     * Get bytes from DataInputStream
     *
     * @param dis     The DataInputStream
     * @param len     The quantity of bytes
     * @param reverse true to receive bytes reversed
     * @return The resulting set of bytes
     * @throws IOException
     */
    public final static byte[] getBytes(DataInputStream dis, int len, boolean reverse) throws IOException {
        byte[] arrayBytes = new byte[len];
        for (int i = 0; i < len; i++) {
            arrayBytes[i] = dis.readByte();
        }
        if (reverse) {
            ArrayUtils.reverse(arrayBytes);
        }
        return arrayBytes;
    }

}
