package ru.sokolov.gui;

import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;

import java.util.ArrayList;
import java.util.List;

public class KeyTextField extends TextField {

    private static List<KeyTextField> fields = new ArrayList<>();

    public KeyTextField() {
        fields.add(this);
    }

    @Override
    public void paste(){
        if (fields.indexOf(this) == 0) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                String[] keyParts = clipboard.getString().split("-");
                for (int i = 0; i < fields.size(); i++) {
                    fields.get(i).replaceSelection(keyParts[i]);
                }
            }
        } else {
            super.paste();
        }
    }
}
