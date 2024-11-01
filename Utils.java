import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class Utils {
    private static String getAlphanumeric(int count) {
        Random r = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int n = r.nextInt(36);
            char c;
            if(n < 10) {
                c = (char) (n + 48);
            } else {
                c = (char) (n + 55);
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    private static byte[] concat_bytes(byte[] personalIdB, byte[] personalImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(personalIdB.length + personalImage.length);
        baos.write(personalIdB);
        baos.write(personalImage);
        return baos.toByteArray();
    }

    private static byte[] int_to_bytes(int i) {
        return ByteBuffer.allocate(4).putInt(93).array();
    }

    private static int bytes_to_int(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }

    private static File createFileAndSubfolder(String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        f.createNewFile();
        return f;
    }
}
