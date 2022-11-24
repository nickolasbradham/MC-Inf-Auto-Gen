package nbradham.infgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Handles configuration file IO.
 * 
 * @author Nickolas Bradham
 *
 */
final class Config {
	private static final String K_BUT_X = "buttonX", K_SP_Y = "singleplayerY", K_W1_Y = "world1Y",
			K_MEN_X = "menuPixelX", K_MEN_HUE = "menuHue", K_QUIT_HUE = "quitHue", K_CENTER_X = "centerX",
			K_CENTER_Y = "centerY", K_HB_X = "hotbarX", K_HB_Y = "hotbarY", K_HB_H = "hotbarHue";
	private static final File FILE = new File("config.cfg");
	static float menuHue, quitHue, hbh;
	static int mouseX, mouseY;
	static short buttonX, sp, w1, menuX, hbx, hby;

	/**
	 * Loads the configuration file (generates a new one if one doesn't exist).
	 * 
	 * @return True if the file was loaded.
	 * @throws FileNotFoundException Thrown by
	 *                               {@link FileInputStream#FileInputStream(File)}
	 *                               and
	 *                               {@link FileOutputStream#FileOutputStream(File)}.
	 * @throws IOException           Thrown by
	 *                               {@link Properties#load(java.io.InputStream)},
	 *                               {@link FileInputStream#FileInputStream(File)},
	 *                               and
	 *                               {@link FileOutputStream#FileOutputStream(File)}.
	 */
	static final boolean loadConfig() throws FileNotFoundException, IOException {
		Properties p = new Properties();
		boolean fileExists = FILE.exists();
		if (fileExists)
			p.load(new FileInputStream(FILE));
		p.putIfAbsent(K_BUT_X, "950");
		p.putIfAbsent(K_SP_Y, "550");
		p.putIfAbsent(K_W1_Y, "410");
		p.putIfAbsent(K_MEN_X, "700");
		p.putIfAbsent(K_MEN_HUE, ".07");
		p.putIfAbsent(K_QUIT_HUE, ".64");
		p.putIfAbsent(K_CENTER_X, "960");
		p.putIfAbsent(K_CENTER_Y, "552");
		p.putIfAbsent(K_HB_X, "790");
		p.putIfAbsent(K_HB_Y, "750");
		p.putIfAbsent(K_HB_H, ".3");
		p.store(new FileOutputStream(FILE),
				"Use https://github.com/nickolasbradham/Java-Mouse-and-Color-Util to get the values needed.");

		buttonX = Short.parseShort(p.getProperty(K_BUT_X));
		sp = Short.parseShort(p.getProperty(K_SP_Y));
		w1 = Short.parseShort(p.getProperty(K_W1_Y));
		menuX = Short.parseShort(p.getProperty(K_MEN_X));
		menuHue = Float.parseFloat(p.getProperty(K_MEN_HUE));
		quitHue = Float.parseFloat(p.getProperty(K_QUIT_HUE));
		mouseX = Short.parseShort(p.getProperty(K_CENTER_X)) + 2;
		mouseY = Short.parseShort(p.getProperty(K_CENTER_Y));
		hbx = Short.parseShort(p.getProperty(K_HB_X));
		hby = Short.parseShort(p.getProperty(K_HB_Y));
		hbh = Float.parseFloat(p.getProperty(K_HB_H));
		return fileExists;
	}
}