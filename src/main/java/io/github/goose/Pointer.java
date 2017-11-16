package io.github.goose;

/**
 * A pointer to a segment of memory.
 */
public class Pointer {
    /**
     * The size of the header of each pointer.
     */
    public static final int HEADER_SIZE = 4;
    private final Stack stack;
    private final int address;
    private boolean valid = true;

    /**
     * Instantiate a new pointer belonging to a stack frame.
     *
     * @param stack The stack frame where this pointer was allocated.
     * @param address The address of the start of the header for this pointer.
     * @param size The amount of memory allocated to this pointer.
     */
    Pointer(Stack stack, int address, int size) {
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

    /**
     * Reads the size of this pointer from its header.
     *
     * @return The amount of memory allocated to this pointer.
     */
    public int getSize() {
        int size = 0x0;
        size |= (read(-4)) << 24;
        size |= (read(-3)) << 16;
        size |= (read(-2)) << 8;
        size |= (read(-1));
        return size;
    }

    /**
     * @return Whether this pointer is valid. Invalid pointers do not crash, but using
     * them means that your data may be written over by other pointers.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return Reads the byte at the start of this pointer's memory address.
     */
    public int read() {
        return read(0);
    }

    /**
     * Read a byte at an offset of this pointer.
     * @param offset The number of bytes beyond the start of this pointer's
     * allocated memory to read.
     * @return The read byte.
     */
    public int read(int offset) {
        return stack.getGC().getHeap()[address + offset] & 0xFF;
    }

    /**
     * Checks if this pointer refers to the specified address.
     *
     * @param address The address to check against.
     * @return True if this pointer is a pointer to the address.
     */
    public boolean refersTo(int address) {
        return this.address == address;
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

    /**
     * Writes an int to the memory pointed at by this pointer at a specific
     * offset.
     * @param i The value to write.
     * @param offset The offset to write it at.
     */
    public void writeInt(int i, int offset) {
        writeInt(stack.getGC().getHeap(), i, address + offset);
    }

    /**
     * Writes an int to a byte array.
     * @param bytes The byte array.
     * @param i The int to write.
     * @param offset The offset to write it at.
     */
    public static void writeInt(byte[] bytes, int i, int offset) {
        bytes[offset] = (byte) ((i & 0xFF000000) >> 24);
        bytes[offset + 1] = (byte) ((i & 0xFF0000) >> 16);
        bytes[offset + 2] = (byte) ((i & 0xFF00) >> 8);
        bytes[offset + 3] = (byte) (i & 0xFF);
    }
}
