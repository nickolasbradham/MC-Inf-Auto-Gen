package nbradham.infgen;

import javax.swing.JOptionPane;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

final class Generator implements NativeKeyListener {

	private static enum Dir {
		UP(0, 1), RIGHT(1, 0), DOWN(0, -1), LEFT(-1, 0);

		private final int dx, dy;

		private Dir(int x, int y) {
			dx = x;
			dy = y;
		}
	};

	private byte r, skpX, skpY;

	private Generator(byte radius, byte skipX, byte skipY) {
		r = radius;
		skpX = skipX;
		skpY = skipY;
	}

	private void start() throws NativeHookException {
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(this);

		JOptionPane.showMessageDialog(null, "Ready");

		byte x = 0, y = 0, t = 1;
		Dir d = Dir.UP;
		while (x != skpX && y != skpY) {
			// TODO Continue code.
		}

		GlobalScreen.unregisterNativeHook();
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

	public static void main(String[] args) throws NativeHookException {
		if (!(args.length == 1 || args.length == 3)) {
			System.out.println(
					"Arguments: <radius> [<skipX> <skipY>]\n  radius - Chunk radius to generate.\n  skipX - Generator region skip X.\n skipY = Generator region skip Y.");
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