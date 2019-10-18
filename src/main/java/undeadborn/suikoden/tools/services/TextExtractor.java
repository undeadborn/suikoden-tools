package undeadborn.suikoden.tools.services;

/**
 * This class is used to extract the japanese texts from the unpacked bin files
 * As per hex investigations seems texts are like at the very end of the files, and encoding is "Unicode UTF-16 LE"
 * Texts on file start after the following bytes: 00 00 00 00 00 00 00 00 FF FE
 * And this seems like a "phrase separator" for them: 0D 00 0A 00
 */
public class TextExtractor {
}
