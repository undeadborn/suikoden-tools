package net.undeadborn.suikoden.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.undeadborn.suikoden.tools.model.BinFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static java.util.stream.Collectors.toList;

public class GSD2Repacker {

    private final static byte[] MAGIC_NUMBER_GSD2 = {(byte) 0x47, (byte) 0x53, (byte) 0x44, (byte) 0x32};

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Must provide <gsd1.bin|gsd2.bin> <path of binaries> as arguments. Exiting program.");
            System.exit(1);
        }

        if (!args[0].matches("gsd1.bin|gsd2.bin")) {
            System.err.println("Type of file must be \"gsd1.bin\" for <Suikoden I> or \"gsd2.bin\" for <Suikoden II>");
            System.exit(1);
        }

        if (!new File(args[1]).exists() || !new File(args[1]).isDirectory()) {
            System.err.println(String.format("Path [%s] is not a folder or does not exist.", args[0]));
            System.exit(1);
        }

        try {
            new GSD2Repacker().start(args[0], args[1]);
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("\nErrors found. Exiting program.");
            System.exit(1);
        }
    }

    /**
     * Trigger the process to repack binaries
     *
     * @param type type of the binary pack
     * @param path binaries path
     * @throws IOException error
     */
    private void start(String type, String path) throws IOException {
        String schema = type.equals("gsd1.bin") ? "suikoden1_schema.json" : "suikoden2_schema.json";
        ObjectMapper mapper = new ObjectMapper();
        List<BinFile> schemaBinFiles = Arrays.asList(mapper.readValue(getClass().getClassLoader().getResource(schema), BinFile[].class));
        List<File> inputFiles = Files.walk(Paths.get(path)).filter(Files::isRegularFile).map(Path::toFile).collect(toList());

        List<BinFile> binFiles = schemaBinFiles.stream().map(binFile -> {
            File inputFile = inputFiles.stream().filter(file -> file.getName().equals(binFile.getName())).findFirst().orElse(null);
            BinFile newBinFile = binFile.toBuilder().build();
            newBinFile.setOffset(0);
            newBinFile.setFile(inputFile);
            return newBinFile;
        }).collect(toList());

        List<BinFile> filesNotFound = binFiles.stream().filter(file -> file.getFile() == null).collect(toList());
        if (filesNotFound.size() > 0) {
            System.err.println("Following bin files do not exist in folder:");
            filesNotFound.stream().map(BinFile::getName).forEach(System.err::println);
            throw new IOException();
        }

        String outputFile = path + File.separator + type + ".repack";
        System.out.println("Repacking files into " + outputFile + " ...");
        try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
            // write header
            raf.write(MAGIC_NUMBER_GSD2);
            raf.write(new byte[]{(byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00});
            raf.write(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
            raf.write(new byte[]{(byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00});
            raf.write(new byte[]{(byte) 0x50, (byte) 0x00, (byte) 0x00, (byte) 0x00});
            raf.write(genByteBuffer(4).putInt(binFiles.size()).array());
            raf.write(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
            raf.write(new byte[]{(byte) 0x00, (byte) 0x40, (byte) 0x01, (byte) 0x00});
            // write files
            writeFiles(raf, binFiles);
            // write files metadata
            writeFilesMetadata(raf, binFiles);
            // write final file size
            writeFileSize(raf, Long.valueOf(raf.length()).intValue());
        }

        System.out.println("\nRepack done !!");
        System.out.println("\nProgram finished");
    }

    private void writeFiles(RandomAccessFile raf, List<BinFile> files) throws IOException {
        raf.seek(81920);
        for (int i = 0; i < files.size(); i++) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                BinFile binFile = files.get(i);
                binFile.setOffset(Long.valueOf(raf.getFilePointer()).intValue());
                gzip.write(Files.readAllBytes(binFile.getFile().toPath()));
                gzip.close(); // this is important !!
                byte[] compressedFile = bos.toByteArray();
                raf.write(compressedFile);
                binFile.setFSize(compressedFile.length);
                binFile.setDSize(compressedFile.length);
            }
        }
    }

    private void writeFileSize(RandomAccessFile raf, int size) throws IOException {
        raf.seek(8);
        raf.write(genByteBuffer(4).putInt(size).array());
    }

    private void writeFilesMetadata(RandomAccessFile raf, List<BinFile> files) throws IOException {
        raf.seek(112);
        for (int i = 0; i < files.size(); i++) {
            writeFileMetadata(raf, files.get(i));
        }
    }

    private void writeFileMetadata(RandomAccessFile raf, BinFile file) throws IOException {
        raf.write(genByteBuffer(4).putInt(file.getId()).array());
        raf.write(genByteBuffer(4).putInt(file.getOffset()).array());
        raf.write(genByteBuffer(4).putInt(file.getFSize()).array());
        raf.write(genByteBuffer(4).putInt(file.getDSize()).array());
        raf.write(genByteBuffer(4).putInt(file.getDummy()).array());
        raf.write(new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        raf.write(genByteBuffer(56).put(file.getName().getBytes(StandardCharsets.UTF_8)).array());
    }

    private ByteBuffer genByteBuffer(int allocate) {
        return ByteBuffer.allocate(allocate).order(ByteOrder.LITTLE_ENDIAN);
    }

}
