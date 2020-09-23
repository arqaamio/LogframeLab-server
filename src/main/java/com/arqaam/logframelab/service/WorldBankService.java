package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.FailedJsonMappingException;
import com.arqaam.logframelab.exception.InvalidDataSourceException;
import com.arqaam.logframelab.exception.WorldBankAPIRequestFailedException;
import com.arqaam.logframelab.model.WorldBankCountry;
import com.arqaam.logframelab.model.WorldBankIndicator;
import com.arqaam.logframelab.model.WorldDevelopmentIndicator;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.util.Logging;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * World Bank API Documentation
 * https://datahelpdesk.worldbank.org/knowledgebase/topics/125589-developer-information
 */
@Service
public class WorldBankService implements Logging {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${logframelab.world-bank-url}")
    private String URL;

    /**
     * Retrieves all the countries in which World Bank API has indicators
     * @return Map of where key is the country's Id and value the name of the country
     */
    public Map<String, String> getCountries(){
        logger().info("Requesting countries from the World Bank API");
        List<Object> response = null;
        try {
            // The request has "per_page=1000" because "/all" doesn't return all and in case the list of countries increases
            ResponseEntity<List<Object>> responseEntity = restTemplate.exchange(URL+"country?per_page=1000&format=json", HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<Object>>() {});
            response = responseEntity.getBody();
            if(responseEntity.getStatusCode()!=HttpStatus.OK || response == null || response.isEmpty()){
                logger().error("Failed to receive countries' data from World Bank API. Response: {}", response);
                throw new WorldBankAPIRequestFailedException();
            }
            // Since it was parsed as a List it needs to be parsed to Json again so it can be mapped to the object
            List<WorldBankCountry> countryList = objectMapper.readValue(objectMapper.writeValueAsString(response.get(1)),
                    new TypeReference<List<WorldBankCountry>>(){});

            return countryList.stream().collect(Collectors.toMap(WorldBankCountry::getId, WorldBankCountry::getName));
        // The error response is in XML so it can't be parsed
        }catch (RestClientException e){
            logger().error("Failed to receive countries' data from World Bank API", e);
            throw new WorldBankAPIRequestFailedException();
        } catch (JsonProcessingException e) {
            logger().error("Failed to map json to List<WorldBankCountry>. Response: {}", response, e);
            throw new FailedJsonMappingException();
        }
    }

    /**
     * Retrieves indicators from the all sources of the World Bank API filtered by country and years
     * @param indicator Indicator that contains the source from which the indicatorId is extracted
     * @param countryId World Bank country's Id
     * @param years List of years from which the values are form
     * @return List of WorldBankIndicator (this object is the same as the one given by the API)
     */
    public List<WorldBankIndicator> getIndicatorValues(Indicator indicator, String countryId, List<Integer> years) {
        logger().info("Requesting from the World Bank API data for the indicator: {} with countryId: {} and years: {}",
                indicator, countryId, years);
        if(indicator.getDataSource()!= null) {
            Matcher matcher = Pattern.compile("[A-Z.]{2,}").matcher(indicator.getDataSource());
            if (matcher.find()) {
                String indicatorId = matcher.group(0);
                try {
                    String FULL_URL = URL + "country/" + countryId + "/indicator/" + indicatorId + "?format=jsonstat";
                    logger().info("Requesting data from the World Bank API. URL: {} ", FULL_URL);
                    WorldDevelopmentIndicator response = restTemplate.getForObject(FULL_URL, WorldDevelopmentIndicator.class);

                    if (response == null || response.getWDI() == null) {
                        logger().error("Failed to receive indicator's values from World Bank API. IndicatorId: {}", indicatorId);
                        throw new WorldBankAPIRequestFailedException();
                    }
                    List<WorldBankIndicator> result = new ArrayList<>();
                    List<Integer> valuesList = response.getWDI().getValue();

                    String[] yearsList = response.getWDI().getDimension().getYear().getCategory().getLabel().values().toArray(new String[0]);
                    for (int i = 0; i < valuesList.size(); i++) {
                        // The API doesn't take multiple dates so the filtering is done by us
                        if (valuesList.get(i) != null && (years==null || years.isEmpty() || years.contains(Integer.parseInt(yearsList[i])))) {
                            result.add(WorldBankIndicator.builder().value(valuesList.get(i)).date(yearsList[i]).build());
                        }
                    }
                    return result;
                // The error response is in XML so it can't be parsed
                }catch (RestClientException e){
                    logger().error("Failed to receive indicator's values from World Bank API. IndicatorId: {}", indicatorId, e);
                    throw new WorldBankAPIRequestFailedException();
                }
            }
        }
        logger().error("Failed to extract indicator id from data source. DataSource: {}", indicator.getDataSource());
        throw new InvalidDataSourceException();
    }
}