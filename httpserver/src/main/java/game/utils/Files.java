package game.utils;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import static com.google.common.base.Preconditions.checkArgument;

/** Created by wyt on 16-12-1. */
public class Files {

    public static byte[] fileToBytes(String filepath) {
        return fileToBytes(new File(filepath));
    }

    public static byte[] fileToBytes(File file) {
        try {
            return com.google.common.io.Files.toByteArray(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fileToStr(String filepath) {
        return fileToStr(new File(filepath));
    }

    public static String fileToStr(File file) {
        byte[] data = fileToBytes(file);
        return Bytes.toUnixFriendlyStr(data, file.getAbsolutePath());
    }

    public static Map<String, byte[]> dirToBytes(String dirpath, boolean recursively, boolean keepDir) {
        return dirToBytes(new File(dirpath), recursively, keepDir);
    }

    public static Map<String, byte[]> dirToBytes(File dir, boolean recursively, boolean keepDir) {
        return dirCollect(dir, Files::fileToBytes, recursively, keepDir);
    }

    public static Map<String, String> dirToStr(String dirpath, boolean recursively, boolean keepDir) {
        return dirToStr(new File(dirpath), recursively, keepDir);
    }

    public static Map<String, String> dirToStr(File dir, boolean recursively, boolean keepDir) {
        return dirCollect(dir, Files::fileToStr, recursively, keepDir);
    }

    public static <T> Map<String, T> dirCollect(File dir, Function<File, T> f, boolean recursively, boolean keepDir) {
        checkArgument(dir.isDirectory(), "给定的路径不是目录！%s", dir.getAbsolutePath());
        Map<String, T> r = Maps.newHashMap();
        dirCollect(dir, keepDir ? dir.getName() : "", r, f, recursively);
        return r;
    }

    private static <T> void dirCollect(File dir, String cdir, Map<String, T> r, Function<File, T> f, boolean recursively) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String path = Paths.join(cdir, file.getName());
            if (file.isFile()) {
                checkArgument(r.put(path, f.apply(file)) == null);
                continue;
            }
            if (recursively) {
                dirCollect(file, path, r, f, recursively);
            }
        }
    }

    public static void writeFile(byte[] data, String filepath) {
        writeFile(data, new File(filepath));
    }

    public static void writeFile(byte[] data, File file) {
        try {
            File pfile = file.getParentFile();
            if (pfile != null) {
                pfile.mkdirs();
            }
            com.google.common.io.Files.write(data, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
