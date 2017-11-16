package io.github.goose;

public class Pointer {
    public static final int HEADER_SIZE = 4;
    private final Stack stack;
    private final int address;
    private boolean valid = true;

    Pointer(Stack stack, int address, int size) {
        stack.register(this);
        this.stack = stack;
        this.address = address + HEADER_SIZE;
        writeInt(size, -HEADER_SIZE);
    }

    /**
     * Sets every byte of this pointer to the value. Example usage is
     * <code>fill(0)</code> to zero a pointer.
     *
     * @param value The value to fill every byte of this pointer with.
     */
    public void fill(int value) {
        int size = getSize();
        for (int i = 0; i < size; i++) {
            write(value, i);
        }
    }

    /**
     * Frees the memory of this pointer, and zeroes it.
     */
    void free() {
        fill(0);
        writeInt(0, -HEADER_SIZE);
        valid = false;
    }

    /**
     * @return The start address of the headers of this pointer.
     */
    public int getHeaderLocation() {
        return address - HEADER_SIZE;
    }

    /**
     * @return The start address of the data of this pointer.
     */
    public int getAddress() {
        return address;
    }

    public int getSize() {
        int size = 0x0;
        size |= (read(-4)) << 24;
        size |= (read(-3)) << 16;
        size |= (read(-2)) << 8;
        size |= (read(-1));
        return size;
    }

    public boolean isValid() {
        return valid;
    }

    public int read() {
        return read(0);
    }

    public int read(int offset) {
        return stack.getGC().getHeap()[address + offset] & 0xFF;
    }

    public boolean refersTo(int location) {
        return this.address == location;
    }

    /**
     * Writes a byte to the first byte pointed at by this pointer.
     *
     * @param b The byte to write.
     */
    public void write(int b) {
        write(b, 0);
    }

    /**
     * Writes a byte at an offset of this pointer.
     *
     * @param b The byte to write.
     * @param offset The offset.
     */
    public void write(int b, int offset) {
        stack.getGC().getHeap()[address + offset] = (byte) b;
    }

    public void writeInt(int i, int offset) {
        writeInt(stack.getGC().getHeap(), i, address + offset);
    }

    public static void writeInt(byte[] bytes, int i, int offset) {
        bytes[offset] = (byte) ((i & 0xFF000000) >> 24);
        bytes[offset + 1] = (byte) ((i & 0xFF0000) >> 16);
        bytes[offset + 2] = (byte) ((i & 0xFF00) >> 8);
        bytes[offset + 3] = (byte) (i & 0xFF);
    }
}
