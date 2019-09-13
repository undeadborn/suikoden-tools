package undeadborn.suikoden.tools.services;

import org.apache.commons.lang3.ArrayUtils;
import undeadborn.suikoden.tools.model.BinFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class is used to unpack GSD files from the game
 * These GSD file contains game data and they are found under the following paths
 * -- Suikoden I  --> ISO://PSP_GAME/USRDIR/bin/gsd1.bin
 * -- Suikoden II --> ISO://PSP_GAME/USRDIR/bin/gsd2.bin
 */
public class GSDUnpacker {

    private final static String EXTRACT_SUFFIX = "_extract";
    private final static int GSD_NAME_RESERVATION = 56;
    private final static Charset GZIP_CHARSET = StandardCharsets.ISO_8859_1;
    private final static String GZIP_EXT = "gz";
    private final static byte[] GZ_SIGNATURE = {(byte) 0x1F, (byte) 0x8B, (byte) 0x08};

    /**
     * Trigger the process to unpack binaries
     *
     * @param input The path of the GSD file in your filesystem
     * @throws IOException
     */
    public void start(String input) throws IOException {
        List<BinFile> binFiles = new ArrayList<>();
        File fileInput = new File(input);

        String workingFolder = fileInput.getParent();
        String outputFolder = workingFolder + "\\" + fileInput.getName() + EXTRACT_SUFFIX;

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileInput.getAbsolutePath())))) {
            // these bytes are common in GSD files, but no idea what is about (could contain file signature? it starts with a GSD2 ...)
            dis.skipBytes(8);
            // no idea of what is this, could be the CRC of the entire file?
            dis.skipBytes(4);
            // these bytes are common in GSD files, but no idea what is about
            dis.skipBytes(4);
            // these bytes are common in GSD files, but no idea what is about
            dis.skipBytes(4);
            // these are the number of files that GSD file contains, they need to be read in reverse
            int totalFiles = ByteBuffer.wrap(getBytes(dis, 4, true)).getInt();
            // these bytes are common in GSD files, but no idea what is about
            dis.skipBytes(8);
            // seems header finishes here so, ignore all remaining zeros until we found the first byte
            skipZeros(dis);

            System.out.println(String.format("This BIN file contains %s files", totalFiles));
            System.out.println("--------------------------------------------------------------------");

            // after this seems we have the list of files that GSD contains
            // every set of byte contains information from the file (id, offset, length... etc)
            while (dis.available() > 0 && totalFiles > binFiles.size()) {
                // this is the ID or the order of the file
                int id = ByteBuffer.wrap(getBytes(dis, 4, true)).getInt();
                // this is the offset of the file (where it begins)
                int off = ByteBuffer.wrap(getBytes(dis, 4, true)).getInt();
                // this is the length of the file
                int len = ByteBuffer.wrap(getBytes(dis, 4, true)).getInt();
                // no idea of this
                dis.skipBytes(8);
                // these bytes are common in all files, but don't know what is about
                dis.skipBytes(4);
                // here the file name starts, which terminates when 0 is found
                // seems there are 56 bytes reserved for the file name
                byte currentByte;
                String name;
                try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()) {
                    while (dis.available() > 0 && ((currentByte = dis.readByte()) != 0)) {
                        byteArrayStream.write(currentByte);
                    }
                    name = new String(byteArrayStream.toByteArray(), GZIP_CHARSET);
                }
                // skip all remaining zeros from file name to start with the next one
                dis.skipBytes(GSD_NAME_RESERVATION - name.length() - 1);
                // we have already all data we need, so register the file info inside a list to process later
                BinFile file = new BinFile(name, id, off, len);
                binFiles.add(file);

                System.out.println(String.format("Found file #%s --> %s", String.format("%05d", file.getId()), file.toString()));
            }
        }

        // begin with the extract
        System.out.println("--------------------------------------------------------------------");
        System.out.println("Extracting files ...");

        // create output folder if doesn't exist
        File directory = new File(outputFolder);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // reopen file in RandomAccessFile to seek for offsets
        try (RandomAccessFile raf = new RandomAccessFile(fileInput.getAbsolutePath(), "r");) {
            for (int i = 0; i < binFiles.size(); i++) {
                System.out.println(String.format("Extracting file [%s] ...", binFiles.get(i).getName()));
                try {
                    // get the bytes from file using the offset and length
                    byte[] arrayBytes = new byte[binFiles.get(i).getLength()];
                    raf.seek(Integer.valueOf(binFiles.get(i).getOffset()).longValue());
                    raf.read(arrayBytes, 0, binFiles.get(i).getLength());
                    // get the file extension of the file using the file signature
                    String fileExtension = getFileExtension(arrayBytes);
                    // create the file
                    try (FileOutputStream fos = new FileOutputStream(outputFolder + "\\" + binFiles.get(i).getName() + "." + fileExtension)) {
                        fos.write(arrayBytes);
                    }
                } catch (Exception e) {
                    binFiles.get(i).getErrors().add(
                            String.format("Error while extracting file --> %s", e.getMessage())
                    );
                }
            }
        }

        // return any errors found
        if (binFiles.stream().anyMatch(file -> file.getErrors().size() > 0)) {
            System.err.println("\nErrors found in some files");
            binFiles.stream().filter(file -> file.getErrors().size() > 0).forEach(file -> {
                System.err.println("------------------------------------------------------------------");
                System.err.println(String.format("Errors found in file [%s]", file.getName()));
                file.getErrors().stream().forEach(System.err::println);
            });
        } else {
            System.out.println("\nAll files extracted successfully !!");
        }

        System.out.println("\nProgram finished");
    }

    /**
     * Get bytes from DataInputStream
     *
     * @param dis The DataInputStream
     * @param len The quantity of bytes
     * @return
     * @throws IOException
     */
    private byte[] getBytes(DataInputStream dis, int len) throws IOException {
        return getBytes(dis, len, false);
    }

    /**
     * Get bytes from DataInputStream
     *
     * @param dis     The DataInputStream
     * @param len     The quantity of bytes
     * @param reverse true to receive the bytes reversed
     * @return
     * @throws IOException
     */
    private byte[] getBytes(DataInputStream dis, int len, boolean reverse) throws IOException {
        byte[] arrayBytes = new byte[len];
        for (int i = 0; i < len; i++) {
            arrayBytes[i] = dis.readByte();
        }
        if (reverse) {
            ArrayUtils.reverse(arrayBytes);
        }
        return arrayBytes;
    }

    /**
     * Skip all remaining zeros from a DataInputStream
     *
     * @param dis The DataInputStream
     * @throws IOException
     */
    private void skipZeros(DataInputStream dis) throws IOException {
        do {
            dis.mark(1);
        } while (dis.available() > 0 && dis.readByte() == 0);
        dis.reset();
    }

    /**
     * Get the file extension using the file signature (the first bytes of the file)
     *
     * @param arrayBytes The array bytes of file
     * @return
     * @throws Exception
     */
    private String getFileExtension(byte[] arrayBytes) throws Exception {
        if (IntStream.range(0, GZ_SIGNATURE.length).allMatch(i -> arrayBytes[i] == GZ_SIGNATURE[i])) {
            return GZIP_EXT;
        }
        throw new Exception("No file format found for the specific file");
    }

}