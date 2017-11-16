package io.github.goose;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GCTest {
    @Test
    public void testAllocate() {
        GC gc = new GC(1024, 8);
        gc.enterStackFrame();
        Pointer p = gc.allocate(4);
        assertEquals(4, p.getSize());

        p.write(0xFF);
        assertEquals(0xFF, p.read());

        Pointer p2 = gc.allocate(3);
        assertEquals(3, p2.getSize());
        p2.write(0xF0);
        assertEquals(0xF0, p2.read());
    }

    @Test
    public void testFillHeap() {
        int baseSize = 4;
        int realSize = baseSize + 4;
        int pointerCount = 8;
        GC gc = new GC(realSize * pointerCount, 8);
        List<Pointer> pointerList = new ArrayList<>();
        for (int i = 0; i < pointerCount; i++) {
            Pointer allocated = gc.allocate(baseSize);
            assertNotNull(allocated);
            pointerList.add(allocated);
        }
    }

    @Test
    public void testCollect() {
        GC gc = new GC(1024, 8);
        gc.enterStackFrame();
        Pointer p = gc.allocate(4);
        assertNotNull(p);
        gc.leaveStackFrame();
        gc.collect();
        for (int i = 0; i < gc.getHeap().length; i++) {
            assertEquals(0, gc.getHeap()[i]);
        }
    }

    @Test
    public void testMemoryReuse() {
        GC gc = new GC(16, 8);
        gc.enterStackFrame();
        Pointer p0 = gc.allocate(4);
        assertNotNull(p0);
        gc.enterStackFrame();
        Pointer p1 = gc.allocate(4);
        assertNotNull(p1);
        gc.leaveStackFrame();
        gc.collect();
        Pointer p2 = gc.allocate(4);
        assertNotNull(p2);
    }

    @Test
    public void testAllocateLarger() {
        GC gc = new GC(16, 8);
        gc.enterStackFrame();
        Pointer p0 = gc.allocate(4);
        assertNotNull(p0);
        gc.leaveStackFrame();
        gc.collect();
        Pointer p1 = gc.allocate(12);
        assertNotNull(p1);
    }
}
