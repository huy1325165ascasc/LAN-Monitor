package com.vku.lanmonitor.admin.ui.theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Design System for LAN Monitor Admin.
 * Contains color constants, fonts, and UI helper methods.
 */
public final class LanMonitorTheme {

    // Colors
    public static final Color PURPLE_HEADER = new Color(0x59, 0x22, 0x8E);
    public static final Color PURPLE_LIGHT = new Color(0xE9, 0xD8, 0xF5);
    public static final Color DARK_SIDEBAR = new Color(0x23, 0x26, 0x34);
    public static final Color DARK_CARD = new Color(0x2D, 0x30, 0x43);
    public static final Color DARK_CARD_HOVER = new Color(0x38, 0x3D, 0x54);
    public static final Color DARK_BORDER = new Color(0x3C, 0x40, 0x58);
    public static final Color LIGHT_BG = new Color(0xDB, 0xE1, 0xE8);
    public static final Color LIGHT_CONTENT = new Color(0xC8, 0xD1, 0xDC);
    public static final Color WHITE = Color.WHITE;
    public static final Color LOG_BG = new Color(0x0D, 0x0F, 0x14);
    public static final Color LOG_TEXT = new Color(0xA3, 0xE6, 0x35);
    public static final Color GREEN_BTN = new Color(0x1D, 0xA8, 0x52);
    public static final Color RED_BTN = new Color(0xA3, 0x3C, 0x46);
    public static final Color BLUE_BTN = new Color(0x2B, 0x6C, 0xB0);
    public static final Color ORANGE_BTN = new Color(0xD9, 0x77, 0x06);
    public static final Color TEXT_LIGHT = new Color(0xE2, 0xE8, 0xF0);
    public static final Color TEXT_MUTED = new Color(0x8A, 0x92, 0xA6);

    // Fonts
    public static final Font FONT_BASE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private LanMonitorTheme() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates a primary button with specified background color.
     *
     * @param text Button text
     * @param bg   Background color
     * @return Styled JButton
     */
    public static JButton createPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    /**
     * Creates a secondary button with default styling.
     *
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BASE);
        btn.setBackground(DARK_BORDER);
        btn.setForeground(TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(DARK_BORDER));
        return btn;
    }

    public static JButton createSecondaryButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BASE);
        btn.setBackground(bg);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    /**
     * Creates a dark-themed card panel.
     *
     * @return Styled JPanel
     */
    public static JPanel createDarkCard() {
        JPanel panel = new JPanel();
        panel.setBackground(DARK_CARD);
        panel.setBorder(new LineBorder(DARK_BORDER));
        return panel;
    }

    /**
     * Applies global styles to Swing components.
     */
    public static void applyGlobalStyle() {
        UIManager.put("Button.font", FONT_BASE);
        UIManager.put("Label.font", FONT_BASE);
        UIManager.put("TextField.font", FONT_BASE);
        UIManager.put("TextArea.font", FONT_MONO);
    }
}
