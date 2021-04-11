package net.undeadborn.suikoden.tools.gsd2unpacker;

import com.google.common.io.LittleEndianDataInputStream;
import net.undeadborn.suikoden.tools.gsd2unpacker.common.Constants;
import net.undeadborn.suikoden.tools.gsd2unpacker.model.BinFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class is used to unpack GSD2 files from the game
 * These GSD2 file contains game data and they are found under the following paths
 * -- Suikoden I  --> ISO://PSP_GAME/USRDIR/bin/gsd1.bin
 * -- Suikoden II --> ISO://PSP_GAME/USRDIR/bin/gsd2.bin
 */
public class App {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Should provide a GSD2 file path as argument. Exiting program.");
            System.exit(0);
        }

        if (!new File(args[0]).exists()) {
            System.err.println(String.format("File [%s] does not exist.", args[0]));
            System.exit(0);
        }

        try {
            new App().start(args[0]);
        } catch (Exception e) {
            System.err.println("Error found. Exiting program.");
            throw e;
        }
    }

    /**
     * Trigger the process to unpack binaries
     *
     * @param input The path of the GSD2 file in your filesystem
     * @throws IOException error
     */
    public void start(String input) throws IOException {
        List<BinFile> binFiles = new ArrayList<>();
        File fileInput = new File(input);

        String workingFolder = fileInput.getParent();
        String outputFolder = workingFolder + File.separator + fileInput.getName() + "_extract";

        try (LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new BufferedInputStream(new FileInputStream(fileInput.getAbsolutePath())))) {
            // first 4 bytes are the GSD2 file signature
            byte[] signature = dis.readNBytes(4);
            if (!matchesSignature(signature, Constants.GSD2.SIGNATURE)) {
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
                int off = dis.readInt(); // offset of the file
                int size = dis.readInt(); // size of the gz file
                int len = dis.readInt(); // size of the file
                dis.skipBytes(4); // variable unidentified bytes
                dis.skipBytes(4); // common bytes in all files
                String name = new String(dis.readNBytes(56)).trim(); // 56 bytes reserved for the file name

                // we have already all data we need, so register the file info inside a list to process them later
                BinFile file = new BinFile(name, id, off, size, len, new ArrayList<>());
                binFiles.add(file);
                System.out.println(String.format("Found file #%s --> %s", String.format("%05d", file.getId()), file));
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

        // reopen file in RandomAccessFile to seek for offsets and extract files
        try (RandomAccessFile raf = new RandomAccessFile(fileInput.getAbsolutePath(), "r")) {
            for (int i = 0; i < binFiles.size(); i++) {
                System.out.println(String.format("Extracting file [%s] ...", binFiles.get(i).getName()));
                try {
                    // get the bytes from file using the offset and length
                    byte[] arrayBytes = new byte[binFiles.get(i).getSize()];
                    raf.seek(Integer.valueOf(binFiles.get(i).getOffset()).longValue());
                    raf.read(arrayBytes, 0, binFiles.get(i).getSize());
                    // write stream into file
                    try (FileOutputStream fos = new FileOutputStream(outputFolder + File.separator + binFiles.get(i).getName() + "." + getFileExtension(arrayBytes))) {
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
                file.getErrors().forEach(System.err::println);
            });
            System.err.println("\nProgram finished with errors");
        } else {
            System.out.println("\nAll files extracted successfully !!");
            System.out.println("\nProgram finished");
        }
    }

    /**
     * returns true if signature matches with the file otherwise false
     * @param arrayBytes the array bytes
     * @param signature the file siganture to check
     * @return true if matches false otherwise
     */
    private Boolean matchesSignature(byte[] arrayBytes, byte[] signature) {
        return IntStream.range(0, signature.length).allMatch(i -> arrayBytes[i] == signature[i]);
    }

    /**
     * Get the file extension using the file signature (the first bytes of the file)
     *
     * @param arrayBytes the array bytes
     * @return the file extension
     * @throws Exception if no file extension found
     */
    private String getFileExtension(byte[] arrayBytes) throws Exception {
        if (matchesSignature(arrayBytes, Constants.GZIP.SIGNATURE)) {
            return Constants.GZIP.EXTENSION;
        }
        throw new Exception("No file format found for the specific file");
    }

}
