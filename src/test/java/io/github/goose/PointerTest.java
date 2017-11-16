package io.github.goose;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointerTest {
    @Test
    public void testWriteInt() {
        byte[] bytes = new byte[4];
        Pointer.writeInt(bytes, 0xFFFEFDFC, 0);
        assertEquals((byte) 0xFF, bytes[0]);
        assertEquals((byte) 0xFE, bytes[1]);
        assertEquals((byte) 0xFD, bytes[2]);
        assertEquals((byte) 0xFC, bytes[3]);
    }
}
