package com.arqaam.logframelab.model.persistence;

import lombok.*;

import javax.persistence.*;

@Entity(name = "SOURCE")
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;
}