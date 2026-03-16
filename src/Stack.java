package src;

import java.util.ArrayList;
import java.util.List;

public class Stack<T> {
    private List<T> elements;

    public Stack() {
        this.elements = new ArrayList<>();
    }

    public void push(T item) {
        elements.add(item);
    }

    public T pop() {
        if (isEmpty()) return null;
        return elements.remove(elements.size() - 1);
    }

    public T peek() {
        if (isEmpty()) return null;
        return elements.get(elements.size() - 1);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T element : elements) {
            sb.append(element.toString()).append(" ");
        }
        return sb.toString().trim();
    }
}