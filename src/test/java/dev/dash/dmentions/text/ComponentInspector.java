package dev.dash.dmentions.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Test oracle that inspects component trees independently of {@link StyledText},
 * so assertions never rely on the implementation under test.
 */
final class ComponentInspector {

    /** One visible unit of a component tree: a single char of text, or one non-text component. */
    record Unit(char ch, Style style, Component atom) {
        boolean isAtom() {
            return atom != null;
        }
    }

    private ComponentInspector() {
    }

    /** Flattens the tree into units carrying their effective (inherited) style. */
    static List<Unit> flatten(Component component) {
        List<Unit> out = new ArrayList<>();
        walk(component, Style.empty(), out);
        return out;
    }

    private static void walk(Component component, Style parent, List<Unit> out) {
        Style effective = parent.merge(component.style());
        if (component instanceof TextComponent text) {
            String content = text.content();
            for (int i = 0; i < content.length(); i++) {
                out.add(new Unit(content.charAt(i), effective, null));
            }
        } else {
            out.add(new Unit(StyledText.ATOM, effective, component.children(List.of())));
        }
        for (Component child : component.children()) {
            walk(child, effective, out);
        }
    }

    /** Plain text with every non-text component written as {@link StyledText#ATOM}. */
    static String plain(Component component) {
        StringBuilder sb = new StringBuilder();
        for (Unit unit : flatten(component)) {
            sb.append(unit.ch());
        }
        return sb.toString();
    }

    /** Effective style of the unit at the given index. */
    static Style styleAt(Component component, int index) {
        return flatten(component).get(index).style();
    }

    /** Contents of all non-empty text components, in depth-first order. */
    static List<String> textLeaves(Component component) {
        List<String> out = new ArrayList<>();
        visit(component, c -> {
            if (c instanceof TextComponent text && !text.content().isEmpty()) {
                out.add(text.content());
            }
        });
        return out;
    }

    /** Every component of the tree, depth-first, root included. */
    static List<Component> allNodes(Component component) {
        List<Component> out = new ArrayList<>();
        visit(component, out::add);
        return out;
    }

    private static void visit(Component component, Consumer<Component> action) {
        action.accept(component);
        for (Component child : component.children()) {
            visit(child, action);
        }
    }
}
