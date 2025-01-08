package compiler.parser.ast.nodes;

import java.util.HashMap;

public interface LineTrackingNode {
    HashMap<LineTrackingNode, Integer> lineLookup = new HashMap<>();

    default void setLine(int line) {
        lineLookup.put(this, line);
    }

    default int getLine() {
        return lineLookup.get(this);
    }
}
