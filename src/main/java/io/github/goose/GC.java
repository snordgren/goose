package io.github.goose;

import java.util.ArrayList;
import java.util.List;

/**
 * A super-simple tracing garbage collector.
 *
 */
public class GC {
    private final byte[] heap;
    private Stack stack;
    private final List<Chunk> chunkList = new ArrayList<>();
    private final List<Pointer> pointerList = new ArrayList<>();

    /**
     * Create a new GC with a specific size for the heap and the stack.
     *
     * @param heapSize The size of the heap that this garbage collector
     * should manage. The garbage collector is backed by a byte array with
     * this many elements.
     * @param stackSize The size of the stack. Every stack frame will have
     * this number of pointers available for allocation.
     */
    public GC(int heapSize, int stackSize) {
        heap = new byte[heapSize];
        stack = new Stack(this, stackSize);
        chunkList.add(new Chunk(0, heapSize));
    }

    /**
     * Allocates a new pointer with the specified size. Note that the size of
     * the pointer is stored immediately ahead of the pointer in memory,
     * meaning that the actual allocation is larger than the passed size.
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

    /**
     * Allocate a pointer of a specific size from a specific chunk.
     *
     * @param chunk The chunk to use for this pointer.
     * @param size The size of the pointer.
     * @return The pointer that was allocated.
     */
    private Pointer allocateChunk(Chunk chunk, int size) {
        chunkList.remove(chunk);
        if (chunk.getSize() > size) {
            Chunk tailChunk = chunk.getTail(getRealSize(size));
            chunkList.add(tailChunk);
        }
        Pointer pointer = new Pointer(stack, chunk.getStart(), size);
        pointerList.add(pointer);
        return pointer;
    }

    /**
     * Triggers a garbage collection. Any pointers allocated in a stack frame
     * that has since been left will be considered unreachable, unless they
     * are referenced in the currently-in-scope stack frames.
     */
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
            int pointerEnd = pointer.getAddress() + pointer.getSize();
            chunkList.add(new Chunk(pointerStart, pointerEnd));
            pointer.free();
        }

        mergeChunks();
    }

    /**
     * Get a list of values that may be pointers, from the values that are
     * present at a pointer location. Practically speaking, this method
     * extracts all the 4-byte combinations present in the pointer.
     *
     * @param pointer The pointer to start from.
     * @return The list of potential pointer-locations.
     */
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
     * Access the actual size of a pointer, including headers.
     *
     * @param size The base size of the pointer.
     * @return The size of a pointer including headers.
     */
    public static int getRealSize(int size) {
        return size + Pointer.HEADER_SIZE;
    }

    /**
     * Access the raw heap.
     *
     * @return The raw byte array backing this garbage collector.
     */
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

    /**
     * Merge the chunks that are adjacent but still divided, allowing us to
     * allocate larger objects than those that have been freed.
     */
    private void mergeChunks() {
        List<Chunk> oldChunkList = new ArrayList<>(chunkList);
        chunkList.clear();
        for (Chunk chunk : oldChunkList) {
            Chunk mergeableChunk = findMergeableChunk(chunkList, chunk);
            if (mergeableChunk != null) {
                chunkList.remove(mergeableChunk);
                chunkList.add(mergeableChunk.merge(chunk));
            } else {
                chunkList.add(chunk);
            }
        }
    }

    /**
     * Finds a chunk that can be merged with the current one.
     *
     * @param available The list of chunks to test.
     * @param chunk The chunk to be checked for.
     * @return A chunk that can be merged with, otherwise null.
     */
    private static Chunk findMergeableChunk(List<Chunk> available, Chunk chunk) {
        Chunk mergeable = null;
        for (Chunk c : available) {
            if (c.isAdjacent(chunk)) {
                mergeable = c;
            }
        }

        return mergeable;
    }
}
