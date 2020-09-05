package com.arqaam.logframelab.model.persistence;

import lombok.*;

import javax.persistence.*;

@Entity(name = "SDG_CODE")
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class SDGCode {
    @Column(name = "ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;
}