package imageprocessing.colors;

import imageprocessing.IImageProcessor;
import org.eclipse.swt.graphics.ImageData;

public class MyInverter implements IImageProcessor {
    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        for (int x = 0; x < inData.width; x++) {
            for (int y = 0; y < inData.height; y++) {
                inData.setPixel(x, y, ~inData.getPixel(x, y));
            }
        }
        return inData;
    }
}
