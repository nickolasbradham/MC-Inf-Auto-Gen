# MC-Inf-Auto-Gen
Automates pre-generating InfDev worlds in MC.

Run this utility from the command line. On first run, it will generate a config file that you will almost certainly need to edit (The exception might be if you are runnning at 1920x1080 and 100% screen scale). You can use [this](https://github.com/nickolasbradham/Java-Mouse-and-Color-Util) utility to help get the values for the config file.

This program has been tested on Minecraft version inf-20100330. Although it probably will work on other versions, I give no guarantee.

## Configuration Details
Here are the settings in the `config.cfg` file, their defaults, and what they mean.
- buttonX=950       The X coordinate of the center of all GUI buttons.
- hotbarHue=.3      The hue of the hotbar at (`hotbarX`, `hotbarY`).
- hotbarX=790       The hotbar pixel X coiordinate.
- hotbarY=750       The hotbar pixel Y coiordinate.
- menuHue=.07       The hue of the menu background at (`menuPixelX`, `singleplayerY`).
- menuPixelX=700    The X coordinate of the menu background pixel.
- quitHue=.64       The hue of the highlighted "Quit Game" button at (`buttonX`, `singleplayerY`).
- singleplayerY=550 The Y coordinate of the center of the "Singleplayer" button.
- world1Y=410       The Y coordinate of the "World 1" button.

## Launch Arguments
`<radius> [skipX skipY]`
- radius - The chunk radius around (0, 0) to generate. Note that this program will generate a MINIMUM of this radius, but it will most likely generate slightly more.
- skipX - The generation area X coordinate to skip to. Note that this is not a chunk or block coordinate, but a generator specific coordinate reported by the output.
- skipY - The generation area Y coordinate to skip to. Note that this is not a chunk or block coordinate, but a generator specific coordinate reported by the output.
