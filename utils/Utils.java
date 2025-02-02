package utils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

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
        System.arraycopy(s, 0 + start, res, 0, res.length);
        return res;
    }

    public static boolean exists(String url) {
        return new File(url).exists();
    }

    public static boolean dateAfter(String d1, String d2, boolean orEquals) throws ParseException {
        String pattern = "dd.MM.yyyy";
        Date date1 = new SimpleDateFormat(pattern).parse(d1);
        Date date2 = new SimpleDateFormat(pattern).parse(d2);
        return date2.after(date1) || (orEquals && date2.equals(date1));
    }

    public static int daysBetween(String d1, String d2) throws ParseException {
        String pattern = "dd.MM.yyyy";
        Date date1 = new SimpleDateFormat(pattern).parse(d1);
        Calendar calendar1 = new GregorianCalendar();
        calendar1.setTime(date1);

        Date date2 = new SimpleDateFormat(pattern).parse(d2);
        Calendar calendar2 = new GregorianCalendar();
        calendar2.setTime(date2);

        int dayOfYear1 = calendar1.get(GregorianCalendar.DAY_OF_YEAR);
        int dayOfYear2 = calendar2.get(GregorianCalendar.DAY_OF_YEAR);
        int year1 = calendar1.get(GregorianCalendar.YEAR);
        int year2 = calendar2.get(GregorianCalendar.YEAR);
        int year_diff = year2 - year1;
        int daysBetweenYears = 0;
        if(year_diff > 0){
            for (int i = year1; i < year2; i++) {
                if(i % 4 == 0) {
                    daysBetweenYears += 366;
                } else {
                    daysBetweenYears += 365;
                }
            }
        } else {
            for (int i = year1; i > year2; i--) {
                if(i % 4 == 0) {
                    daysBetweenYears -= 366;
                } else {
                    daysBetweenYears -= 365;
                }
            }
        }
        return daysBetweenYears + (dayOfYear2 - dayOfYear1);
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

        public void write_byte(byte[] b) throws IOException {
            baos.write(b);
        }

        public byte[] get_bytes() throws IOException {
            baos.close();
            return baos.toByteArray();
        }
    }

    public static class SliceReader {
        private final InputStream inputStream;
        public SliceReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public byte[] next() throws IOException {
            int len = nextInt();
            byte[] data = new byte[len];
            inputStream.read(data, 0, len);
            return data;
        }

        private int nextInt() throws IOException {
            byte[] len_personal_id_b = new byte[4];
            inputStream.read(len_personal_id_b, 0, 4);
            return Utils.bytes_to_int(len_personal_id_b);
        }
    }

    public static class SliceWriter {
        private final OutputStream outputStream;
        public SliceWriter(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public void write(byte[] b) throws IOException {
            outputStream.write(int_to_bytes(b.length));
            outputStream.write(b);
        }
    }

    public static class AES_InputStream extends InputStream {
        private final InputStream inputStream;
        public final int buf_len;
        private byte[] buf;
        private int buf_position = 0;
        public AES_InputStream(InputStream inputStream, int buf_len) {
            this.inputStream = inputStream;
            this.buf_len = buf_len;
        }
        @Override
        public int read() throws IOException {
            byte[]b = new byte[1];
            inputStream.read(b, 0, 4);
            return ByteBuffer.wrap(b).getInt();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if(buf == null) {
                buf = new byte[buf_len];
                inputStream.read(buf, 0, buf_len);
            }
            int b_position = 0;

            while(b_position < b.length) {
                int buf_remaining = buf_len - buf_position;
                int b_remaining = b.length - b_position;

                if(b_remaining < buf_remaining) {
                    System.arraycopy(buf, buf_position, b, b_position, b_remaining);
                    b_position += b_remaining;
                    buf_position += b_remaining;
                } else if (b_remaining == buf_remaining) {
                    System.arraycopy(buf, buf_position, b, b_position, b_remaining);
                    b_position += b_remaining;
                    inputStream.read(buf, 0, buf_len);
                    buf_position = 0;
                } else {
                    int chunk_size = buf_len - buf_position;
                    System.arraycopy(buf, buf_position, b, b_position, chunk_size);
                    inputStream.read(buf, 0, buf_len);
                    buf_position = 0;
                    b_position += chunk_size;
                }
            }

            return 0;
        }
    }

    public static class AES_OutputStream extends OutputStream {
        private final OutputStream outputStream;
        private byte[]buf;
        private int buf_position = 0;
        public AES_OutputStream(OutputStream outputStream, int buf_size) {
            this.outputStream = outputStream;
            this.buf = new byte[buf_size];
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(new byte[]{Integer.valueOf(b).byteValue()});
        }

        @Override
        public void write(byte[]b) throws IOException {
            int buf_len = buf.length;
            for (int start = 0; start < b.length; start += buf_len) {
                int end;
                if(b.length - start > buf_len) {
                    end = start + buf_len;
                } else {
                    end = b.length;
                }

                int chunk_len = end - start;
                int remaining_size = buf_len - buf_position;
                if(remaining_size > chunk_len) {
                    System.arraycopy(b, start, buf, buf_position, chunk_len);
                    buf_position += chunk_len;
                } else if (remaining_size == chunk_len) {
                    System.arraycopy(b, start, buf, buf_position, chunk_len);
                    outputStream.write(buf);
                    buf_position = 0;
                } else {
                    int overflow_pos = start + remaining_size;
                    System.arraycopy(b, start, buf, buf_position, overflow_pos);
                    outputStream.write(buf);
                    buf_position = 0;
                    System.arraycopy(b, overflow_pos, buf, buf_position, chunk_len - overflow_pos);
                }
            }
        }

        @Override
        public void close() throws IOException {
            outputStream.write(buf, 0, buf_position);
            outputStream.close();
            super.close();
        }
    }
}
