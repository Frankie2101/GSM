package com.gsm.dto;

/**
 * A simple DTO representing a single, valid combination of a style and a color
 * within a sale order.
 */
public class ZaloStyleColorDto {
    private String style;
    private String color;

    public ZaloStyleColorDto(String style, String color) {
        this.style = style;
        this.color = color;
    }

    // Getters and Setters
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}