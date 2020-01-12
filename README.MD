# Genso Suikoden I & II PSP Tools
Reverse engineering tools for Genso Suikoden I & II PSP game.

ISO MD5 CHECKSUM 
`ecedcf4008d6b1e0c8cb5e6ab672f472`

GAME ID 
`ULJM-05086`

# Requirements
There is no console cmd input yet so you need to run this on a IDE by now.

Requirements:
* JDK8 or superior
* Graddle for dependency management
* Java IDE of your preference (e.g. IntelliJ)

# Tools
Tools are placed on `undeadborn.suikoden.tools.services` package. Tools are mean to be executed through the `undeadborn.suikoden.tools.App` class.

**GSD2Unpacker**

Service to unpack the GSD2 files from the game. There is one GSD2 file for each game. Suikoden I (`gsd1.bin`) and Suikoden II (`gsd2.bin`) These files are suppose to contain all the games data. Seems likely a custom package just containing GZ files.

This tool will export and extract those GZ files into folders.

# To do
* Create ISO extractor and repacker tool.
* Create Text Extractor tool to export all game text data into a readable format.
* Create Text Replacer tool to replace Japanese chars to a Latin alphabet.
* Investigate pending unidentified GSD2 bytes to create a valid GSD2 repacker tool.