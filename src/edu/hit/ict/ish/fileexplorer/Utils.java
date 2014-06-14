package edu.hit.ict.ish.fileexplorer;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class Utils {

	public static final String[] EXTENSIONS = new String[] { ".jpg", ".jpeg", ".JPG", ".JPEG" };

	public static boolean checkExtension(File file) {
		String[] exts = EXTENSIONS;
		for (int i = 0; i < exts.length; i++) {
			if (file.getName().indexOf(exts[i]) > 0) {
				return true;
			}
		}
		return false;
	}

	public static void sortFilesByName(File[] files) {
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				if (f1.isDirectory() && f2.isFile()) {
					return -1;
				} else if (f1.isFile() && f2.isDirectory()) {
					return 1;
				} else {
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			}

		});
	}

}
