package com.arqaam.indicator.model;

import javax.persistence.*;

@Entity(name = "IND_LEVEL_INDICATOR")
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TEMPLATE_VAR")
    private String templateVar;

    @Column(name = "COLOR")
    private String color;

    public Level(){

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateVar() {
        return templateVar;
    }

    public void setTemplateVar(String templateVar) {
        this.templateVar = templateVar;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
