package compiler.parser.ast.nodes;

import java.util.HashMap;

/**
 * Interface for nodes which allows them to store the line number they were parsed from.
 *
 * This is useful for allowing error messages from the type checker to specify and display
 * the line where this node was parsed from if it has an error. Nodes where type errors are thrown
 * must implement this interface. This includes LocNode, OperationNode, and AssignmentNode, and their
 * children.
 */
public interface LineTrackingNode {
    // Static lookup table for storing line numbers associated with a node.
    HashMap<LineTrackingNode, Integer> lineLookup = new HashMap<>();

    /**
     * Set the line number this node was parsed from.
     *
     * @param line the line number this node was parsed from in the source code.
     */
    default void setLine(int line) {
        lineLookup.put(this, line);
    }

    /**
     * Get the line number this node was parsed from.
     *
     * @return the line number this node was parsed from in the source code.
     */
    default int getLine() {
        return lineLookup.get(this);
    }
}
