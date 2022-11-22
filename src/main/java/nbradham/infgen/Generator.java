package nbradham.infgen;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
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

final class Generator implements NativeKeyListener {

	private static final String LEVEL_DAT = Path
			.of(System.getenv("appdata"), ".minecraft", "saves", "World1", "level.dat").toString();

	private static enum Dir {
		UP(0, 1), RIGHT(1, 0), DOWN(0, -1), LEFT(-1, 0);

		private final int dx, dy;

		private Dir(int x, int y) {
			dx = x;
			dy = y;
		}
	};

	private final Robot bot;
	private final NamedTag nbt;
	private final CompoundTag dat;
	private final double r;
	private final byte skpX, skpY;
	private byte x = 0, y = 0;

	private Generator(byte radius, byte skipX, byte skipY) throws IOException, AWTException {
		bot = new Robot();
		nbt = NBTUtil.read(LEVEL_DAT);
		dat = ((CompoundTag) nbt.getTag()).getCompoundTag("Data");
		r = Math.ceil(radius / 32);
		skpX = skipX;
		skpY = skipY;
	}

	private void start() throws NativeHookException, IOException {
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(this);

		JOptionPane.showMessageDialog(null, "Ready");

		byte t = 0;
		Dir d = Dir.UP;
		while (x != skpX || y != skpY) {
			t++;
			for (byte n = 0; n < 2 && (x != skpX || y != skpY); n++) {
				switch (d) {
				case DOWN:
					d = Dir.LEFT;
					break;
				case LEFT:
					d = Dir.UP;
					break;
				case RIGHT:
					d = Dir.DOWN;
					break;
				case UP:
					d = Dir.RIGHT;
				}
				for (byte m = 0; m < t && (x != skpX || y != skpY); m++) {
					x += d.dx;
					y += d.dy;
				}
			}
		}

		genArea();
//		while (Math.abs(x) <= r && Math.abs(y) <= r) {
//
//		}

		GlobalScreen.unregisterNativeHook();
	}

	private void genArea() throws IOException {
		System.out.printf("Generating area: (%d, %d)%n", x, y);
		dat.putInt("SpawnX", x * 32 * 16);
		dat.putInt("SpawnY", y * 32 * 16);
		NBTUtil.write(nbt, LEVEL_DAT);
		click(Config.BUT_SP);
		// TODO Continue.
	}

	private void click(Point p) {
		bot.mouseMove(p.x, p.y);
		bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	/**
	 * Invoked when a key has been typed.
	 *
	 * @param nativeEvent the native key event.
	 */
	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
		// TODO Handle.
	}

	public static void main(String[] args)
			throws NativeHookException, NumberFormatException, IOException, AWTException {
		if (!(args.length == 1 || args.length == 3)) {
			System.out.println(
					"Arguments: <radius> [skipX skipY]\n  radius - Chunk radius to generate.\n  skipX - Generator region skip X.\n skipY = Generator region skip Y.");
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