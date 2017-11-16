# goose

Goose is a super-simple garbage collector written in Java. It's a toy project
to explore how garbage collectors work. Here's an example:

	// allocate a new garbage collector with a 1024-byte heap
	// and 8 potential pointers per stack frame
	GC gc = new GC(1024, 8);
	gc.enterStackFrame(); // enter a stack frame
	Pointer p = gc.allocate(4); // allocate our pointer
	gc.leaveStackFrame(); // leave our stack frame, pointer is now out of scope
	gc.collect(); // trigger garbage collection
	boolean isPointerValid = p.isValid(); // false
