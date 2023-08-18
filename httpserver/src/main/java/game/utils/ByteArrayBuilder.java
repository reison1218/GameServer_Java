package game.utils;

import java.util.Arrays;

public class ByteArrayBuilder {

    private byte[] data;

    private int size;

    public ByteArrayBuilder() {
        this(16);
    }

    public ByteArrayBuilder(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative");
        }
        data = new byte[initialCapacity];
    }

    public ByteArrayBuilder append(byte[] toAdd) {
        if (toAdd == null || toAdd.length == 0) {
            return this;
        }
        ensureCapacity(size + toAdd.length);
        System.arraycopy(toAdd, 0, data, size, toAdd.length);
        size += toAdd.length;
        return this;
    }

    public ByteArrayBuilder appendInt(int value) {
        ensureCapacity(size + 4);
        data[size++] = (byte) (value >> 24);
        data[size++] = (byte) (value >> 16);
        data[size++] = (byte) (value >> 8);
        data[size++] = (byte) (value);
        return this;
    }

    public ByteArrayBuilder appendShort(int value) {
        ensureCapacity(size + 2);
        data[size++] = (byte) (value >> 8);
        data[size++] = (byte) (value);
        return this;
    }

    public ByteArrayBuilder appendSingleByte(byte b) {
        ensureCapacity(size + 1);
        data[size++] = b;
        return this;
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    /**
     * ���������ݵĲ���. �ı䷵�ص����ݲ���Ӱ���κζ���
     *
     * @return
     */
    public byte[] toByteArray() {
        return Arrays.copyOf(data, size);
    }

    /**
     * ���ر��������, �ı䷵�ص����ݻ�Ӱ���Ժ󷵻ص�����
     *
     * @return
     */
    public byte[] array() {
        return data;
    }

}
