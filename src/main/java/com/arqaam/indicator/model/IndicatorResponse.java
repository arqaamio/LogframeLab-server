package com.arqaam.indicator.model;

import java.util.List;

public class IndicatorResponse {

    private long id;
    private String level;
    private String color;
    private String label;
    private String description;
    private List<String> keys;
    private String var;

    public IndicatorResponse(long id, String level, String color, String label, String description, List<String> keys, String var) {
        this.id = id;
        this.level = level;
        this.color = color;
        this.label = label;
        this.description = description;
        this.keys = keys;
        this.var = var;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
