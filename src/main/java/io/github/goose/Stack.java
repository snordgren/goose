package io.github.goose;

/**
 * A stack is used for
 */
public class Stack {
    private final GC gc;
    private final Stack parentStack;
    private final Pointer[] pointers;
    private final int stackSize;
    private int pointerCounter = 0;
    private boolean inScope = true;

    Stack(GC gc, int stackSize) {
        this(gc, null, stackSize);
    }

    Stack(Stack parentStack) {
        this(parentStack.gc, parentStack, parentStack.stackSize);
    }

    private Stack(GC gc, Stack parentStack, int stackSize) {
        this.gc = gc;
        this.parentStack = parentStack;
        this.pointers = new Pointer[stackSize];
        this.stackSize = stackSize;
    }

    public GC getGC() {
        return gc;
    }

    public Stack getParentStack() {
        return parentStack;
    }

    public Pointer[] getPointers() {
        return pointers;
    }

    public boolean hasPointer(int index) {
        for (Pointer pointer : pointers) {
            if (pointer.refersTo(index)) {
                return true;
            }
        }

        if (parentStack != null) {
            return parentStack.hasPointer(index);
        }

        return false;
    }

    public boolean isInScope() {
        return inScope;
    }

    void register(Pointer pointer) {
        pointers[pointerCounter] = pointer;
        pointerCounter++;
    }

    void setOutOfScope() {
        inScope = false;
    }
}
