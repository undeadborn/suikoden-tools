# Genso Suikoden I & II PSP Tools
Reverse engineering tools for Genso Suikoden I & II PSP game.

ISO MD5 CHECKSUM 
`ecedcf4008d6b1e0c8cb5e6ab672f472`

GAME ID 
`ULJM-05086`

# Requirements
* Gradle
* JDK11

# Tools
Tools are placed on `net.undeadborn.suikoden.tools.services` package.

**GSD2Unpacker**

Service to unpack the GSD2 files from the game. There is one GSD2 file for each game. Suikoden I (`gsd1.bin`) and Suikoden II (`gsd2.bin`). These files are suppose to contain all the games data. Seems likely a custom package just containing GZ files.

This tool will export those GZ files into a folder.

# Resources

For the analysis of the bin files I've been using 010 hex editor.
The templates I created for game files are placed in `src/main/resources/010-files`