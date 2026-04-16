package ds;

/**
 * Array-based Stack for Rule Management.
 * Built without java.util.
 */
public class RuleEngineStack {
    private final Object[] stack;
    private int top;

    public RuleEngineStack(int capacity) {
        this.stack = new Object[capacity];
        this.top = -1;
    }

    public void push(Object rule) {
        if (top < stack.length - 1) {
            stack[++top] = rule;
        }
    }

    public Object pop() {
        return (top >= 0) ? stack[top--] : null;
    }

    public Object peek() {
        return (top >= 0) ? stack[top] : null;
    }

    public int size() { return top + 1; }
    
    public Object getAt(int index) {
        if (index >= 0 && index <= top) return stack[index];
        return null;
    }
}
