package com.arqaam.logframelab.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto.Approval;
import com.arqaam.logframelab.controller.dto.auth.UserDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningRequestDto;
import com.arqaam.logframelab.controller.dto.auth.create.UserAuthProvisioningResponseDto;
import com.arqaam.logframelab.model.persistence.CRSCode;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.SDGCode;
import com.arqaam.logframelab.model.persistence.Source;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
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

public class IndicatorsManagementControllerTest extends BaseControllerTest {

  public static final int INDICATOR_ADMIN_GROUP_ID = 3;
  public static final String INDICATOR_USERNAME = "indicator";
  public static final String INDICATOR_PASSWORD = "indicator";
  public static final String AUTH_USERS_URI = "/auth/users/";
  final static int DEFAULT_PAGE_SIZE = 10;
  final static int DEFAULT_PAGE = 1;
  private final static int DEFAULT_PAGE_INDEX = DEFAULT_PAGE - 1;
  private static final String INDICATORS_URI = "/indicators/";
  private static final String APPROVALS_URI = INDICATORS_URI + "approvals";
  //@Autowired
  //private IndicatorMapper indicatorMapper;
  @Autowired
  private IndicatorRepository indicatorRepository;

  private String indicatorsManagerToken;

  @BeforeEach
  void generateIndicatorManagerToken() {
    super.generateAuthToken();

    ResponseEntity<UserDto> userResponse = testRestTemplate
        .exchange(AUTH_USERS_URI + INDICATOR_USERNAME, HttpMethod.GET,
            new HttpEntity<>(super.headersWithAuth()),
            new ParameterizedTypeReference<UserDto>() {
            });

    if (userResponse.getStatusCode().is4xxClientError()) {
      UserAuthProvisioningRequestDto authProvisioningRequest =
          new UserAuthProvisioningRequestDto(INDICATOR_USERNAME, INDICATOR_PASSWORD,
              Collections.singletonList(INDICATOR_ADMIN_GROUP_ID));

      testRestTemplate.exchange(AUTH_USERS_URI, HttpMethod.POST,
          new HttpEntity<>(authProvisioningRequest, super.headersWithAuth()),
          UserAuthProvisioningResponseDto.class);
    }

    indicatorsManagerToken = token(INDICATOR_USERNAME, INDICATOR_PASSWORD);
  }

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
        indicatorToIndicatorRequestDto(indicator), headersWithAuth());

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
    IndicatorRequestDto request = IndicatorRequestDto.builder().crsCode(Collections.singleton(112L))
        .dataSource("https://data.worldbank.org/indicator/FB.ATM.TOTL.P5?view=chart")
        .keywords(
            "household expenditure per capita,family income,family expenditure,domestic household")
        .disaggregation(true)
        .levelId(3L)
        .name("Proportion of population reporting having personally felt discriminated against")
        .sdgCode(Collections.singleton(1L))
        .source(Collections.singleton(1L))
        .sourceVerification("Project's M&E system")
        .sector("Inequality")
        .build();

    ResponseEntity<Indicator> indicator = testRestTemplate
        .exchange(INDICATORS_URI, HttpMethod.POST, new HttpEntity<>(request, headersWithAuth()),
            Indicator.class);

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

    HttpEntity<Object> httpEntity = new HttpEntity<>(headersWithAuth());

    ResponseEntity<Void> deleteResponse = testRestTemplate
        .exchange(INDICATORS_URI + indicator.getId(), HttpMethod.DELETE, httpEntity, Void.class);

    ResponseEntity<Indicator> getResponse = testRestTemplate
        .exchange(INDICATORS_URI + indicator.getId(), HttpMethod.GET, httpEntity, Indicator.class);

    assertAll(
        () -> assertThat(deleteResponse.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(getResponse.getStatusCode(), is(HttpStatus.NOT_FOUND))
    );
  }

  @Test
  void whenFileUploadedForApproval_thenVerifyAddedToTemp() {
    ResponseEntity<Void> response = uploadIndicatorsForApproval();

    List<Indicator> tempIndicators = indicatorRepository.findAllByTempEquals(true);

    assertAll(
        () -> assertThat(response.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(tempIndicators, hasSize(427))
    );
  }

  @Test
  void whenTempIndicatorsRequested_thenVerifyResult() {
    ResponseEntity<ResponsePage<Indicator>> tempsForApproval = getTempIndicatorsForApproval();

    assertAll(
        () -> assertThat(tempsForApproval.getStatusCode(), is(HttpStatus.OK))
    );
  }

  @Test
  void whenTempIndicatorsApproved_thenVerifyResult() {
    ResponseEntity<ResponsePage<Indicator>> tempsForApproval = getTempIndicatorsForApproval();

    final Random random = new Random();
    List<Approval> approvals = Objects.requireNonNull(tempsForApproval.getBody()).get().map(temp ->
        new Approval(temp.getId(), random.nextBoolean())
    ).collect(Collectors.toList());

    IndicatorApprovalRequestDto approvalRequest =
        new IndicatorApprovalRequestDto(approvals);
    ResponseEntity<Void> approvalResponse = testRestTemplate
        .exchange(APPROVALS_URI, HttpMethod.POST,
            new HttpEntity<>(approvalRequest, headersWithAuth()), Void.class);

    assertAll(
        () -> assertThat(tempsForApproval.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(approvalResponse.getStatusCode(), is(HttpStatus.OK)),
        () -> assertThat(indicatorRepository
                .findAllById(approvals.stream().map(Approval::getId).collect(Collectors.toList())).stream().map(Indicator::isTemp).collect(
                Collectors.toList()), everyItem(is(false)))
    );
  }
  
  @Test
  void getIndicators() {
    /*ResponseEntity<ResponsePage<Indicator>> response = testRestTemplate
            .exchange("/indicators?filters.indicatorName=NUMBER&filters.sectors=Poverty&page=1&pageSize=10", HttpMethod.GET,
                    defaultHttpEntity, new ParameterizedTypeReference<>() {});
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(5,response.getBody().getTotalElements());
    assertEquals(5,response.getBody().getContent().size());
    assertTrue(response.getBody().getContent().stream().anyMatch(i -> i.getName().contains("Number")));
    assertTrue(response.getBody().getContent().stream().allMatch(i -> i.getName().toLowerCase().contains("number")));
    assertTrue(response.getBody().getContent().stream().allMatch(i -> i.getSector().toLowerCase().contains("poverty")));
    */
  }

  @Test
  void getIndicator() {
    ResponseEntity<Indicator> response = testRestTemplate
            .exchange("/indicators/47", HttpMethod.GET,
                    defaultHttpEntity, Indicator.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    assertEquals(47, response.getBody().getId());
    assertEquals("Adequacy of social protection and labor programs (% of total welfare of beneficiary households)", response.getBody().getName());
    assertEquals("https://data.worldbank.org/indicator/per_allsp.adq_pop_tot?view=chart", response.getBody().getDataSource());
    assertEquals("social security,social protection,social insurance,labor,labour,labor protection,labour protection,social welfare", response.getBody().getKeywords());
    assertEquals("Social Protection & Labour", response.getBody().getSector());
    assertEquals("World Bank Data", response.getBody().getSourceVerification());
    assertTrue(response.getBody().getSource().stream().allMatch(x->x.getName().equals("World Bank")));
    assertEquals(false, response.getBody().getDisaggregation());
    assertEquals(4, response.getBody().getLevel().getId());
    assertTrue(response.getBody().getCrsCode().isEmpty());
    assertTrue(response.getBody().getSdgCode().isEmpty());
    assertEquals("", response.getBody().getDescription());
  }

  private ResponseEntity<Void> uploadIndicatorsForApproval() {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource("Clusters.xlsx"));

    HttpHeaders headers = headersWithAuth();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return testRestTemplate
        .exchange(INDICATORS_URI + "upload", HttpMethod.POST, new HttpEntity<>(body, headers),
            Void.class);
  }

  private ResponseEntity<ResponsePage<Indicator>> getPageOfIndicators() {

    return testRestTemplate.exchange(uriForDefaultPage(INDICATORS_URI), HttpMethod.GET,
        new HttpEntity<>(headersWithAuth()),
        new ParameterizedTypeReference<ResponsePage<Indicator>>() {
        });
  }

  private ResponseEntity<ResponsePage<Indicator>> getTempIndicatorsForApproval() {
    uploadIndicatorsForApproval();

    return testRestTemplate
        .exchange(uriForDefaultPage(APPROVALS_URI), HttpMethod.GET,
            new HttpEntity<>(headersWithAuth()),
            new ParameterizedTypeReference<ResponsePage<Indicator>>() {
            });
  }

  private String uriForDefaultPage(String uri) {
    return UriComponentsBuilder.fromUriString(uri).queryParam("page", DEFAULT_PAGE)
        .queryParam("pageSize", DEFAULT_PAGE_SIZE).toUriString();
  }

  protected HttpHeaders headersWithAuth() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(indicatorsManagerToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    return headers;
  }

  private IndicatorRequestDto indicatorToIndicatorRequestDto(Indicator indicator){
    return IndicatorRequestDto.builder()
          .id(indicator.getId())
          .crsCode(indicator.getCrsCode().stream().map(CRSCode::getId).collect(Collectors.toSet()))
          .dataSource(indicator.getDataSource())
          .description(indicator.getDescription())
          .keywords(indicator.getKeywords())
          .name(indicator.getName())
          .sdgCode(indicator.getSdgCode().stream().map(SDGCode::getId).collect(Collectors.toSet()))
          .source(indicator.getSource().stream().map(Source::getId).collect(Collectors.toSet()))
          .sourceVerification(indicator.getSourceVerification())
          .sector(indicator.getSector())
          .levelId(indicator.getLevel().getId())
          .build();
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