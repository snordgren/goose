package io.github.goose;

import java.util.ArrayList;
import java.util.List;

/**
 * A very simple tracing garbage collector.
 */
public class GC {
    private final byte[] heap;
    private Stack stack;
    private final List<Chunk> chunkList = new ArrayList<>();
    private final List<Pointer> pointerList = new ArrayList<>();

    public GC(int heapSize, int stackSize) {
        heap = new byte[heapSize];
        stack = new Stack(this, stackSize);
        chunkList.add(new Chunk(0, heapSize));
    }

    /**
     * Allocates a new pointer with the specified size. Note that the size of
     * the pointer is stored immediately ahead of the pointer in memory,
     * meaning that the actual allocation is 4 bytes larger than the passed
     * size.
     *
     * @param size The size to allocate for use.
     * @return A pointer to the allocated block, or null if allocation was not
     * possible.
     */
    public Pointer allocate(int size) {
        return chunkList.stream()
                .filter(c -> c.getSize() >= getRealSize(size))
                .findFirst()
                .map(chunk -> allocateChunk(chunk, size))
                .orElse(null);
    }

    private Pointer allocateChunk(Chunk chunk, int size) {
        Chunk tailChunk = chunk.getTail(getRealSize(4));
        chunkList.remove(chunk);
        chunkList.add(tailChunk);
        Pointer pointer = new Pointer(stack, chunk.getStart() + 4, size);
        pointerList.add(pointer);
        return pointer;
    }

    public void collect() {
        List<Pointer> unreachable = new ArrayList<>(pointerList);
        Stack currentStack = stack;
        while (currentStack != null && !unreachable.isEmpty()) {
            for (Pointer pointer : currentStack.getPointers()) {
                if (pointer != null) {
                    unreachable.remove(pointer);
                    if (pointer.getSize() >= 8) {
                        List<Integer> potentialPointers = getPotentialPointers(pointer);
                        for (Integer potentialPointer : potentialPointers) {
                            unreachable.removeIf(u -> u.refersTo(potentialPointer));
                        }
                    }
                }
            }

            currentStack = currentStack.getParentStack();
        }

        for (Pointer pointer : unreachable) {
            int pointerStart = pointer.getHeaderLocation();
            int pointerEnd = pointer.getLocation() + pointer.getSize();
            chunkList.add(new Chunk(pointerStart, pointerEnd));
            pointer.free();
        }

        mergeChunks();
    }

    private List<Integer> getPotentialPointers(Pointer pointer) {
        List<Integer> potentialPointers = new ArrayList<>();
        int offset = 0;
        do {
            int pointerLocation = 0x0;
            pointerLocation |= pointer.read(offset);
            pointerLocation |= pointer.read(offset + 1) << 8;
            pointerLocation |= pointer.read(offset + 2) << 16;
            pointerLocation |= pointer.read(offset + 3) << 24;
            potentialPointers.add(pointerLocation);
        } while (offset < pointer.getSize() - 4);
        return potentialPointers;
    }

    /**
     * Enter a new stack frame, for example when a function is called.
     */
    public void enterStackFrame() {
        stack = new Stack(stack);
    }

    /**
     * Access the actual size of a
     *
     * @param size
     * @return
     */
    private int getRealSize(int size) {
        return size + 4;
    }

    public byte[] getHeap() {
        return heap;
    }

    /**
     * Leaves the current stack frame, for example when a function returns.
     */
    public void leaveStackFrame() {
        stack.setOutOfScope();
        stack = stack.getParentStack();
    }

    private Chunk getMergeableChunk(List<Chunk> available, Chunk chunk) {
        Chunk mergeable = null;
        for (Chunk c : available) {
            if (c.getStart() == chunk.getEnd() ||
                    c.getEnd() == chunk.getStart()) {
                mergeable = c;
            }
        }

        return mergeable;
    }

    /**
     * Merge the chunks that are adjacent but still divided, allowing us to
     * allocate larger objects than those that have been freed.
     */
    private void mergeChunks() {
        List<Chunk> oldChunkList = new ArrayList<>(chunkList);
        chunkList.clear();
        for (Chunk chunk : oldChunkList) {
            Chunk mergeableChunk = getMergeableChunk(chunkList, chunk);
            if (mergeableChunk != null) {
                chunkList.remove(mergeableChunk);
                chunkList.add(mergeableChunk.merge(chunk));
            } else {
                chunkList.add(chunk);
            }
        }
    }
}
