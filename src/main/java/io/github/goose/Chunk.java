package io.github.goose;

/**
 * A chunk represents a continuous block of non-allocated memory.
 */
class Chunk {
    private final int start, end;

    /**
     * Instantiate a new chunk between two memory points.
     *
     * @param start The start of this chunk.
     * @param end The end of this chunk.
     */
    Chunk(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @return The end point of this chunk.
     */
    int getEnd() {
        return end;
    }

    /**
     * @return The size of this chunk; its end address minus its start address.
     */
    int getSize() {
        return end - start;
    }

    /**
     * @return The memory address of the start of this chunk.
     */
    int getStart() {
        return start;
    }

    /**
     * Creates a new chunk from this one, ending at the same place, but
     * starting at an offset. Used when a pointer is allocated from a
     * chunk and the chunk is larger than the allocated memory.
     *
     * @param size The size of the offset.
     * @return A new, smaller chunk.
     */
    Chunk getTail(int size) {
        return new Chunk(start + size, end);
    }

    /**
     * Checks if this chunk is adjacent with another - its start is at the end
     * of this one, or its end is at the start of this one.
     *
     * @return True if this chunk is adjacent to the other.
     */
    boolean isAdjacent(Chunk chunk) {
        return getStart() == chunk.getEnd()
                || getEnd() == chunk.getStart();
    }

    /**
     * Returns a new chunk that starts where this chunk starts and end where
     * the other chunk starts.
     *
     * @param chunk The chunk to merge with.
     * @return A chunk if these chunks are adjacent, otherwise null.
     */
    Chunk merge(Chunk chunk) {
        if (getEnd() == chunk.getStart()) {
            int start = getStart();
            int end = chunk.getEnd();
            return new Chunk(start, end);
        } else if (getStart() == chunk.getEnd()) {
            return chunk.merge(this);
        } else return null;
    }
}
