import java.io.IOException;
import java.util.Vector;

public class Compression {
    static int[][] image;
    static int[][] predicted;
    static int[][] decodedImg;
    static int[][] uniformQuantization;
    static int originalWidth;
    static int originalHeight;

    int QStep = 32; // No of levels 16

    public static void constructPredicted_Decoded(String Path) {
        predicted = new int[originalWidth][originalHeight];
        decodedImg = new int[originalWidth][originalHeight];

        if (image != null) {
            for (int i = 0; i < originalWidth; i++) {
                predicted[i][0] = image[i][0];
                decodedImg[i][0] = image[i][0];
            }

            for (int i = 0; i < originalHeight; i++) {
                predicted[0][i] = image[0][i];
                decodedImg[0][i] = image[0][i];
            }
        } else {
            // Handle the case when image is null
            System.out.println("Error: Image is null.");
        }
    }

    public static void applyPredictiveCoding(int i, int j){
            int C = decodedImg[i][j - 1];
            int A = decodedImg[i - 1][j];
            int B = decodedImg[i - 1][j - 1];

            // Calculate the predicted value
            int predictedValue = predictValue(A, B, C);

            // Set the predicted value to the predicted array
            predicted[i][j] = predictedValue;
    }

    private static int predictValue(int A, int B, int C) {
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
    //uniform quantization
    private static int getDequantized(int difference, int i, int j){
        uniformQuantization = new int[originalWidth - 1][originalHeight - 1];
        int mid = 0;
        for (int k = -255; k < 256; k += 32){
            if (difference <= i + 32 && difference >= i) {
                uniformQuantization[i - 1][j - 1] = i;
                mid = ((2 * i) + 32) / 2;
                break;
            }
        }
        return mid;
    }

    public static void Compress(String Path) {
        image = Image.readImage(Path);

        originalHeight = Image.height;
        originalWidth = Image.width;

        // Construct first row and first colum in both predicted and decodedImg
        constructPredicted_Decoded(Path);

        for (int i = 1; i < originalWidth; i++){
            for (int j = 1; j < originalHeight; j++){
                applyPredictiveCoding(i, j);
                assert image != null;
                int difference = image[i][j] - predicted[i][j];
                int dequantized = getDequantized(difference, i, j);
                decodedImg[i][j] = predicted[i][j] + dequantized;
            }
        }
        Image.writeImage(decodedImg, originalWidth, originalHeight, "R_Decompressed.jpg");
    }
}
