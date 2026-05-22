package dk.easv.tiffixexamweblager.BLL.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void hasBarcode_returnsFalse_whenFileDoesNotExist() {
        File missing = new File("nonexistent.tiff");
        assertFalse(BarcodeDetector.hasBarcode(missing));
    }

    @Test
    void hasBarcode_returnsFalse_whenImageHasNoBarcode() throws Exception {
        // a blank white image with no barcode
        BufferedImage blank = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        File tiff = tempDir.resolve("blank.tiff").toFile();
        ImageIO.write(blank, "tiff", tiff);

        assertFalse(BarcodeDetector.hasBarcode(tiff));
    }

    @Test
    void hasBarcode_returnsTrue_whenImageContainsBarcode() throws Exception {
        // generate a real qre code using zxing (already on the classpath)
        var matrix = new MultiFormatWriter().encode("TEST-123", BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(matrix);

        File tiff = tempDir.resolve("barcode.tiff").toFile();
        ImageIO.write(barcodeImage, "tiff", tiff);

        assertTrue(BarcodeDetector.hasBarcode(tiff));
    }
}