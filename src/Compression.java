import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class Compression {
    private static int[][] image;
    static int[][] predicted;
    static int[][] decodedImg;
    static int[][] uniformQuantization;
    static int originalWidth;
    static int originalHeight;
    static int QStep = 32; // No of levels 16

    protected static void constructPredicted_Decoded() {
        predicted = new int[originalHeight][originalWidth];
        decodedImg = new int[originalHeight][originalWidth];

        if (image != null) {
            for (int i = 0; i < originalHeight; i++) {
                predicted[i][0] = image[i][0];
                decodedImg[i][0] = image[i][0];
            }

            for (int i = 0; i < originalWidth; i++) {
                predicted[0][i] = image[0][i];
                decodedImg[0][i] = image[0][i];
            }
        } else {
            // Handle the case when image is null
            System.out.println("Error: Image is null.");
        }
    }

    protected static void applyPredictiveCoding(int i, int j){
            int C = decodedImg[i][j - 1];
            int A = decodedImg[i - 1][j];
            int B = decodedImg[i - 1][j - 1];

            // Calculate the predicted value
            int predictedValue = predictValue(A, B, C);

            // Set the predicted value to the predicted array
            predicted[i][j] = predictedValue;
    }

    protected static int predictValue(int A, int B, int C) {
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
        uniformQuantization = new int[originalHeight - 1][originalWidth - 1];
        int mid = 0;
        int count = 0;
        for (int k = -255; k < 256; k += QStep){
            if (difference <= k + QStep && difference >= k) {
                uniformQuantization[i - 1][j - 1] = count;
                mid = (k + (k + QStep)) / 2;
                break;
            }
            count++;
        }
        return mid;
    }

    private static void writeCompressedFile(String filename) throws IOException {
        FileOutputStream outputFile = new FileOutputStream(filename);
        // originalHeight_originalWidth_Padding_firstRow_firstColumn_Quantized
        //     1 Byte    _   1 Byte    _ 1Byte _    1 Byte/cell     _ 4 bits/cell
        // height byte = originalHeight / 10
        // width byte = originalWidth / 10

        // Write Original Height Scaled - 1 Byte
        int height = originalHeight / 10;
        outputFile.write(height);

        // Write Original Width Scaled - 1 Byte
        int width = originalWidth / 10;
        outputFile.write(width);

        // Construct Quantized bytes to calculate padding
        StringBuilder quantized = new StringBuilder(convertQuantizedToStream());
        int padding = 8 - (quantized.length() % 8);
        quantized.append("0".repeat(padding));

        // Write Padding Size
        outputFile.write(padding);

        // Write First Row of Image
        for (int i = 0; i < originalHeight; i++){
            outputFile.write(image[i][0]);
        }

        // Write First Column of Image
        for (int i = 1; i < originalWidth; i++){
            outputFile.write(image[0][i]);
        }

        // Write Uniform Quantization
        for (int i = 0; i < quantized.length(); i += 8){
            String streamByte = quantized.substring(i, i + 8);
            short value = Short.parseShort(streamByte, 2);
            outputFile.write(value);
        }

        outputFile.close();
    }

    private static String convertQuantizedToStream(){
        StringBuilder stream = new StringBuilder();
        for (int i = 0; i < originalHeight - 1; i++){
            for (int j = 0; j < originalWidth - 1; j++){
                String bits = String.format("%4s", Integer.toBinaryString(uniformQuantization[i][j] & 0xFF)).replace(' ', '0');
                stream.append(bits);
            }
        }
        return String.valueOf(stream);
    }

    public static void Compress(String inputPath, String outPath) throws IOException {
        image = Image.readImage(inputPath);

        originalHeight = Image.height;
        originalWidth = Image.width;

        // Construct first row and first colum in both predicted and decodedImg
        constructPredicted_Decoded();

        for (int i = 1; i < originalHeight; i++){
            for (int j = 1; j < originalWidth; j++){
                applyPredictiveCoding(i, j);
                assert image != null;
                int difference = image[i][j] - predicted[i][j];
                int dequantized = getDequantized(difference, i, j);
                if (predicted[i][j] + dequantized < 0){
                    decodedImg[i][j] = 0;
                    continue;
                }
                decodedImg[i][j] = predicted[i][j] + dequantized;
            }
        }
        writeCompressedFile(outPath);
//        Image.writeImage(decodedImg, originalWidth, originalHeight, "R_Decompressed.jpg");
    }
}
