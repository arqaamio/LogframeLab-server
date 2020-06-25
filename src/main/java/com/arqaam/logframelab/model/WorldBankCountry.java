package com.arqaam.logframelab.model;

import lombok.Data;

@Data
public class WorldBankCountry {

    private String id;
    private String iso2Code;
    private String name;
    private CountryProperty region;
    private CountryProperty adminRegion;
    private CountryProperty incomeLevel;
    private CountryProperty lendingType;
    private String capitalCity;
    private String longitude;
    private String latitude;


    @Data
    public static class CountryProperty {
        private String id;
        private String iso2code;
        private String value;
    }
}
