// http://www.eclipse.org/swt/javadoc.php

package main;
import gui.MainWindow;
import gui.TwinView;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import files.ImageFiles;

/**
 * Main method of the Picsi image viewer
 * 
 * @author Christoph Stamm
 *
 */
public class Picsi {
	public static final int IMAGE_TYPE_BINARY = 1;
	public static final int IMAGE_TYPE_GRAY = 2;
	public static final int IMAGE_TYPE_RGB = 4;
	public static final int IMAGE_TYPE_INDEXED = 8;

	public static final String APP_NAME = "FHNW Picsi";
	public static final String APP_VERSION = "2.6.2020.33 (Student)\n\n"; // major.minor.year.week
	public static final String APP_COPYRIGHT = "Copyright \u00a9 " + new GregorianCalendar().get(Calendar.YEAR) 
			+ "\nUniversity of Applied Sciences Northwestern Switzerland\nFHNW School of Engineering, IMVS\nWindisch, Switzerland\n\n";
	public static final String APP_URL = "https://gitlab.fhnw.ch/christoph.stamm/picsi-student";
	
	public static Shell s_shell;
	
	public static void main(String[] args) {
		ImageFiles.registerUserImageFiles();
		Display display = new Display();
		MainWindow picsi = new MainWindow();
		s_shell = picsi.open(display);
		
		while (!s_shell.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
		display.dispose();
	}

	/*
	 * Open an error dialog displaying the specified information.
	 */
	public static String createMsg(String msg, Object[] args) {
		MessageFormat formatter = new MessageFormat(msg);
		return formatter.format(args);
	}
	
	public static String createMsg(String msg, Object arg) {
		MessageFormat formatter = new MessageFormat(msg);
		return formatter.format(new Object[]{arg});
	}

	public static TwinView getTwinView() {
		Control c = s_shell.getChildren()[0];
		if (c instanceof TwinView) {
			return (TwinView)c;
		} else {
			return null;
		}
	}
	
	/**
	 * Determine image type depending on given image data
	 * @param imageData
	 * @return image type
	 */
	public static int determineImageType(ImageData imageData) {
		if (imageData.depth == 1) {
			return Picsi.IMAGE_TYPE_BINARY;
		} else {
			if (imageData.palette.isDirect) {
				PaletteData palette = imageData.palette;
				
				if (imageData.depth == 8 && (palette.blueMask & palette.greenMask & palette.redMask) == 0xFF) {
					return Picsi.IMAGE_TYPE_GRAY;
				} else {
					return Picsi.IMAGE_TYPE_RGB;
				}
			} else {
				// indexed "color" image

				// check the palette
				if (imageData.depth == 8) {
					RGB[] rgbs = imageData.getRGBs();
					
					// check for grayscale
					int i = 0;
					while(i < rgbs.length && rgbs[i].blue == rgbs[i].green && rgbs[i].green == rgbs[i].red) i++;
					if (i >= rgbs.length) {
						return Picsi.IMAGE_TYPE_GRAY;
					}
				}
				return Picsi.IMAGE_TYPE_INDEXED;
			}
		}
	}
	
	/**
	 * Return image type specific short name
	 * @param imageType
	 * @return
	 */
	public static String imageTypeString(int imageType) {
		// check system file types
		switch(imageType) {
		case IMAGE_TYPE_BINARY: return "Binary";
		case IMAGE_TYPE_GRAY: return "Gray";
		case IMAGE_TYPE_RGB: return "RGB";
		case IMAGE_TYPE_INDEXED: return "Indexed";
		default:
			assert false;
			return "Unknown";
		}
	}
}
