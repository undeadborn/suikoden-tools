package undeadborn.suikoden.tools.services;

import undeadborn.suikoden.tools.common.Constants;
import undeadborn.suikoden.tools.common.Utils;
import undeadborn.suikoden.tools.model.TextData;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * This class is to extract the japanese Texts from the gz files extracted using GSD2Unpacker
 * As per hex investigations seems texts are like at the very end of the files, and the encoding is "Unicode UTF-16 LE"
 * Seems that texts on file start after the following bytes: 00 00 00 00 00 00 00 00 FF FE
 * And this seems like a "phrase separator" for them: 0D 00 0A 00
 * There are 4 bytes that informs the pointer of the texts inside the file. These bytes are on the 1C position of the file.
 */
public class TextExtractor {

    // TODO unfinished method
    public void start(String inputFolder) throws IOException {
        File folder = new File(inputFolder);
        List<TextData> textDataList = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if (file.isFile() && isGz(file.getName())) {
                try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file.getAbsolutePath())))) {
                    // didn't identified yet these bytes meaning
                    dis.skipBytes(28);
                    // this is the offset of the texts inside the file
                    int off = ByteBuffer.wrap(Utils.getBytes(dis, 4, true)).getInt();
                    // get data, process later using RandomAccessFile
                    TextData textData = new TextData(file.getName(), off);
                }
            } else {
                System.out.println(String.format("Ignoring file [%s] which is not a GZ file", file.getName()));
            }
        }

    }

    private boolean isGz(String fileName) {
        return Constants.GZIP.EXTENSION.equals(fileName.substring(fileName.lastIndexOf('.')));
    }

}
