package utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.LocalDate;
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

    public static byte[] concat_bytes(byte[] personalIdB, byte[] personalImage, byte[] handSignature) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(personalIdB.length + personalImage.length);
        baos.write(personalIdB);
        baos.write(personalImage);
        baos.write(handSignature);
        return baos.toByteArray();
    }

    public static byte[] int_to_bytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
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

    public static String stringArrayToLines(String[] dynamicAttributes) {
        StringBuilder sb = new StringBuilder();
        for(String s: dynamicAttributes) {
            sb.append(s);
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String[] bytesToStringArray(byte[] data) {
        if(data.length == 0) {
            return new String[0];
        } else {
            return new String(data).split("\n");
        }
    }

    public static String today() {
        LocalDate localDate = LocalDate.now();
        return localDate.getDayOfMonth() + "." + localDate.getMonthValue() + "." + localDate.getYear();
    }

    public static boolean validateStringDate(String date) {
        String[]s = date.split("\\.");
        if(s.length != 3)
            return false;
        int d;
        int m;
        int y;
        try {
            d = Integer.parseInt(s[0]);
            m = Integer.parseInt(s[1]);
            y = Integer.parseInt(s[2]);
        } catch (NumberFormatException e) {
            return false;
        }
        int[]daysOfMonth = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
        if(y % 4 == 0 && m == 2 && d == 29)
            return true;
        else return d <= daysOfMonth[m - 1];
    }

    public static String[] sliceStringArray(String[] s, int start, int end) {
        String[] res = new String[end - start];
        for (int i = 0; i < res.length; i++) {
            res[i] = s[i + start];
        }
        return res;
    }

    public static class LineWriter {
        public final ByteArrayOutputStream baos;
        public LineWriter() {
            baos = new ByteArrayOutputStream();
        }

        public void write(String s) throws IOException {
            baos.write(s.getBytes());
            baos.write('\n');
        }

        public byte[] get_bytes() throws IOException {
            baos.close();
            return baos.toByteArray();
        }

        public void write_byte(byte[] b) throws IOException {
            baos.write(b);
        }
    }

    public static class SliceReader {
        private final Readable readable;
        public SliceReader(Readable readable) {
            this.readable = readable;
        }

        public byte[] next() throws IOException {
            int len = nextInt();
            byte[] data = new byte[len];
            readable.read(data, len);
            return data;
        }

        private int nextInt() throws IOException {
            byte[] len_personal_id_b = new byte[4];
            readable.read(len_personal_id_b, 4);
            return Utils.bytes_to_int(len_personal_id_b);
        }
    }

    public static class SliceWriter {
        private final Writable writable;
        public SliceWriter(Writable writable) {
            this.writable = writable;
        }

        public void write(byte[] b) throws IOException {
            writable.write(int_to_bytes(b.length));
            writable.write(b);
        }
    }

    public interface Writable {
        void write(byte[] data) throws IOException;
    }

    public interface Readable {
        void read(byte[] data, int length) throws IOException;
    }
}
