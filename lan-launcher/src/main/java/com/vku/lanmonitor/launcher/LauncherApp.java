package com.vku.lanmonitor.launcher;

import javax.swing.SwingUtilities;

public class LauncherApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LauncherFrame().setVisible(true));
    }
}
