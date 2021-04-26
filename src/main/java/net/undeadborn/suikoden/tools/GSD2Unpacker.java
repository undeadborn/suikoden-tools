package net.undeadborn.suikoden.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteSource;
import com.google.common.io.LittleEndianDataInputStream;
import net.undeadborn.suikoden.tools.model.BinFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

/**
 * This class is used to unpack GSD2 files from the game
 * These GSD2 file contains game data and they are found under the following paths
 * -- Suikoden I  --> ISO://PSP_GAME/USRDIR/bin/gsd1.bin
 * -- Suikoden II --> ISO://PSP_GAME/USRDIR/bin/gsd2.bin
 */
public class GSD2Unpacker {

    private final static byte[] MAGIC_NUMBER_GZIP = {(byte) 0x1F, (byte) 0x8B, (byte) 0x08};
    private final static byte[] MAGIC_NUMBER_GSD2 = {(byte) 0x47, (byte) 0x53, (byte) 0x44, (byte) 0x32};

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Must provide a GSD2 file path as argument. Exiting program.");
            System.exit(1);
        }

        if (!new File(args[0]).exists() || !new File(args[0]).isFile()) {
            System.err.println(String.format("File [%s] is not a file or does not exist.", args[0]));
            System.exit(1);
        }

        try {
            new GSD2Unpacker().start(args[0]);
        } catch (Exception e) {
            System.err.println("------------------------------------------------------------------");
            System.err.println(e);
            System.err.println("\nErrors found. Exiting program.");
            System.exit(1);
        }
    }

    /**
     * Trigger the process to unpack binaries
     *
     * @param input The path of the GSD2 file in your filesystem
     * @throws IOException error
     */
    private void start(String input) throws Exception {
        List<BinFile> binFiles = new ArrayList<>();
        File fileInput = new File(input);

        String workingFolder = fileInput.getParent();
        String outputFolder = workingFolder + File.separator + fileInput.getName() + "_extract";

        try (LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new BufferedInputStream(new FileInputStream(fileInput.getAbsolutePath())))) {
            // first 4 bytes are the GSD2 file signature
            byte[] signature = dis.readNBytes(4);
            if (!matchesSignature(signature, MAGIC_NUMBER_GSD2)) {
                throw new IOException(String.format("The file [%s] is not of GSD2 type", input));
            }
            dis.skipBytes(4); // common bytes in GSD2 files
            int gsdFileSize = dis.readInt(); // full size of the file
            dis.skipBytes(8); // common bytes in GSD2 files
            int totalFiles = dis.readInt(); // number of files it contains
            dis.skipBytes(8); // common bytes in GSD2 files
            dis.skipBytes(80); // trailing zeros in header

            System.out.println(String.format("This GSD2 file has a size of [%s] bytes and contains [%s] files", gsdFileSize, totalFiles));
            System.out.println("--------------------------------------------------------------------");

            // after this seems we have the list of files that GSD2 file contains
            // every set of byte contains information from the file (id, offset, length... etc)
            while (totalFiles > binFiles.size()) {
                int id = dis.readInt(); // id of the file
                int offset = dis.readInt(); // offset of the file
                int fSize = dis.readInt(); // file size
                int dSize = dis.readInt(); // file size in disk
                int dummy = dis.readInt(); // variable unidentified bytes
                dis.skipBytes(4); // common bytes in all files
                String name = new String(dis.readNBytes(56)).trim(); // 56 bytes reserved for the file name

                // we have already all data we need, so register the file info inside a list to process them later
                BinFile file = new BinFile(id, name, offset, fSize, dSize, dummy, null, new ArrayList<>());
                binFiles.add(file);
                System.out.println(String.format("Found file #%s --> %s", String.format("%05d", file.getId()), file));
            }
        }

        // begin with the extract
        System.out.println("--------------------------------------------------------------------");
        System.out.println("Extracting files into " + outputFolder + "...");

        // create output folder if doesn't exist
        File directory = new File(outputFolder);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // generate files schema
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(outputFolder + File.separator + "schema.json"), binFiles);

        // reopen file in RandomAccessFile to seek for offsets and extract files
        try (RandomAccessFile raf = new RandomAccessFile(fileInput.getAbsolutePath(), "r")) {
            for (int i = 0; i < binFiles.size(); i++) {
                System.out.println(String.format("Extracting file [%s] ...", binFiles.get(i).getName()));
                try {
                    // get the bytes from file using the offset and length
                    byte[] arrayBytes = new byte[binFiles.get(i).getFSize()];
                    raf.seek(Integer.valueOf(binFiles.get(i).getOffset()).longValue());
                    raf.read(arrayBytes, 0, binFiles.get(i).getFSize());
                    // file must be of gz type
                    if (!matchesSignature(arrayBytes, MAGIC_NUMBER_GZIP)) {
                        throw new Exception("File is not of GZ type !");
                    }
                    // uncompress and write into file
                    Path outputFile = Path.of(outputFolder + File.separator + binFiles.get(i).getName());
                    InputStream inputStream = ByteSource.wrap(arrayBytes).openStream();
                    try (GZIPInputStream gis = new GZIPInputStream(inputStream)) {
                        Files.copy(gis, outputFile);
                    }
                } catch (Exception e) {
                    binFiles.get(i).getErrors().add(
                            String.format("Error while extracting file --> %s", e)
                    );
                }
            }
        }

        // return any errors found
        if (binFiles.stream().anyMatch(file -> file.getErrors().size() > 0)) {
            binFiles.stream().filter(file -> file.getErrors().size() > 0).forEach(file -> {
                System.err.println("------------------------------------------------------------------");
                System.err.println(String.format("Errors found in file [%s]", file.getName()));
                file.getErrors().forEach(System.err::println);
            });
            throw new Exception("Errors found in some files");
        } else {
            System.out.println("\nAll files extracted successfully !!");
            System.out.println("\nProgram finished");
        }
    }

    /**
     * returns true if signature matches with the file otherwise false
     *
     * @param arrayBytes the array bytes
     * @param signature  the file siganture to check
     * @return true if matches false otherwise
     */
    private Boolean matchesSignature(byte[] arrayBytes, byte[] signature) {
        return IntStream.range(0, signature.length).allMatch(i -> arrayBytes[i] == signature[i]);
    }

}
