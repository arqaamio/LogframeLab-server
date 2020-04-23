package com.arqaam.logframelab.model.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity(name = "IND_INDICATOR")

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
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

    public List<String> getKeywordsList() {
        if(keywordsList == null && ( keywords != null && !keywords.isEmpty())){
            keywordsList = new ArrayList<>(Arrays.asList(keywords.split(",")));
        }
        return keywordsList;
    }
}
