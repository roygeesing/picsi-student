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

public class Rotate implements IImageProcessor {
    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        String angleString = JOptionPane.showInputDialog("Angle");
        int angleDegrees = Integer.parseInt(angleString);
        double angle = angleDegrees / 360d * 2 * Math.PI;

        int method = OptionPane.showOptionDialog("Method", SWT.ICON_INFORMATION, new Object[]{ "Nearest Neighbor", "Bilinear" }, 0);

        int dx1 = (int) Math.abs(Math.round(Math.sin(angle) * inData.height));
        int dx2 = (int) Math.abs(Math.round(Math.cos(angle) * inData.width));
        int dy1 = (int) Math.abs(Math.round(Math.sin(angle) * inData.width));
        int dy2 = (int) Math.abs(Math.round(Math.cos(angle) * inData.height));

        int outWidth = dx1 + dx2;
        int outHeight = dy1 + dy2;

        ImageData outData = ImageProcessing.createImage(outWidth, outHeight, Picsi.IMAGE_TYPE_RGB);
        outData.palette = inData.palette;

        Matrix rotation = Matrix.rotation(angle);
        Matrix translation;
        if (angleDegrees < 90) {
            translation = Matrix.translation(0, dy1);
        } else if (angleDegrees < 180) {
            translation = Matrix.translation(dx2, dy1+dy2);
        } else if (angleDegrees < 270) {
            translation = Matrix.translation(dx1+dx2, dy2);
        } else {
            translation = Matrix.translation(dx1, 0);
        }

        Matrix combined = translation.multiply(rotation).inverse();

        for (int u = 0; u < outWidth; u++) {
            for (int v = 0; v < outHeight; v++) {
                double[] targetCoords = new double[] {u, v, 1};
                double[] sourceCoords = combined.multiply(targetCoords);

                int pixel = Util.interpolate(inData, sourceCoords[0], sourceCoords[1], method);
                outData.setPixel(u, v, pixel);
            }
        }
        return outData;
    }
}
