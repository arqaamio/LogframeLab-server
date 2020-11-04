package com.arqaam.logframelab.service;


import com.arqaam.logframelab.model.persistence.Statistic;
import com.arqaam.logframelab.repository.StatisticRepository;
import com.arqaam.logframelab.util.Constants;
import com.arqaam.logframelab.util.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
public class StatisticService implements Logging {
    
    @Autowired
    private StatisticRepository statisticsRepository;

    public void addDownloadStatistic(String format) {
        Calendar.getInstance().set(Calendar.DAY_OF_MONTH, 1);
        Calendar.getInstance().set(Calendar.HOUR, 0);
        Calendar.getInstance().set(Calendar.MINUTE, 0);
        Calendar.getInstance().set(Calendar.SECOND, 0);
        Calendar.getInstance().set(Calendar.MILLISECOND, 0);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),1);
        Date date = gregorianCalendar.getTime();
        Statistic statistic = statisticsRepository.findByDate(date).orElse(new Statistic());
        statistic.setDate(date);
        switch(format){
            case Constants.XLSX_FORMAT:
                statistic.setDownloadXLSXTemplate(statistic.getDownloadXLSXTemplate() + 1);
                break;
            case Constants.DFID_FORMAT:
                statistic.setDownloadDFIDTemplate(statistic.getDownloadDFIDTemplate() + 1);
                break;
            case Constants.PRM_FORMAT:
                statistic.setDownloadPRMTemplate(statistic.getDownloadPRMTemplate() + 1);
                break;
            case Constants.WORD_FORMAT:
            default:
                statistic.setDownloadWordTemplate(statistic.getDownloadWordTemplate() + 1);
                break;
        }
        statisticsRepository.save(statistic);
    }
}
