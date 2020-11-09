package com.arqaam.logframelab.model.persistence;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "STATISTIC")
@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DOWNLOAD_WORD_TEMPLATE", nullable = false)
    @Builder.Default
    private Integer downloadWordTemplate = 0;

    @Column(name = "DOWNLOAD_DFID_TEMPLATE", nullable = false)
    @Builder.Default
    private Integer downloadDFIDTemplate = 0;

    @Column(name = "DOWNLOAD_XLSX_TEMPLATE", nullable = false)
    @Builder.Default
    private Integer downloadXLSXTemplate = 0;

    @Column(name = "DOWNLOAD_PRM_TEMPLATE", nullable = false)
    @Builder.Default
    private Integer downloadPRMTemplate = 0;

    @Column(name = "DATE")
    private Date date;
}