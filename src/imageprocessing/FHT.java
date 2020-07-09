package imageprocessing;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import utils.Complex;

/**
 * 2D Fast Hartley Transform
 * @author Christoph Stamm
 *
 */
public class FHT extends FHT1D implements Cloneable {
	private int width, height;
	private boolean isFrequencyDomain;
	private int maxN;
	private float[] pixels;
	private int depth;
	private PaletteData palette;
	
	public FHT(ImageData inData) {
		this(inData, 1);
	}

	/**
	 * Constructor for forward transform
	 * @param inData
	 * @param norm
	 */
	public FHT(ImageData inData, int norm) {
		width = inData.width;
		height = inData.height;
		depth = inData.depth;
		palette = inData.palette;
		isFrequencyDomain = false;
		
		int l = Math.max(inData.width, inData.height) - 1;
		maxN = 1;
		while(l > 0) {
			l >>= 1;
			maxN <<= 1;
		}
		pixels = new float[maxN*maxN];
		
		int iPos = 0, oPos = 0;
		for (int v = 0; v < inData.height; v++) {
			for (int u = 0; u < inData.width; u++) {
				//pixels[oPos++] = inData.data[iPos++]/(float)norm; // signed values
				pixels[oPos++] = (0xFF & inData.data[iPos++])/(float)norm; // unsigned values
			}
			iPos += inData.bytesPerLine - inData.width;
			oPos += maxN - inData.width;
		}
	}

	/**
	 * Constructor for inverse transform
	 * @param G
	 * @param w
	 * @param h
	 * @param depth
	 * @param palette
	 */
	public FHT(Complex[][] G, int w, int h, int depth, PaletteData palette) {
		width = w;
		height = h;
		this.depth = depth;
		this.palette = palette;
		maxN = G.length;
		maxN = G[0].length;
		pixels = new float[maxN*maxN];
		isFrequencyDomain = true;
		
		int base = 0;
		for (int row = 0; row < maxN; row++) {
	        int offs = ((maxN - row)%maxN)*maxN;
	        
	        for (int col = 0; col < maxN; col++) {
	        	int omegaPlus = base + col;
	        	int omegaNeg = offs + ((maxN - col)%maxN);
	        	Complex c = G[row][col];
	        	
	        	// compute FHT using FT
	        	pixels[omegaPlus] = (float)(c.m_re - c.m_im);
	        	pixels[omegaNeg]  = (float)(c.m_re + c.m_im);
	        }
	        base += maxN;
		}
	}

	private FHT(FHT fht2D, float[] fht) {
		maxN = fht2D.maxN;
		width = fht2D.width;
		height = fht2D.height;
		depth = fht2D.depth;
		palette = fht2D.palette;
		
		assert fht.length == maxN*maxN : "fht has wrong length";
		pixels = fht;
		isFrequencyDomain = true;		
	}
	
	@Override
	public FHT clone() {
		FHT res = new FHT(this, pixels.clone());
		isFrequencyDomain = res.isFrequencyDomain;
		return res;
	}

	/**
	 * Performs a forward transform, converting this image into the frequency
	 * domain. The image contained in this FHT2D must be square and its width must
	 * be a power of 2.
	 */
	public void transform() {
		rc2DFHT(pixels, false, maxN);
		isFrequencyDomain = true;
	}

	/**
	 * Performs an inverse transform, converting this image into the space
	 * domain. The image contained in this FHT2D must be square and its width must
	 * be a power of 2.
	 */
	public void inverseTransform() {
		rc2DFHT(pixels, true, maxN);
		isFrequencyDomain = false;
	}

	/** Performs a 2D FHT (Fast Hartley Transform). */
	private void rc2DFHT(float[] x, boolean inverse, int maxN) {
		initializeTables(maxN);
		for (int row = 0; row < maxN; row++)
			dfht3(x, row*maxN, inverse, maxN);
		transposeR(x, maxN);
		for (int row = 0; row < maxN; row++)
			dfht3(x, row * maxN, inverse, maxN);
		transposeR(x, maxN);

		int mRow, mCol;
		float A, B, C, D, E;
		for (int row = 0; row <= maxN / 2; row++) { // Now calculate actual Hartley transform
			for (int col = 0; col <= maxN / 2; col++) {
				mRow = (maxN - row) % maxN;
				mCol = (maxN - col) % maxN;
				A = x[row * maxN + col]; // see Bracewell, 'Fast 2D Hartley Transf.' IEEE Procs. 9/86
				B = x[mRow * maxN + col];
				C = x[row * maxN + mCol];
				D = x[mRow * maxN + mCol];
				E = ((A + D) - (B + C)) / 2;
				x[row * maxN + col] = A - E;
				x[mRow * maxN + col] = B + E;
				x[row * maxN + mCol] = C + E;
				x[mRow * maxN + mCol] = D - E;
			}
		}
	}

