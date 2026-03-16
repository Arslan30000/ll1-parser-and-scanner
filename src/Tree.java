package src;

import java.util.ArrayList;
import java.util.List;

public class Tree {
    public String symbol;
    public List<Tree> children;

    public Tree(String symbol) {
        this.symbol = symbol;
        this.children = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.symbol;
    }

    public String getTreeString(String prefix, boolean isTail) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(isTail ? "└── " : "├── ").append(symbol).append("\n");
        for (int i = 0; i < children.size() - 1; i++) {
            sb.append(children.get(i).getTreeString(prefix + (isTail ? "    " : "│   "), false));
        }
        if (children.size() > 0) {
            sb.append(children.get(children.size() - 1).getTreeString(prefix + (isTail ? "    " : "│   "), true));
        }
        return sb.toString();
    }
}