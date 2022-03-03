package imageprocessing.colors;

import imageprocessing.IImageProcessor;
import imageprocessing.ImageProcessing;
import main.Picsi;
import org.eclipse.swt.graphics.ImageData;

public class Grayscale implements IImageProcessor {
    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        ImageData image = ImageProcessing.createImage(inData.width, inData.height, Picsi.IMAGE_TYPE_GRAY);
        for (int x = 0; x < inData.width; x++) {
            for (int y = 0; y < inData.height; y++) {
                int pixel = inData.getPixel(x, y);
                int r = pixel & 0xff;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff0000) >> 16;
                int gray = 2 * r / 10 + 7 * g / 10 + b / 10;
                image.setPixel(x, y, gray);
            }
        }
        return image;
    }
}
