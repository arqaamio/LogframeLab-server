package com.arqaam.logframelab.model.persistence;

import lombok.*;

import javax.persistence.*;

@Entity(name = "CRS_CODE")
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class CRSCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;
}