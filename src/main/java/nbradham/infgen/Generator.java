package nbradham.infgen;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

final class Generator implements NativeKeyListener {

	private void start() throws NativeHookException {
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(this);
		//TODO Everything else.
		GlobalScreen.unregisterNativeHook();
	}

	/**
	 * Invoked when a key has been typed.
	 *
	 * @param nativeEvent the native key event.
	 */
	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeEvent) {

	}

	public static void main(String[] args) throws NativeHookException {
		new Generator().start();
	}
}