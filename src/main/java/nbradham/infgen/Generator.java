package nbradham.infgen;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JOptionPane;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;

/**
 * Handles core execution.
 * 
 * @author Nickolas Bradham
 *
 */
final class Generator implements NativeKeyListener {

	private static final String LEVEL_DAT = Path
			.of(System.getenv("appdata"), ".minecraft", "saves", "World1", "level.dat").toString();
	private static final short LOAD_DIST = 512;

	/**
	 * Holds direction travel information.
	 * 
	 * @author Nickolas Bradham
	 *
	 */
	private static enum Dir {
		UP(0, 1), RIGHT(1, 0), DOWN(0, -1), LEFT(-1, 0);

		private final int dx, dy;

		/**
		 * Constructs a new Dir with movement info.
		 * 
		 * @param x The x movement.
		 * @param y The y movement.
		 */
		private Dir(int x, int y) {
			dx = x;
			dy = y;
		}

		/**
		 * Retrieves the next direction in the spiral.
		 * 
		 * @param cur The current direction.
		 * @return The next direction to travel.
		 */
		private static Dir next(Dir cur) {
			switch (cur) {
			case DOWN:
				return Dir.LEFT;
			case LEFT:
				return Dir.UP;
			case RIGHT:
				return Dir.DOWN;
			case UP:
				return Dir.RIGHT;
			}
			return null;
		}
	};

	private final Robot bot;
	private final NamedTag nbt;
	private final ListTag<?> pos;
	private final double r;
	private final byte skpX, skpY;
	private byte x = 0, y = 0;
	private boolean pause = false;

	/**
	 * Constructs a new Generator.
	 * 
	 * @param radius The chunk radius to generate.
	 * @param skipX  The x AREA to skip to.
	 * @param skipY  The y AREA to skip to.
	 * @throws IOException  Thrown by {@link NBTUtil#read(File)}.
	 * @throws AWTException Thrown by {@link Robot#Robot()}.
	 */
	private Generator(byte radius, byte skipX, byte skipY) throws IOException, AWTException {
		bot = new Robot();
		nbt = NBTUtil.read(LEVEL_DAT);
		pos = ((CompoundTag) nbt.getTag()).getCompoundTag("Data").getCompoundTag("Player").getListTag("Pos");
		r = Math.ceil(radius / 16);
		skpX = skipX;
		skpY = skipY;
	}

