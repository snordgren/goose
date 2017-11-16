package io.github.goose;

/**
 * A stack is used for keeping track of which pointers are allocated where.
 */
public class Stack {
    private final GC gc;
    private final Stack parentStack;
    private final Pointer[] pointers;
    private final int stackSize;
    private int pointerCounter = 0;

    /**
     * Instantiate a new root stack for a garbage collector.
     *
     * @param gc The garbage collector that owns this stack.
     * @param stackSize The size of this stack.
     */
    Stack(GC gc, int stackSize) {
        this(gc, null, stackSize);
    }

    /**
     * Instantiate a new frame for an existing stack.
     * @param parentStack The parent stack frame.
     */
    Stack(Stack parentStack) {
        this(parentStack.gc, parentStack, parentStack.stackSize);
    }

    /**
     * Internal value-setting constructor.
     *
     * @param gc The garbage collector that owns this stack.
     * @param parentStack The parent stack frame.
     * @param stackSize The number of pointers available to this stack.
     */
    private Stack(GC gc, Stack parentStack, int stackSize) {
        this.gc = gc;
        this.parentStack = parentStack;
        this.pointers = new Pointer[stackSize];
        this.stackSize = stackSize;
    }

    /**
     * @return The garbage collector that owns this stack.
     */
    public GC getGC() {
        return gc;
    }

    /**
     * @return The parent stack of this stack.
     */
    public Stack getParentStack() {
        return parentStack;
    }

    /**
     * @return The pointers that are registered with this stack.
     */
    public Pointer[] getPointers() {
        return pointers;
    }

    /**
     * Register a pointer as belonging to this stack.
     * @param pointer The pointer to register.
     */
    void register(Pointer pointer) {
        pointers[pointerCounter] = pointer;
        pointerCounter++;
    }
}
