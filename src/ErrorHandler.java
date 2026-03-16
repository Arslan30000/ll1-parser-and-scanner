package src;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private List<String> errors;

    public ErrorHandler() {
        this.errors = new ArrayList<>();
    }

    public void reportError(String message) {
        errors.add(message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getErrorReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SYNTAX ERRORS ===\n");
        for (String err : errors) {
            sb.append("- ").append(err).append("\n");
        }
        return sb.toString();
    }
    
    public void clear() {
        errors.clear();
    }
}