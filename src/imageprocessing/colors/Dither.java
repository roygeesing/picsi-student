package imageprocessing.colors;

import imageprocessing.IImageProcessor;
import imageprocessing.ImageProcessing;
import main.Picsi;
import org.eclipse.swt.graphics.ImageData;

public class Dither implements IImageProcessor {
    private final Grayscale grayscale = new Grayscale();

    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        inData = grayscale.run(inData, imageType);
        ImageData image = ImageProcessing.createImage(inData.width, inData.height, Picsi.IMAGE_TYPE_BINARY);

        for (int y = 0; y < inData.height; y++) {
            for (int x = 0; x < inData.width; x++) {
                int pixel = inData.getPixel(x, y);
                int closest = pixel > 127 ? 255 : 0;
                int error = pixel - closest;

                if (x < inData.width - 1) {
                    inData.setPixel(x + 1, y, clamp(inData.getPixel(x + 1, y) + error * 7 / 16));
                }
                if (x > 0 && y < inData.height - 1) {
                    inData.setPixel(x - 1, y + 1, clamp(inData.getPixel(x - 1, y + 1) + error * 3 / 16));
                }
                if (y < inData.height - 1) {
                    inData.setPixel(x, y + 1, clamp(inData.getPixel(x, y + 1) + error * 5 / 16));
                }
                if (x < inData.width - 1 && y < inData.height - 1) {
                    inData.setPixel(x + 1, y + 1, clamp(inData.getPixel(x + 1, y + 1) + error / 16));
                }

                int binary = closest == 255 ? 0 : 1;
                image.setPixel(x, y, binary);
            }
        }

        return image;
    }

    private int clamp(int number) {
        return Math.max(Math.min(number, 255), 0);
    }
}
