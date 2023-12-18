import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Decompression extends Compression{
    static int padding;

    private static void readInputFile(String inputPath) {
        try (FileInputStream inputFile = new FileInputStream(inputPath)) {
            // originalHeight_originalWidth_Padding_firstRow_firstColumn_Quantized

            // Read Original Height
            originalHeight = inputFile.read() * 10;

            // Read Original Width
            originalWidth = inputFile.read() * 10;

            // Read Padding size
            padding = inputFile.read();

            // Read First Row and First Column
            readFirstRow_Column(inputFile);

            // Read Uniform Quantization
            readQuantized(inputFile);

        } catch (IOException e) {
            System.out.println("An Error Occurred while reading the file.");
        }
    }

    private static void readFirstRow_Column(FileInputStream file) throws IOException {
        decodedImg = new int[originalHeight][originalWidth];

        // Read First Row
        for (int i = 0; i < originalHeight; i++){
            decodedImg[i][0] = file.read();
        }

        // Read First Column
        for (int i = 1; i < originalWidth; i++){
            decodedImg[0][i] = file.read();
        }
    }

    private static void readQuantized(FileInputStream file) throws IOException {
        uniformQuantization = new int[originalHeight - 1][originalWidth - 1];
        StringBuilder quantizedBytes = new StringBuilder();
        int bytesRead;
        byte[] buffer = new byte[1024];

        while ((bytesRead = file.read(buffer)) != -1){
            for (int i = 0; i < bytesRead; i++){
                // Convert each byte to its binary representation and append to the compressed text
                String bits = String.format("%8s", Integer.toBinaryString(buffer[i] & 0xFF)).replace(' ', '0');
                quantizedBytes.append(bits);
            }
        }

        // Remove the padding bits from the end of the compressed text
        if (padding != 8){
            quantizedBytes.setLength(quantizedBytes.length() - padding);
        }

        int k = 0;
        for (int i = 0; i < originalHeight - 1; i++) {
            for (int j = 0; j < originalWidth - 1; j++) {
                String streamByte = quantizedBytes.substring(k, k + 4);
                uniformQuantization[i][j] = Integer.parseInt(streamByte, 2);
                k += 4;
            }
        }
    }

    private static void constructPredicted(){
        predicted = new int[originalHeight][originalWidth];
        for (int i = 0; i < originalHeight; i++) {
            predicted[i][0] = decodedImg[i][0];
        }

        for (int i = 0; i < originalWidth; i++) {
            predicted[0][i] = decodedImg[0][i];
        }
    }

    private static int getDequantized(int quantizedValue){
        int count = 0;
        int mid = 0;
        for (int i = -255; i < 256; i += QStep){
            if (count == quantizedValue){
                mid = (i + (i + QStep)) / 2;
                break;
            }
            count++;
        }
        return mid;
    }

    public static boolean decompress(String inputFile, String outputFile){
        readInputFile(inputFile);

        constructPredicted();

        for (int i = 1; i < originalHeight; i++){
            for (int j = 1; j < originalWidth; j++){
                applyPredictiveCoding(i, j);
                int dequantized = getDequantized(uniformQuantization[i - 1][j - 1]);
//                if (predicted[i][j] + dequantized < 0){
//                    decodedImg[i][j] = 0;
//                    continue;
//                }
                decodedImg[i][j] = predicted[i][j] + dequantized;
            }
        }
        Image.writeImage(decodedImg, originalWidth, originalHeight, outputFile);
        return true;
    }


}
