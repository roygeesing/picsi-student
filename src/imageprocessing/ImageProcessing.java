package imageprocessing;

import utils.Parallel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;

/**
 * Image processing class: contains widely used image processing functions
 * SWT help: https://www.eclipse.org/swt/javadoc.php
 * 
 * @author Christoph Stamm
 *
 */
public class ImageProcessing {
	/**
	 * Compute PSNR of two images of the same image type
	 * @param inData1
	 * @param inData2
	 * @param imageType
	 * @return double-array of length 1 or 3 containing the separate PSNR of each channel
	 */
	public static double[] psnr(ImageData inData1, ImageData inData2, int imageType) {
		// TODO
		return null;
	}
	
	/**
	 * Clamp to range [0,255]
	 * @param v
	 * @return
	 */
	public static int clamp8(int v) {
		// needs only one test in the usual case
		if ((v & 0xFFFFFF00) != 0) 
			return (v < 0) ? 0 : 255; 
		else 
			return v;
		//return (v >= 0) ? (v < 256 ? v : 255) : 0;
	}
	
	/**
	 * Clamp to range [0,255]
	 * @param d
	 * @return
	 */
	public static int clamp8(double d) {
		if (d < 0) {
			return 0;
		} else if (d > 255) {
			return 255;
		} else {
			return (int)Math.round(d);
		}
	}

	/**
	 * Convert normalized value in the range [0,1] to equivalent byte value
	 * From byte to normalized, just use b/256.0
	 * Math.round(d*255) would result in 254 full and 2 half ranges
	 * @param d in range [0,1]
	 * @return equivalent value in range [0, 255]
	 */
	public static int normalized2byte(double d) {
		final int v = (int)(256*d);
		if (v > 255) return 255;
		else if (v < 0) return 0;
		else return v;
	}

	/**
	 * Compute image histogram with nClasses
	 * @param inData
	 * @param nClasses <= 256
	 * @return
	 */
	public static int[] histogram(ImageData inData, int nClasses) {
		final int maxClasses = 1 << inData.depth;
		assert 0 < nClasses && nClasses <= maxClasses : "wrong number of classes: " + nClasses;
		
		int[] histo = new int[nClasses];
		
		Parallel.For(0, inData.height,
			// creator
			() -> new int[nClasses],
			// loop body
			(v, h) -> {
				for (int u=0; u < inData.width; u++) {
					h[inData.getPixel(u, v)*nClasses/maxClasses]++;
				}
			},
			// reducer
			h -> {
				for(int i=0; i < histo.length; i++) histo[i] += h[i];
			}
		);
		return histo;
	}

	/**
	 * Compute RGB image histogram for a selected channel
	 * @param inData
	 * @param channel [0..2]
	 * @return
	 */
	public static int[] histogramRGB(ImageData inData, int channel) {
		final int nClasses = 256;
		assert inData.palette.isDirect : "wrong image type";
	
		int[] histo = new int[nClasses];
		final int mask, shift;
		switch(channel) {
		case 0: mask = inData.palette.redMask; shift = inData.palette.redShift; break;
		case 1: mask = inData.palette.greenMask; shift = inData.palette.greenShift; break;
		default: mask = inData.palette.blueMask; shift = inData.palette.blueShift; break;
		}
		
		Parallel.For(0, inData.height,
			// creator
			() -> new int[nClasses],
			// loop body
			(v, h) -> {
				for (int u=0; u < inData.width; u++) {
					final int pixel = inData.getPixel(u, v);
					// mask can be negative -> use >>> instead of >>
					h[(shift > 0) ? (mask & pixel) << shift : (mask & pixel) >>> -shift]++;					
				}
			},
			// reducer
			h -> {
				for(int i=0; i < histo.length; i++) histo[i] += h[i];
			}
		);
		return histo;
	}
	
	/**
	 * Crops input image of given input rectangle
	 */
	public static ImageData crop(ImageData inData, int x, int y, int w, int h) {
		ImageData outData = new ImageData(w, h, inData.depth, inData.palette);
		
		for (int v=0; v < h; v++) {
			for (int u=0; u < w; u++) {
				outData.setPixel(u, v, inData.getPixel(u + x, v + y));
			}
			if (inData.getTransparencyType() == SWT.TRANSPARENCY_ALPHA) {
				for (int u=0; u < w; u++) {
					outData.setAlpha(u, v, inData.getAlpha(u + x, v + y));
				}
			}
		}
		return outData;
	}
	
	/**
	 * Inserts image insData into image data at position (x,y)
	 */
	public static boolean insert(ImageData data, ImageData insData, int x, int y) {
		if (data.depth != insData.depth) return false;
		int x2 = Math.min(data.width, x + insData.width);
		int y2 = Math.min(data.height, y + insData.height);
		
		for (int v=y; v < y2; v++) {
			for (int u=x; u < x2; u++) {
				data.setPixel(u, v, insData.getPixel(u - x, v - y));
			}
		}
		return true;
	}
	
}
