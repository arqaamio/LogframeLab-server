package com.arqaam.logframelab.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.TempIndicatorApprovalRequestDto;
import com.arqaam.logframelab.controller.dto.TempIndicatorApprovalRequestDto.Approval;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.TempIndicator;
import com.arqaam.logframelab.repository.TempIndicatorRepository;
import com.arqaam.logframelab.repository.initializer.BaseDatabaseTest;
import com.arqaam.logframelab.service.IndicatorMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class IndicatorsManagementControllerTest extends BaseControllerTest implements
    BaseDatabaseTest {

  private final static int DEFAULT_PAGE_SIZE = 10;
  private final static int DEFAULT_PAGE = 1;
  private final static int DEFAULT_PAGE_INDEX = DEFAULT_PAGE - 1;
  private static final String INDICATORS_URI = "/indicators/";
  private static final String APPROVALS_URI = INDICATORS_URI + "approvals";

  @Autowired
  private IndicatorMapper indicatorMapper;

  @Autowired
  private TempIndicatorRepository tempIndicatorRepository;

  @Test
  void whenIndicatorsByPageRequested_thenVerifyResult() {
    ResponseEntity<ResponsePage<Indicator>> indicatorsPerPageResponse = getPageOfIndicators();

    assertAll(
        () -> assertThat(indicatorsPerPageResponse.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(Objects.requireNonNull(indicatorsPerPageResponse.getBody()).getContent(),
            hasSize(DEFAULT_PAGE_SIZE)),
        () -> assertThat(indicatorsPerPageResponse.getBody().getPageable().getPageNumber(),
            is(DEFAULT_PAGE_INDEX))
    );
  }

  @Test
  void whenExistingIndicatorUpdated_thenVerifyChanges() {
    ResponseEntity<ResponsePage<Indicator>> indicatorsPerPageResponse = getPageOfIndicators();

    Indicator indicator = Objects.requireNonNull(indicatorsPerPageResponse.getBody()).getContent()
        .get(new Random().nextInt(DEFAULT_PAGE_SIZE));

    String nameBeforeUpdate = indicator.getName();
    Boolean disaggregationBeforeUpdate = indicator.getDisaggregation();

    indicator.setName(nameBeforeUpdate + "addendum");
    indicator.setDisaggregation(!disaggregationBeforeUpdate);

    HttpEntity<IndicatorRequestDto> httpEntity = new HttpEntity<>(
        indicatorMapper.indicatorToIndicatorRequestDto(indicator));

    ResponseEntity<Indicator> updatedIndicatorResponse = testRestTemplate
        .exchange(INDICATORS_URI, HttpMethod.PUT, httpEntity, Indicator.class);

    assertAll(
        () -> assertThat(updatedIndicatorResponse.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(Objects.requireNonNull(updatedIndicatorResponse.getBody()).getName(),
            is(not(nameBeforeUpdate))),
        () -> assertThat(
            Objects.requireNonNull(updatedIndicatorResponse.getBody()).getDisaggregation(),
            not(disaggregationBeforeUpdate))
    );
  }

  @Test
  void whenNewIndicatorSaved_thenVerifyAdded() {
    IndicatorRequestDto request = IndicatorRequestDto.builder().crsCode("99810.0")
        .dataSource("https://data.worldbank.org/indicator/FB.ATM.TOTL.P5?view=chart")
        .keywords(
            "household expenditure per capita,family income,family expenditure,domestic household")
        .disaggregation(true)
        .levelId(3L)
        .name("Proportion of population reporting having personally felt discriminated against")
        .sdgCode("10.4")
        .source("UN Sustainable Development Goals")
        .sourceVerification("Project's M&E system")
        .themes("Inequality")
        .build();

    ResponseEntity<Indicator> indicator = testRestTemplate
        .exchange(INDICATORS_URI, HttpMethod.POST, new HttpEntity<>(request), Indicator.class);

    assertAll(
        () -> assertThat(indicator.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(Objects.requireNonNull(indicator.getBody()).getId(), is(notNullValue())),
        () -> assertThat(indicator.getBody().getId(), is(greaterThan(0L)))
    );
  }

  @Test
  void whenExistingIndicatorDeleted_thenVerifyDeleted() {
    ResponseEntity<ResponsePage<Indicator>> indicatorsPerPageResponse = getPageOfIndicators();

    Indicator indicator = Objects.requireNonNull(indicatorsPerPageResponse.getBody()).getContent()
        .get(new Random().nextInt(DEFAULT_PAGE_SIZE));

    ResponseEntity<Void> deleteResponse = testRestTemplate
        .exchange(INDICATORS_URI + indicator.getId(), HttpMethod.DELETE, null, Void.class);

    ResponseEntity<Indicator> getResponse = testRestTemplate
        .exchange(INDICATORS_URI + indicator.getId(), HttpMethod.GET, null, Indicator.class);

    assertAll(
        () -> assertThat(deleteResponse.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(getResponse.getStatusCode(), is(HttpStatus.NOT_FOUND))
    );
  }

  @Test
  void whenFileUploadedForApproval_thenVerifyAddedToTemp() {
    ResponseEntity<Void> response = uploadIndicatorsForApproval();

    List<TempIndicator> tempIndicators = tempIndicatorRepository.findAll();

    assertAll(
        () -> assertThat(response.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(tempIndicators, hasSize(427))
    );
  }

  @Test
  void whenTempIndicatorsRequested_thenVerifyResult() {
    ResponseEntity<ResponsePage<TempIndicator>> tempsForApproval = getTempIndicatorsForApproval();

    assertAll(
        () -> assertThat(tempsForApproval.getStatusCode(), is(HttpStatus.OK))
    );
  }

  @Test
  void whenTempIndicatorsApproved_thenVerifyResult() {
    ResponseEntity<ResponsePage<TempIndicator>> tempsForApproval = getTempIndicatorsForApproval();

    final Random random = new Random();
    List<Approval> approvals = Objects.requireNonNull(tempsForApproval.getBody()).get().map(temp ->
        new Approval(temp.getId(), random.nextBoolean())
    ).collect(Collectors.toList());

    TempIndicatorApprovalRequestDto approvalRequest =
        new TempIndicatorApprovalRequestDto(approvals);
    ResponseEntity<Void> approvalResponse = testRestTemplate
        .exchange(APPROVALS_URI, HttpMethod.POST, new HttpEntity<>(approvalRequest), Void.class);

    ;

    assertAll(
        () -> assertThat(tempsForApproval.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(approvalResponse.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(tempIndicatorRepository
                .findAllById(approvals.stream().map(Approval::getId).collect(Collectors.toList())),
            is(empty()))
    );
  }

  private ResponseEntity<Void> uploadIndicatorsForApproval() {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("Clusters.xlsx"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return testRestTemplate
        .exchange(INDICATORS_URI + "upload", HttpMethod.POST, new HttpEntity<>(body, headers),
            Void.class);
  }

  private ResponseEntity<ResponsePage<Indicator>> getPageOfIndicators() {
    return testRestTemplate.exchange(uriForDefaultPage(INDICATORS_URI), HttpMethod.GET, null,
        new ParameterizedTypeReference<ResponsePage<Indicator>>() {
        });
  }

  private String uriForDefaultPage(String uri) {
    return UriComponentsBuilder.fromUriString(uri).queryParam("page", DEFAULT_PAGE)
        .queryParam("pageSize", DEFAULT_PAGE_SIZE).toUriString();
  }

  private ResponseEntity<ResponsePage<TempIndicator>> getTempIndicatorsForApproval() {
    uploadIndicatorsForApproval();

    return testRestTemplate
        .exchange(uriForDefaultPage(APPROVALS_URI), HttpMethod.GET, null,
            new ParameterizedTypeReference<ResponsePage<TempIndicator>>() {
            });
  }
}

/*
 * This class is necessary for test cases that expect Spring's Page<T> response. Jackson is unable
 * to serialize the default PageImpl.
 * Solution found at https://stackoverflow.com/a/52509886/2211446
 */
class ResponsePage<T> extends PageImpl<T> {

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public ResponsePage(@JsonProperty("content") List<T> content, @JsonProperty("number") int number,
      @JsonProperty("size") int size,
      @JsonProperty("totalElements") Long totalElements,
      @JsonProperty("pageable") JsonNode pageable, @JsonProperty("last") boolean last,
      @JsonProperty("totalPages") int totalPages, @JsonProperty("sort") JsonNode sort,
      @JsonProperty("first") boolean first,
      @JsonProperty("numberOfElements") int numberOfElements) {
    super(content, PageRequest.of(number, size), totalElements);
  }

  public ResponsePage(List<T> content, Pageable pageable, long total) {
    super(content, pageable, total);
  }

  public ResponsePage(List<T> content) {
    super(content);
  }

  public ResponsePage() {
    super(new ArrayList<T>());
  }
}
