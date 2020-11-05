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
import java.util.List;

@Service
public class StatisticService implements Logging {
    
    @Autowired
    private StatisticRepository statisticsRepository;

    /**
     * Adds to the counter of number of times a download format
     * has been downloaded
     * @param format Download Format
     * @return Statistic with new values
     */
    public Statistic addDownloadStatistic(String format) {
        logger().info("Adding download statistics to {} format", format);
        Date date = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),1, 0,0,0).getTime();
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
        return statisticsRepository.save(statistic);
    }

    /**
     * Retrieves all statistics
     * @return List of statistics
     */
    public List<Statistic> getAllStatistics() {
        logger().info("Retrieving all statistics");
        return statisticsRepository.findAll();
    }
}