	public ImageData getImage() {
		ImageData outData = new ImageData(width, height, depth, palette);
		
		int iPos = 0, oPos = 0;
		for(int v = 0; v < outData.height; v++) {
			for(int u = 0; u < outData.width; u++) {
				outData.data[oPos++] = (byte)ImageProcessing.clamp8(pixels[iPos++]);	// unsigned values
				//outData.data[oPos++] = (byte)ImageProcessing.signedClamp8(pixels[iPos++]);	// signed values				
			}
			iPos += maxN - outData.width;
			oPos += outData.bytesPerLine - outData.width;
		}
		
		return outData;
	}
	
	public Complex[][] getSpectrum() {
		if (!isFrequencyDomain)
			throw new  IllegalArgumentException("Frequency domain image required");
		
		Complex[][] G = new Complex[maxN][maxN];

		int base = 0;
		for (int row = 0; row < maxN; row++) {
	        final int offs = ((maxN - row)%maxN)*maxN;
	        
	        for (int col = 0; col < maxN; col++) {
	        	final int omegaPlus = base + col;
	        	final int omegaNeg = offs + ((maxN - col)%maxN);
	        	
	        	// compute FT using FHT
	        	G[row][col] = new Complex((pixels[omegaPlus] + pixels[omegaNeg])*0.5, (-pixels[omegaPlus] + pixels[omegaNeg])*0.5);
	        }
	        base += maxN;
		}
		return G;
	}

	/*void changeValues(ImageData inData, int v1, int v2, int v3) {
		for (int i=0; i < pixels.length; i++) {
			int v = inData.data[i] & 0xFF;
			if (v >= v1 && v <= v2)
				pixels[i] = (byte)v3;
		}
	}*/

	/** Returns the image resulting from the point by point Hartley multiplication
		of this image and the specified image. Both images are assumed to be in
		the frequency domain. Multiplication in the frequency domain is equivalent 
		to convolution in the space domain. */
	public FHT multiply(FHT fht) {
		return multiply(fht, false);
	}

	/** Returns the image resulting from the point by point Hartley conjugate 
		multiplication of this image and the specified image. Both images are 
		assumed to be in the frequency domain. Conjugate multiplication in
		the frequency domain is equivalent to correlation in the space domain. */
	public FHT conjugateMultiply(FHT fht) {
		return multiply(fht, true);
	}

	FHT multiply(FHT fht, boolean conjugate) {
		float[] p1 = pixels;
		float[] p2 = fht.pixels;
		float[] tmp = new float[maxN*maxN];
		
		for (int r = 0; r < maxN; r++) {
			final int rowMod = (maxN - r) % maxN;
			
			for (int c = 0; c < maxN; c++) {
				final int colMod = (maxN - c) % maxN;
				final double h2e = (p2[r*maxN + c] + p2[rowMod*maxN + colMod])/2;
				final double h2o = (p2[r*maxN + c] - p2[rowMod*maxN + colMod])/2;
				if (conjugate) 
					tmp[r*maxN + c] = (float)(p1[r*maxN + c]*h2e - p1[rowMod*maxN + colMod]*h2o);
				else
					tmp[r*maxN + c] = (float)(p1[r*maxN + c]*h2e + p1[rowMod*maxN + colMod]*h2o);
			}
		}
		return new FHT(this, tmp);
	}
		
	/** Returns the image resulting from the point by point Hartley division
		of this image by the specified image. Both images are assumed to be in
		the frequency domain. Division in the frequency domain is equivalent 
		to deconvolution in the space domain. */
	public FHT divide(FHT fht) {
		float[] p1 = pixels;
		float[] p2 = fht.pixels;
		float[] out = new float[maxN*maxN];
		
		for (int r = 0; r < maxN; r++) {
			final int rowMod = (maxN - r) % maxN;
			
			for (int c = 0; c < maxN; c++) {
				final int colMod = (maxN - c) % maxN;
				
				double mag = p2[r*maxN + c] * p2[r*maxN + c] + p2[rowMod*maxN + colMod]*p2[rowMod*maxN + colMod];
				if (mag < 1e-20) mag = 1e-20;
				final double h2e = (p2[r*maxN + c] + p2[rowMod*maxN + colMod]);
				final double h2o = (p2[r*maxN + c] - p2[rowMod*maxN + colMod]);
				final double tmp = (p1[r*maxN + c]*h2e - p1[rowMod*maxN + colMod]*h2o);
				out[r*maxN + c] = (float)(tmp/mag);
			}
		}
		return new FHT(this, out);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof FHT) {
			FHT fht = (FHT)o;
			if (width != fht.width) return false;
			if (height != fht.height) return false;
			if (isFrequencyDomain != fht.isFrequencyDomain) return false;
			if (maxN != fht.maxN) return false;
			if (depth != fht.depth) return false;
			final int size = height*width;
			for(int i = 0; i < size; i++) {
				if (pixels[i] != fht.pixels[i]) return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void round() {
		assert !isFrequencyDomain : "frequency domain is not expected";
	
		final int size = height*width;
		for(int i = 0; i < size; i++) {
			pixels[i] = Math.round(pixels[i]);
		}
	}
}
