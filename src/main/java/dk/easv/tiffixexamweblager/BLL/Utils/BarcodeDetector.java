package dk.easv.tiffixexamweblager.BLL.Utils;

// ZXing imports
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

// Java imports
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class BarcodeDetector {
    public static boolean hasBarcode(File tiffFile) {
        try {
            BufferedImage image = ImageIO.read(tiffFile);
            if (image == null)
                return false;

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            new MultiFormatReader().decode(bitmap);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
