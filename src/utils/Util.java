package utils;

import org.eclipse.swt.graphics.ImageData;

public class Util {

    public static int interpolate(ImageData imageData, double x, double y, int method) {
        switch (method) {
            case 0:
                return Util.interpolateNearestNeighbor(imageData, x, y);
            case 1:
                return Util.interpolateBilinear(imageData, x, y);
        }

        throw new IllegalArgumentException("Unknown interpolation method");
    }

    public static int interpolateNearestNeighbor(ImageData imageData, double x, double y) {
        int x2 = (int) Math.round(x);
        int y2 = (int) Math.round(y);

        if (x2 < 0 || x2 >= imageData.width || y2 < 0 || y2 >= imageData.height) {
            return 0;
        }

        return imageData.getPixel(x2, y2);
    }

    public static int interpolateBilinear(ImageData imageData, double x, double y) {
        int xLow = (int) Math.floor(x);
        int yLow = (int) Math.floor(y);
        int xHigh = (int) Math.ceil(x);
        int yHigh = (int) Math.ceil(y);

        int topLeft = -1;
        if (hasPixel(imageData, xLow, yLow)) {
            topLeft = imageData.getPixel(xLow, yLow);
        }

        int bottomLeft = -1;
        if (hasPixel(imageData, xLow, yHigh)) {
            bottomLeft = imageData.getPixel(xLow, yHigh);
        }

        int topRight = -1;
        if (hasPixel(imageData, xHigh, yLow)) {
            topRight = imageData.getPixel(xHigh, yLow);
        }

        int bottomRight = -1;
        if (hasPixel(imageData, xHigh, yHigh)) {
            bottomRight = imageData.getPixel(xHigh, yHigh);
        }

        double diffY = y - yLow;
        int left = interpolateBilinear(topLeft, bottomLeft, diffY);
        int right = interpolateBilinear(topRight, bottomRight, diffY);

        double diffX = x - xLow;
        int average = interpolateBilinear(left, right, diffX);
        return Math.max(average, 0);
    }

    private static int interpolateBilinear(int first, int second, double diff) {
        if (first >= 0 && second >= 0) {
            return averagePixel(first, second, diff);
        } else if (first >= 0) {
            return first;
        } else {
            return second;
        }
    }

    private static int averagePixel(int first, int second, double firstWeight) {
        int firstR = first & 0xFF0000;
        int firstG = first & 0xFF00;
        int firstB = first & 0xFF;
        int secondR = second & 0xFF0000;
        int secondG = second & 0xFF00;
        int secondB = second & 0xFF;

        double secondWeight = 1 - firstWeight;
        int averageR = (int) (firstR * firstWeight + secondR * secondWeight) & 0xFF0000;
        int averageG = (int) (firstG * firstWeight + secondG * secondWeight) & 0xFF00;
        int averageB = (int) (firstB * firstWeight + secondB * secondWeight) & 0xFF;
        return averageR | averageG | averageB;
    }

    public static boolean hasPixel(ImageData imageData, double x, double y) {
        if (x < 0 || y < 0) {
            return false;
        }

        if (x >= imageData.width || y >= imageData.height) {
            return false;
        }

        return true;
    }
}
