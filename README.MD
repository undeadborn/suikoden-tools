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
* Suikoden I (`gsd1.bin`)
* Suikoden II (`gsd2.bin`)

Accepts the GSD2 file path as argument. Example:

`gsd2unpacker C:\suikoden\bin\gsd2.bin`

`gsd2unpacker C:\suikoden\bin\gsd1.bin`

# Resources

For the analysis of the bin files I've been using 010 hex editor.
The templates I created for game files are placed in `resources/010-files` folder.