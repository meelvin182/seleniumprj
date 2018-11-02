package ru.sokolov.gui;


import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;

public class CopyPasteNumsArea extends TextArea {

    @Override
    public void paste() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            final String text = clipboard.getString().contains(";") ?
                    clipboard.getString().replaceAll("\n", "") : clipboard.getString();
            if (text != null) {
                replaceSelection(text);
            }
        }
    }


}
