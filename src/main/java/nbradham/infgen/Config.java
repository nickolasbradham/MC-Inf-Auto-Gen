package nbradham.infgen;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

final class Config {
	private static final File FILE = new File("config.cfg");
	static final Point BUT_SP = new Point();

	static final boolean loadConfig() throws FileNotFoundException, IOException {
		Properties p = new Properties();
		boolean fileExists = FILE.exists();
		if (fileExists)
			p.load(new FileInputStream(FILE));
		p.putIfAbsent("singleplayerX", 670);
		p.putIfAbsent("singleplayerY", 390);
		return fileExists;
	}
}