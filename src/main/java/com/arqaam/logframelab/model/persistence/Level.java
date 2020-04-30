package com.arqaam.logframelab.model.persistence;

import lombok.*;

import javax.persistence.*;

@Entity(name = "IND_LEVEL_INDICATOR")
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@EqualsAndHashCode
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
}
