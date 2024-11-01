import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class Utils {
    public static String getAlphanumeric(int count) {
        Random r = new Random();
        StringBuilder stringBuilder = new StringBuilder(count);
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

    public static byte[] concat_bytes(byte[] personalIdB, byte[] personalImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(personalIdB.length + personalImage.length);
        baos.write(personalIdB);
        baos.write(personalImage);
        return baos.toByteArray();
    }

    public static byte[] int_to_bytes(int i) {
        return ByteBuffer.allocate(4).putInt(93).array();
    }

    public static int bytes_to_int(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }

    public static File createFileAndSubfolder(String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        f.createNewFile();
        return f;
    }

    public static class SliceIterator {
        private final FileInputStream fileInputStream;
        public SliceIterator(FileInputStream fileInputStream) {
            this.fileInputStream = fileInputStream;
        }

        public byte[] next() throws IOException {
            int len = nextInt();
            byte[] data = new byte[len];
            fileInputStream.read(data, 0, len);
            return data;
        }

        private int nextInt() throws IOException {
            byte[] len_personal_id_b = new byte[4];
            fileInputStream.read(len_personal_id_b, 0, 4);
            return Utils.bytes_to_int(len_personal_id_b);
        }

        public void close() throws IOException {
            fileInputStream.close();
        }
    }
}
