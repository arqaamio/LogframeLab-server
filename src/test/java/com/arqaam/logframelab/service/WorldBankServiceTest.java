package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.InvalidDataSourceException;
import com.arqaam.logframelab.exception.WorldBankAPIRequestFailedException;
import com.arqaam.logframelab.model.WorldBankIndicator;
import com.arqaam.logframelab.model.persistence.Indicator;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "test")
class WorldBankServiceTest {

    @Autowired
    private WorldBankService worldBankService;
    
    @Test
    void retrieveCountries() {
        Map<String, String> result = worldBankService.getCountries();
        System.out.println(result);
        System.out.print(result.size());
        assertNotNull(result);
        assertEquals(302, result.size());
        assertFalse(result.values().stream().anyMatch(Strings::isNullOrEmpty));
    }

    @Test
    void retrieveIndicatorValues() {
        Indicator indicator = Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build();
        List<WorldBankIndicator> result = worldBankService.getIndicatorValues(indicator, "NZL", Collections.emptyList());
        assertNotNull(result);
        assertEquals(17, result.size());
        assertFalse(result.stream().anyMatch(x -> x == null || !x.getValue().equals("100")));

        for (int i = 0; i < result.size(); i++) {
            assertEquals(result.get(i).getDate(), String.valueOf(2000+i));
        }
    }

    @Test
    void retrieveIndicatorValues_withNullDate() {
        Indicator indicator = Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build();
        List<WorldBankIndicator> result = worldBankService.getIndicatorValues(indicator, "NZL", null);
        assertNotNull(result);
        assertEquals(17, result.size());
        assertFalse(result.stream().anyMatch(x -> x == null || !x.getValue().equals("100")));

        for (int i = 0; i < result.size(); i++) {
            assertEquals(result.get(i).getDate(), String.valueOf(2000+i));
        }
    }

    @Test
    void retrieveIndicatorValues_withOneDates() {
        Indicator indicator = Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build();
        List<WorldBankIndicator> result = worldBankService.getIndicatorValues(indicator, "NZL", Collections.singletonList(2000));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("100", result.get(0).getValue());
        assertEquals(result.get(0).getDate(), "2000");
    }

    @Test
    void retrieveIndicatorValues_withDates() {
        Indicator indicator = Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build();
        List<WorldBankIndicator> result = worldBankService.getIndicatorValues(indicator, "NZL", Arrays.asList(2000, 2001));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(x -> x == null || !x.getValue().equals("100")));

        for (int i = 0; i < result.size(); i++) {
            assertEquals(result.get(i).getDate(), String.valueOf(2000+i));
        }
    }

    @Test
    void retrieveIndicatorValues_indicatorMissingDataSource() {
        assertThrows(InvalidDataSourceException.class, () -> worldBankService.getIndicatorValues(Indicator.builder().build(), "NZL", Collections.emptyList()));
    }

    @Test
    void retrieveIndicatorValues_indicatorDataSourceDoesntMatch() {
        assertThrows(InvalidDataSourceException.class, () -> worldBankService.getIndicatorValues(Indicator.builder().dataSource("https://randomwebsite.com").build(), "NZL", Collections.emptyList()));
    }

    @Test
    void retrieveIndicatorValues_indicatorDataSourceDEmpty() {
        assertThrows(InvalidDataSourceException.class, () -> worldBankService.getIndicatorValues(Indicator.builder().dataSource("").build(), "NZL", Collections.emptyList()));
    }

    @Test
    void retrieveIndicatorValues_wrongCountryId() {
        assertThrows(WorldBankAPIRequestFailedException.class, () -> worldBankService.getIndicatorValues(Indicator.builder().dataSource("https://data.worldbank.org/indicator/EG.CFT.ACCS.ZS?view=chart").build(),
                "AAAAA", Collections.emptyList()));
    }

    @Test
    void retrieveIndicatorValues_wrongIndicatorId() {
        assertThrows(WorldBankAPIRequestFailedException.class, () -> worldBankService.getIndicatorValues(Indicator.builder().dataSource("https://data.worldbank.org/indicator/BB.BB.B?view=chart").build(),
                "NZL", Collections.emptyList()));
    }
}