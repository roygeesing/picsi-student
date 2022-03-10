package imageprocessing.transform;

import gui.OptionPane;
import imageprocessing.IImageProcessor;
import imageprocessing.ImageProcessing;
import main.Picsi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import utils.Matrix;
import utils.Util;

import javax.swing.*;

public class Scale implements IImageProcessor {
    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        String scaleString = JOptionPane.showInputDialog("Scale");
        double scale = Double.parseDouble(scaleString);

        int method = OptionPane.showOptionDialog("Method", SWT.ICON_INFORMATION, new Object[]{ "Nearest Neighbor", "Bilinear" }, 0);

        Matrix scaling = Matrix.scaling(scale, scale).inverse();

        int outWidth = (int) Math.round(inData.width * scale);
        int outHeight = (int) Math.round(inData.height * scale);
        ImageData outData = ImageProcessing.createImage(outWidth, outHeight, Picsi.IMAGE_TYPE_RGB);
        outData.palette = inData.palette;

        for (int u = 0; u < outWidth; u++) {
            for (int v = 0; v < outHeight; v++) {
                double[] targetCoords = new double[] {u, v, 1};
                double[] sourceCoords = scaling.multiply(targetCoords);

                int pixel = Util.interpolate(inData, sourceCoords[0], sourceCoords[1], method);
                outData.setPixel(u, v, pixel);
            }
        }

        return outData;
    }
}
