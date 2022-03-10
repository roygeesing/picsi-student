package imageprocessing.transform;

import gui.OptionPane;
import imageprocessing.IImageProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import utils.Matrix;
import utils.Util;

public class RotateAndScale implements IImageProcessor {
    private static final double angle = 45 / 360d * 2 * Math.PI;

    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        ImageData outData = (ImageData) inData.clone();

        int method = OptionPane.showOptionDialog("Method", SWT.ICON_INFORMATION, new Object[]{ "Nearest Neighbor", "Bilinear" }, 0);

        int dx1 = (int) Math.abs(Math.round(Math.sin(angle) * inData.height));
        int dx2 = (int) Math.abs(Math.round(Math.cos(angle) * inData.width));
        int dy1 = (int) Math.abs(Math.round(Math.sin(angle) * inData.width));
        int dy2 = (int) Math.abs(Math.round(Math.cos(angle) * inData.height));

        Matrix rotation = Matrix.rotation(angle);
        Matrix translation = Matrix.translation(-(dx1 + dx2 - (inData.width / 2d)), dy1 + dy2 - inData.height);
        Matrix scaling = Matrix.scaling(2, 2);

        Matrix combined = translation.multiply(rotation).multiply(scaling).inverse();

        for (int u = 0; u < outData.width; u++) {
            for (int v = 0; v < outData.height; v++) {
                double[] targetCoords = new double[] {u, v, 1};
                double[] sourceCoords = combined.multiply(targetCoords);

                int pixel = Util.interpolate(inData, sourceCoords[0], sourceCoords[1], method);
                outData.setPixel(u, v, pixel);
            }
        }

        return outData;
    }
}
