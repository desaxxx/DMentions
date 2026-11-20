package dev.dash.dmentions.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.List;

public final class StyledText {

    public static final char ATOM = '\uFFFC';

    private final String plain;
    private final Style[] styles; // style of plain.charAt(i)
    private final Component[] atoms; // atom of plain.charAt(i) or null

    private StyledText(String plain, Style[] styles, Component[] atoms) {
        this.plain = plain;
        this.styles = styles;
        this.atoms = atoms;
    }

    public static StyledText of(Component component) {
        StringBuilder text = new StringBuilder();
        List<Style> styles = new ArrayList<>();
        List<Component> atoms = new ArrayList<>();
        walk(component, Style.empty(), text, styles, atoms);
        return new StyledText(text.toString(), styles.toArray(new Style[0]), atoms.toArray(new Component[0]));
    }

    private static void walk(Component component,
                        Style parentStyle,
                        StringBuilder text,
                        List<Style> styles,
                        List<Component> atoms
    ) {
        Style effective = parentStyle.merge(component.style());
        if (component instanceof TextComponent textComponent) {
            for (char c : textComponent.content().toCharArray()) {
                text.append(c);
                styles.add(effective);
                atoms.add(null);
            }
        } else {
            text.append(ATOM);
            styles.add(effective);
            atoms.add(component.children(List.of()));
        }
        for (Component child : component.children()) {
            walk(child, effective, text, styles, atoms);
        }
    }

    public String plain() {
        return plain;
    }

    public Component replace(List<Replacement> replacements) {
        int previousEnd = 0;
        for (Replacement r : replacements) {
            if (r.start() < previousEnd) {
                throw new IllegalArgumentException("Replacement " + r + " must start at or after index " + previousEnd
                        + " (replacements must be non-negative, sorted by start and non-overlapping)");
            } else if (r.start() >= r.end()) {
                throw new IllegalArgumentException("Replacement " + r + " has an empty range (start must be less than end)");
            } else if (r.end() > plain.length()) {
                throw new IllegalArgumentException("Replacement " + r + " exceeds the text length " + plain.length());
            }
            previousEnd = r.end();
        }

        TextComponent.Builder root = Component.text();
        int cursor = 0;
        for (Replacement r : replacements) {
            appendRange(root, cursor, r.start());
            root.append(r.component());
            cursor = r.end();
        }
        appendRange(root, cursor, plain.length());
        return root.build();
    }

    private void appendRange(TextComponent.Builder root, int from, int to) {
        int i = from;
        while (i < to) {
            if (atoms[i] != null) {
                root.append(atoms[i].style(styles[i]));
                i++;
            } else {
                int j = i;
                while (j < to && atoms[j] == null && styles[j].equals(styles[i])) {
                    j++;
                }
                root.append(Component.text(plain.substring(i, j), styles[i]));
                i = j;
            }
        }
    }
}
