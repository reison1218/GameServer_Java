package game.utils;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class CommonUtils {

    public static final int CORE_NUM = Runtime.getRuntime().availableProcessors();
    private static final Random rand = new Random(System.currentTimeMillis());

    protected CommonUtils() {
    }

    public static byte[] randomByteArray(int len) {
        byte[] result = new byte[len];
        rand.nextBytes(result);
        return result;
    }

    public static byte[] readFile(String s) throws IOException {
        return readFile(new File(s));
    }

    public static byte[] readFile(File file) throws IOException {
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        return Files.toByteArray(file);
    }

    /**
     * 将当前流中能读入的数据全部读入
     * 调用者自己关闭流
     * @param in
     * @return
     */
    public static byte[] readFile(InputStream in) {
        if (in == null) {
            return null;
        }
        try {
            return ByteStreams.toByteArray(in);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readGzipFile(File file) {
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        try (InputStream in = new FileInputStream(file); GZIPInputStream is = new GZIPInputStream(in)) {
            ByteArrayBuilder builder = new ByteArrayBuilder(8192 * 1024);

            byte abyte0[] = new byte[2048 * 1024];
            int length = 0;
            while ((length = is.read(abyte0)) != -1) {
                builder.append(Arrays.copyOf(abyte0, length));
            }
            return builder.toByteArray();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param inputStream
     * @return
     */
    public static byte[] readGzipFile(InputStream inputStream) {
        try (GZIPInputStream is = new GZIPInputStream(inputStream)) {
            ByteArrayBuilder builder = new ByteArrayBuilder(8192 * 1024);

            byte abyte0[] = new byte[2048 * 1024];
            int length = 0;
            while ((length = is.read(abyte0)) != -1) {
                builder.append(Arrays.copyOf(abyte0, length));
            }
            return builder.toByteArray();
        } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
        }
    }

    public static byte[] loadFileFromClassPath(String sourcePath) throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(sourcePath)) {
            if (is == null) {
                return null;
            }

            return ByteStreams.toByteArray(is);
        }
    }

    /**
     * 从ClassLoader查找指定资源文件
     * @param sourcePath  资源路径
     * @return byte[]
     * @throws IOException
     */
    public static InputStream getInputStreamFromClassPath(String sourcePath) throws IOException {
        assert sourcePath != null && sourcePath.length() > 0;
        return ClassLoader.getSystemResourceAsStream(sourcePath);
    }

    public static int getHighShort(int i) {
        return i >>> 16;
    }

    public static int getLowShort(int i) {
        return i & 0xffff;
    }

    public static int short2Int(int high, int low) {
        return (high << 16) | low;
    }

    public static long int2Long(int high, int low) {
        return (((long) high) << 32) | (low & 0xFFFFFFFFL);
    }

    public static int getLowInt(long l) {
        return (int) l;
    }

    public static int getHighInt(long l) {
        return (int) (l >>> 32);
    }

    /**
     * 获得第一个比x大的2的倍数. 如果x是0, 返回也是0
     *
     * @param x
     * @return
     */
    public static int getClosestPowerOf2(int x) {
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        x++;
        return x;
    }

    public static boolean isPowerOf2(int x) {
        return (x != 0) && ((x & (x - 1)) == 0);
    }

    public static long calculateTPS(int count, long nanos) {
        return (count * 10_0000_0000L) / nanos;
    }

    public static String getStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        StringBuilder sb = new StringBuilder(128);
        for (StackTraceElement e : trace) {
            sb.append("\n\tat ");
            sb.append(e);
        }
        return sb.toString();
    }

    public static boolean isAllEmpty(int[] array) {
        for (int v : array) {
            if (v != 0) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAllEmpty(String[] array) {
        for (String s : array) {
            if (!StringUtils.isEmpty(s)) {
                return false;
            }
        }

        return true;
    }
}
