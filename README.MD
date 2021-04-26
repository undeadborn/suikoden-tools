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

**GSD2Unpacker**

Unpack all files from GSD2 binaries. There is one GSD2 file for each game. These files contain all game data.

* Suikoden I `ISO://PSP_GAME/USRDIR/bin/gsd1.bin`
* Suikoden II `ISO://PSP_GAME/USRDIR/bin/gsd2.bin`

Accepts the GSD2 file path as argument. Example:

`gsd2unpacker C:\suikoden\bin\gsd2.bin`

`gsd2unpacker C:\suikoden\bin\gsd1.bin`

**GSD2Repacker**

Repack all files into a GSD2 bin file.

All required files for that particular GSD2 file must exist in the path provided.

Accepts the GSD2 file type you want to repack and the path containing the files to repack. Example:

`gsd2repacker gsd2.bin C:\suikoden\gsd2.bin_repack`

# Resources

For the analysis of the bin files I've been using 010 hex editor.

The templates I created for game files are placed in `resources/010-files` folder.