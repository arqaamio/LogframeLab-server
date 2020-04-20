package com.arqaam.indicator.model;

import javax.persistence.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity(name = "IND_INDICATOR")
public class Indicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "KEYWORDS")
    private String keywords;

    @OneToOne
    @JoinColumn(name="Level")
    private Level level;

    @Transient
    private List<String> keywordsList;

    public Indicator(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getKeywords() {
        return keywords;
    }

    public List<String> getKeywordsList() {
        if(keywordsList == null && ( keywords != null && !keywords.isEmpty())){
            keywordsList = new ArrayList<>(Arrays.asList(keywords.split(",")));
        }
        return keywordsList;
    }

    public void setKeywordsList(List<String> keywordsList) {
        this.keywordsList = keywordsList;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
