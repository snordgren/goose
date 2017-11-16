package io.github.goose;

public class Pointer {
    private final Stack stack;
    private final int location;

    Pointer(Stack stack, int location, int size) {
        this.stack = stack;
        this.location = location;
        writeInt(size, -4);
    }

    /**
     * Frees the memory of this pointer, zeroing it.
     */
    void free() {
        int size = getSize();
        writeInt(0, -4);
        for (int i = 0; i < size; i++) {
            write(0, i);
        }
    }

    public int getHeaderLocation() {
        return location - 4;
    }

    public int getLocation() {
        return location;
    }

    public int getSize() {
        int size = 0x0;
        size |= (read(-4)) << 24;
        size |= (read(-3)) << 16;
        size |= (read(-2)) << 8;
        size |= (read(-1));
        return size;
    }

    public int read() {
        return read(0);
    }

    public int read(int offset) {
        return stack.getGC().getHeap()[location + offset] & 0xFF;
    }

    public boolean refersTo(int location) {
        return this.location == location;
    }

    public void write(int b) {
        write(b, 0);
    }

    public void write(int b, int offset) {
        stack.getGC().getHeap()[location + offset] = (byte) b;
    }

    public void writeInt(int i, int offset) {
        writeInt(stack.getGC().getHeap(), i, location + offset);
    }

    public static void writeInt(byte[] bytes, int i, int offset) {
        bytes[offset] = (byte) ((i & 0xFF000000) >> 24);
        bytes[offset + 1] = (byte) ((i & 0xFF0000) >> 16);
        bytes[offset + 2] = (byte) ((i & 0xFF00) >> 8);
        bytes[offset + 3] = (byte) (i & 0xFF);
    }
}
