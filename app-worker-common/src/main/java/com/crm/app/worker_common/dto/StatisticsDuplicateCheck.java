package com.crm.app.worker_common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // optional, aber praktisch
public class StatisticsDuplicateCheck {

    @JsonProperty("nEntries")
    long nEntries = 0;

    @JsonProperty("nDuplicateAccountNames")
    long nDuplicateAccountNames = 0;

    @JsonProperty("nAddressMatchesProbable")
    long nAddressMatchesProbable = 0;

    @JsonProperty("nAddressMatchesPossible")
    long nAddressMatchesPossible = 0;

    @JsonProperty("nEmailMatches")
    long nEmailMatches = 0;
}