	/**
	 * Starts the generation algorithm.
	 * 
	 * @throws NativeHookException Thrown by
	 *                             {@link GlobalScreen#registerNativeHook()} and
	 *                             {@link GlobalScreen#unregisterNativeHook()}.
	 * @throws IOException         Thrown by {@link #genArea()} and
	 *                             {@link #saveSpawn()}.
	 */
	private void start() throws NativeHookException, IOException {
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(this);

		JOptionPane.showMessageDialog(null, "Ready");

		byte t = 0;
		Dir d = Dir.UP;
		while (isStartNotMet()) {
			t++;
			for (byte n = 0; n < 2 && isStartNotMet(); n++) {
				d = Dir.next(d);
				for (byte m = 0; m < t && isStartNotMet(); m++) {
					x += d.dx;
					y += d.dy;
				}
			}
		}

		try {
			main: while (isRadiusNotMet()) {
				for (byte n = 0; n < 2 && isRadiusNotMet(); n++) {
					for (byte m = 0; m < t && isRadiusNotMet(); m++) {
						genArea();
						x += d.dx;
						y += d.dy;
						if (pause) {
							if (JOptionPane.showConfirmDialog(null, "Continue?\n(No will quit the program)", "Paused",
									JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
								break main;
							pause = false;
						}
					}
					d = Dir.next(d);
				}
				t++;
			}
		} catch (GameCrashedException e) {
			e.printStackTrace();
		}

		x = y = 0;
		saveSpawn();
		GlobalScreen.unregisterNativeHook();
		System.out.println("Done.");
	}

	/**
	 * Performs one generation cycle.
	 * 
	 * @throws IOException          Thrown by {@link #saveSpawn()}.
	 * @throws GameCrashedException Thrown by {@link #waitForDif(short, float)} and
	 *                              {@link #waitForPixel(short, float)}.
	 */
	private void genArea() throws IOException, GameCrashedException {
		System.out.printf("Generating area: (%d, %d)%n", x, y);
		saveSpawn();
		click(Config.sp);
		click(Config.w1);
		System.out.println("Waiting for load...");
		waitForDif(Config.menuX, Config.menuHue);
		bot.keyPress(KeyEvent.VK_ESCAPE);
		sleep(100);
		bot.keyRelease(KeyEvent.VK_ESCAPE);
		System.out.println("Waiting for pause...");
		waitForPixel(Config.buttonX, Config.quitHue);
		click(Config.sp);
		System.out.println("Waiting for menu...");
		waitForPixel(Config.menuX, Config.menuHue);
	}

	/**
	 * Clicks the mouse at (buttonX, {@code y}).
	 * 
	 * @param y The y coordinate to click at.
	 */
	private void click(short y) {
		bot.mouseMove(Config.buttonX, y);
		bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		sleep(100);
		bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	/**
	 * tests if pixel at ({@code x}, sp) has the same hue as {@code hue}.
	 * 
	 * @param x   The x coordinate.
	 * @param hue The target hue.
	 * @return True if the hues match.
	 */
	private boolean isPixel(short x, float hue) {
		return Math.abs(hue - getPixel(x)) < .01;
	}

	/**
	 * Retrieves the hue of the pixel at ({@code x}, sp).
	 * 
	 * @param x The x coordinate.
	 * @return The pixel's hue.
	 */
	private float getPixel(short x) {
		Color c = bot.getPixelColor(x, Config.sp);
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0];
	}

	/**
	 * Waits until the pixel at ({@code x}, sp) has a different hue that
	 * {@code hue}.
	 * 
	 * @param x   The target x coordinate.
	 * @param hue The target hue.
	 * @throws GameCrashedException Thrown by {@link #checkCrash(long)}.
	 */
	private void waitForDif(short x, float hue) throws GameCrashedException {
		long blackTime = -1;
		while (isPixel(x, hue))
			blackTime = checkCrash(blackTime);
	}

	/**
	 * Waits until the pixel at ({@code x}, sp) has the samne hue as {@code hue}.
	 * 
	 * @param x   The target x coordinate.
	 * @param hue The target hue.
	 * @throws GameCrashedException Thrown by {@link #checkCrash(long)}.
	 */
	private void waitForPixel(short x, float hue) throws GameCrashedException {
		long blackTime = -1;
		while (!isPixel(x, hue))
			blackTime = checkCrash(blackTime);
	}

	/**
	 * Detects if the game has crashed.
	 * 
	 * @param blackTime The time when the screen went black.
	 * @return current time if the screen is black and {@code blackTime < 0}, -1 if
	 *         the screen is not black, else {@code blackTime}.
	 * @throws GameCrashedException Thrown if a game crash has been detected.
	 */
	private long checkCrash(long blackTime) throws GameCrashedException {
		if (isPixel(Config.menuX, 0)) {
			if (blackTime < 0)
				return System.currentTimeMillis();
			else if (System.currentTimeMillis() - blackTime > 1000)
				throw new GameCrashedException();
		} else
			return -1;
		return blackTime;
	}

	/**
	 * Saves the next generation coordinates to disk.
	 * 
	 * @throws IOException Thrown by {@link NBTUtil#write(NamedTag, File)}.
	 */
	private void saveSpawn() throws IOException {
		int cx = x * LOAD_DIST, cy = y * LOAD_DIST;
		setPosVal(0, cx + .5);
		setPosVal(2, cy + .5);
		NBTUtil.write(nbt, LEVEL_DAT);
	}

	/**
	 * Updates position values of the NBT data.
	 * 
	 * @param i   The position value index.
	 * @param val The new value.
	 */
	private void setPosVal(int i, double val) {
		((DoubleTag) pos.get(i)).setValue(val);
	}

	/**
	 * Checks if we haven't reached the end of the skipped area.
	 * 
	 * @return True if we are still in area to skip.
	 */
	private boolean isStartNotMet() {
		return x != skpX || y != skpY;
	}

	/**
	 * Checks if we are still in the generation radius.
	 * 
	 * @return True if we are in the area designated for generation.
	 */
	private boolean isRadiusNotMet() {
		return Math.abs(x) < r && Math.abs(y) < r;
	}

	/**
	 * Invoked when a key has been typed.
	 *
	 * @param nativeEvent the native key event.
	 */
	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
		if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_P)
			pause = true;
	}

	/**
	 * Sleeps for {@code t} milliseconds.
	 * 
	 * @param t The time to sleep for.
	 */
	private static void sleep(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses arguments and starts a new Generator instance.
	 * 
	 * @param args Command line arguments.
	 * @throws FileNotFoundException Thrown by {@link Config#loadConfig()}.
	 * @throws IOException           Thrown by {@link Config#loadConfig()},
	 *                               {@link #Generator(byte, byte, byte)}, and
	 *                               {@link #start()}.
	 * @throws NumberFormatException Thrown by {@link Byte#parseByte(String)}.
	 * @throws NativeHookException   Thrown by {@link #start()}.
	 * @throws AWTException          Thrown by {@link #Generator(byte, byte, byte)}.
	 */
	public static void main(String[] args)
			throws FileNotFoundException, IOException, NumberFormatException, NativeHookException, AWTException {
		if (!Config.loadConfig()) {
			System.out.println("Config generated. Check before relaunch.");
			return;
		}
		if (!(args.length == 1 || args.length == 3)) {
			System.out.println(
					"Arguments: <radius> [skipX skipY]\n  radius - Chunk radius to generate.\n  skipX - Generator region skip X.\n skipY = Generator region skip Y.");
			return;
		}
		if (!new File(LEVEL_DAT).exists()) {
			System.out.println("You must create a new World1 first.");
			return;
		}

		byte x = 0, y = 0;
		if (args.length > 1) {
			x = Byte.parseByte(args[1]);
			y = Byte.parseByte(args[2]);
		}

		new Generator(Byte.parseByte(args[0]), x, y).start();
	}
}