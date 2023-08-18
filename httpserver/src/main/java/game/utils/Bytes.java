package game.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Utf8;
import com.google.common.io.ByteStreams;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.input.UnixLineEndingInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/** Created by wyt on 16-12-1. */
public class Bytes {

    public static String toUnixFriendlyStr(byte[] data, String desc) {
        checkArgument(Utf8.isWellFormed(data), "文件编码不是utf8，请手动将文件转成utf8编码！%s", desc);
        data = of(data).trimUtf8Bom().ensureLineEndingIsLF().toArray();
        return new String(data, Charsets.UTF_8);
        //        return CharSetConvertor.convertBytes(data, desc);
    }

    public static Bytes of(byte[] data) {
        checkNotNull(data);
        return new Bytes(new ByteArrayInputStream(data));
    }

    private final InputStream is;

    private Bytes(InputStream is) {
        this.is = is;
    }

    public Bytes trimUtf8Bom() {
        return new Bytes(new BOMInputStream(is));
    }

    public Bytes ensureLineEndingIsLF() {
        return new Bytes(new UnixLineEndingInputStream(is, false));
    }

    public byte[] toArray() {
        try {
            return ByteStreams.toByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
