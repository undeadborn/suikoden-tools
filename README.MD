# Genso Suikoden I & II PSP Tools

I've created this project to share my investigations on the reverse engineering of this game.

This game is only on Japanese, so, wanted to start analysing the binaries and trying to extract any information of the game, to be able to start with a translation patch.

This is an ongoing work that will need time, but it's an start.

Feel free to contribute and help with the cause.

I use the ISO file of the game for the work

ISO MD5 CHECKSUM 
`ecedcf4008d6b1e0c8cb5e6ab672f472`

GAME ID 
`ULJM 05086`

# Tools

All tools are under the "service" package and should be run using the "App" class.
Will improve this with a console cmd menu or something in the future.

Check source code to know more information about them.

**GSDUnpacker**

This class is used to unpack the GSD files found in the game. GSD file seems like a custom package that contains binaries. Inside GSD header we found the list of files and information from them. Seems those files are compressed in GZ format.