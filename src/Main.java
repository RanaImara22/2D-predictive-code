import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Compression.Compress("R.jpg", "compressedFile.bin");
        Decompression.decompress("compressedFile.bin", "R_Decompressed.jpg");
    }
}