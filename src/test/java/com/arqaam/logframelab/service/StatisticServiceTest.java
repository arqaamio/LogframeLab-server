package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.persistence.Statistic;
import com.arqaam.logframelab.repository.StatisticRepository;
import com.arqaam.logframelab.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticServiceTest {

    @Mock
    private StatisticRepository statisticRepository;
    @InjectMocks
    private StatisticService statisticService;

    @Test
    void addDownloadStatistic() {
        Date date = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),1, 0,0,0).getTime();
        Statistic foundStatistic = new Statistic();
        foundStatistic.setDownloadWordTemplate(10);
        foundStatistic.setDate(date);
        when(statisticRepository.findByDate(date)).thenReturn(Optional.of(foundStatistic));
        Statistic expected = new Statistic();
        expected.setDownloadWordTemplate(11);
        expected.setDate(date);
        when(statisticRepository.save(expected)).thenReturn(expected);
        Statistic result = statisticService.addDownloadStatistic(Constants.WORD_FORMAT);
        verify(statisticRepository).save(expected);
        assertNotNull(result);
        assertEquals(date, result.getDate());
        assertEquals(11, result.getDownloadWordTemplate());
        assertEquals(0, result.getDownloadDFIDTemplate());
        assertEquals(0, result.getDownloadPRMTemplate());
        assertEquals(0, result.getDownloadXLSXTemplate());
    }

    @Test
    void addDownloadStatistic_noDownloadFound() {
        Date date = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),1, 0,0,0).getTime();
        when(statisticRepository.findByDate(any())).thenReturn(Optional.empty());
        Statistic expected = new Statistic();
        expected.setDownloadDFIDTemplate(1);
        expected.setDate(date);
        when(statisticRepository.save(expected)).thenReturn(expected);

        Statistic result = statisticService.addDownloadStatistic(Constants.DFID_FORMAT);
        verify(statisticRepository).save(expected);
        assertNotNull(result);
        assertEquals(date, result.getDate());
        assertEquals(0, result.getDownloadWordTemplate());
        assertEquals(1, result.getDownloadDFIDTemplate());
        assertEquals(0, result.getDownloadPRMTemplate());
        assertEquals(0, result.getDownloadXLSXTemplate());
    }

    @Test
    void getAllStatistics() {
        List<Statistic> expected = new ArrayList<>();
        expected.add(new Statistic(1L,1,1,1,2, null));
        expected.add(new Statistic(2L,0,1,1,1, null));

        when(statisticRepository.findAll()).thenReturn(expected);
        List<Statistic> result = statisticService.getAllStatistics();
        assertEquals(expected, result);
    }
}