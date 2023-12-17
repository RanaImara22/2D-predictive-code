import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        try {
            // Load the image
            BufferedImage originalImage = ImageIO.read(new File("path/to/your/image.jpg"));

            // Compression
            Compression compression = new Compression();
            int[][] compressedData = compression.compress(originalImage);

            // Decompression
            Decompression decompression = new Decompression();
            BufferedImage decompressedImage = decompression.decompress(compressedData);

            // Save the decompressed image
            ImageIO.write(decompressedImage, "jpg", new File("path/to/your/decompressed_image.jpg"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Compression {
    public int[][] compress(BufferedImage originalImage) {
        // Step 1: Convert the BufferedImage to a 2D array of integers
        int[][] pixels = convertImageToPixels(originalImage);

        // Step 2: Apply 2D predictive coding to the pixel values
        int[][] predictedPixels = applyPredictiveCoding(pixels);

        // Step 3: Calculate the difference 2D array
        int[][] differencePixels = calculateDifference(pixels, predictedPixels);

        // Step 4: Apply uniform quantization
        int quantizationStep = 10; // You can adjust this parameter based on your needs
        int[][] quantizedDifference = applyQuantization(differencePixels, quantizationStep);

        return quantizedDifference;
    }

    private int[][] applyPredictiveCoding(int[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        int[][] predictedPixels = new int[width][height];

        // Initialize the first row and column of the predicted array with the original image
        for (int i = 0; i < width; i++) {
            predictedPixels[i][0] = pixels[i][0];
        }

        for (int j = 0; j < height; j++) {
            predictedPixels[0][j] = pixels[0][j];
        }

        // Apply predictive coding to the rest of the pixels
        for (int i = 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                int A = predictedPixels[i][j - 1];
                int B = predictedPixels[i - 1][j];
                int C = predictedPixels[i - 1][j - 1];

                // Calculate the predicted value
                int predictedValue = predictValue(A, B, C);

                // Set the predicted value to the predictedPixels array
                predictedPixels[i][j] = predictedValue;
            }
        }

        return predictedPixels;
    }

    private int predictValue(int A, int B, int C) {
        int minAC = Math.min(A, C);
        int maxAC = Math.max(A, C);

        if (B <= minAC) {
            return maxAC;
        } else if (B >= maxAC) {
            return minAC;
        } else {
            return A + C - B;
        }
    }

    private int[][] calculateDifference(int[][] pixels, int[][] predictedPixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        int[][] difference = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                difference[i][j] = pixels[i][j] - predictedPixels[i][j];
            }
        }

        return difference;
    }

    private int[][] applyQuantization(int[][] differencePixels, int quantizationStep) {
        int width = differencePixels.length;
        int height = differencePixels[0].length;
        int[][] quantizedDifference = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                quantizedDifference[i][j] = differencePixels[i][j] / quantizationStep;
            }
        }

        return quantizedDifference;
    }

    private int[][] convertImageToPixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixels = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = image.getRGB(i, j);
            }
        }

        return pixels;
    }
}

class Decompression {
    public BufferedImage decompress(int[][] compressedData) {
        // Step 1: Decode the quantized difference 2D array
        int[][] dequantizedDifference = decodeQuantization(compressedData);

        // Step 2: Predict the pixels using the dequantized difference
        int[][] predictedPixels = applyPredictiveCoding(dequantizedDifference);

        // Step 3: Reconstruct the original image
        int[][] reconstructedPixels = reconstructImage(predictedPixels, dequantizedDifference);

        // Step 4: Convert the 2D array of integers back to a BufferedImage
        return convertPixelsToImage(reconstructedPixels);
    }

    private int[][] decodeQuantization(int[][] compressedData) {
        int width = compressedData.length;
        int height = compressedData[0].length;
        int[][] dequantizedDifference = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                dequantizedDifference[i][j] = compressedData[i][j] * 10; // Assuming quantizationStep is 10
            }
        }

        return dequantizedDifference;
    }

    private int[][] applyPredictiveCoding(int[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;

        // Initialize the first row and column of the predicted array with the original image
        int[][] predictedPixels = new int[width][height];
        for (int i = 0; i < width; i++) {
            predictedPixels[i][0] = pixels[i][0];
        }

        for (int j = 0; j < height; j++) {
            predictedPixels[0][j] = pixels[0][j];
        }

        // Apply predictive coding to the rest of the pixels
        for (int i = 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                int A = predictedPixels[i][j - 1];
                int B = predictedPixels[i - 1][j];
                int C = predictedPixels[i - 1][j - 1];

                // Calculate the predicted value
                int predictedValue = predictValue(A, B, C);

                // Set the predicted value to the predictedPixels array
                // Set the predicted value to the predictedPixels array
                predictedPixels[i][j] = predictedValue;
            }
        }

        return predictedPixels;
    }

    private int predictValue(int A, int B, int C) {
        int minAC = Math.min(A, C);
        int maxAC = Math.max(A, C);

        if (B <= minAC) {
            return maxAC;
        } else if (B >= maxAC) {
            return minAC;
        } else {
            return A + C - B;
        }
    }

    private int[][] reconstructImage(int[][] predictedPixels, int[][] dequantizedDifference) {
        int width = predictedPixels.length;
        int height = predictedPixels[0].length;
        int[][] reconstructedPixels = new int[width][height];

        // Keep the first row and column as the original image
        for (int i = 0; i < width; i++) {
            reconstructedPixels[i][0] = predictedPixels[i][0];
        }

        for (int j = 0; j < height; j++) {
            reconstructedPixels[0][j] = predictedPixels[0][j];
        }

        // Reconstruct the rest of the pixels using the dequantized difference
        for (int i = 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                int A = reconstructedPixels[i][j - 1];
                int B = reconstructedPixels[i - 1][j];
                int C = reconstructedPixels[i - 1][j - 1];

                // Calculate the predicted value
                int predictedValue = predictValue(A, B, C);

                // Add the dequantized difference to reconstruct the pixel
                reconstructedPixels[i][j] = predictedValue + dequantizedDifference[i][j];
            }
        }

        return reconstructedPixels;
    }

    private BufferedImage convertPixelsToImage(int[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image.setRGB(i, j, pixels[i][j]);
            }
        }

        return image;
    }
}

