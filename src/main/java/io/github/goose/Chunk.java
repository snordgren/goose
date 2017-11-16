package io.github.goose;

public class Chunk {
    private final int start, end;

    public Chunk(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public Chunk getTail(int size) {
        return new Chunk(start + size, end);
    }

    public int getSize() {
        return end - start;
    }

    public int getStart() {
        return start;
    }
}
