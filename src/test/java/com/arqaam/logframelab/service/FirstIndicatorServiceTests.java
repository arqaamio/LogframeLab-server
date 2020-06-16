package com.arqaam.logframelab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.apache.http.entity.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class FirstIndicatorServiceTests extends BaseIndicatorServiceTest {

  @Test
  void extractIndicatorsFromWordFile() throws IOException {
    when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
    List<IndicatorResponse> expectedResult = getExpectedResult();
    MultipartFile file = new MockMultipartFile("test_doc.docx", "test_doc.docx",
        ContentType.APPLICATION_OCTET_STREAM.toString(),
        new ClassPathResource("test_doc.docx").getInputStream());
    List<IndicatorResponse> result = indicatorService.extractIndicatorsFromWordFile(file, null);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(expectedResult, result);
  }

  @Test
  void extractIndicatorsFromWordFile_doc() throws IOException {
    when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
    List<IndicatorResponse> expectedResult = getExpectedResult();
    MultipartFile file = new MockMultipartFile("test_doc.doc", "test_doc.doc",
        ContentType.APPLICATION_OCTET_STREAM.toString(),
        new ClassPathResource("test_doc.doc").getInputStream());
    List<IndicatorResponse> result = indicatorService.extractIndicatorsFromWordFile(file, null);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(expectedResult, result);
  }

  @Test
  void checkIndicators() {
    List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
    List<Indicator> indicators = mockIndicatorList();
    // Test also indicators without keyword
    indicators.add(Indicator.builder().id(0L).name("Name").description("Description").build());
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);
    indicators = indicators.stream().sorted(Comparator.comparing(Indicator::getId))
        .collect(Collectors.toList());
    assertEquals(indicators.size() - 1, mapResult.size());
    for (int i = 0; i < mapResult.values().size(); i++) {
      assertEquals(indicators.get(i + 1), mapResult.values().toArray()[i]);
    }
  }

  @Test
  void checkIndicators_withIndicatorsWithSameId() {
    List<String> keywordsPolicyList = new ArrayList<>();
    keywordsPolicyList.add("policy");
    List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
    List<Indicator> indicators = mockIndicatorList();
    indicators.add(Indicator.builder().id(73L).name(
        "Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
        .description("Digitalisation").keywords("policy").keywordsList(keywordsPolicyList).build());
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);
    indicators = indicators.stream().sorted(Comparator.comparing(Indicator::getId))
        .collect(Collectors.toList());
    assertEquals(indicators.size() - 1, mapResult.size());
    for (int i = 0; i < indicators.size() - 1; i++) {
      assertEquals(indicators.get(i), mapResult.values().toArray()[i]);
    }
  }

  @Test
  void checkIndicators_withoutIndicators() {
    List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
    List<Indicator> indicators = new ArrayList<>();
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

    assertEquals(0, mapResult.size());
  }

  @Test
  void checkIndicators_withoutWordsToScan() {
    List<String> wordsToScan = new ArrayList<>();
    List<Indicator> indicators = new ArrayList<>();
    Map<Long, Indicator> mapResult = new HashMap<>();
    indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

    assertTrue(mapResult.isEmpty());
  }

  @Test
  void exportIndicatorsInWordFile() throws Docx4JException, JAXBException {
    List<IndicatorResponse> indicators = createListIndicatorResponse();
    ByteArrayOutputStream result = indicatorService.exportIndicatorsInWordFile(indicators);
    assertNotNull(result);

    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
        .load(new ByteArrayInputStream(result.toByteArray()));
    List<Object> textNodes = wordMLPackage.getMainDocumentPart()
        .getJAXBNodesViaXPath("//w:t", true);
    boolean valid = false;
    int c = 0;
    for (Object obj : textNodes) {
      String currentText = ((Text) ((JAXBElement) obj).getValue()).getValue();
      if (currentText.equals(indicators.get(c).getName())) {
        c++;
        if (c == indicators.size()) {
          valid = true;
          break;
        }
      }
//            System.out.println(currentText);
    }
    assertTrue(valid);
  }

  @Test
  void parseVarWithValue_withMatchingText() {
    String text = "var";
    List<IndicatorResponse> indicators = createListIndicatorResponse();
    Text textNode = new Text();
    textNode.setParent(new R());
    indicatorService.parseVarWithValue(textNode, text, indicators);
    R result = (R) textNode.getParent();
    assertEquals(5 * indicators.size(), result.getContent().size());
    for (int i = 0; i < indicators.size() * 5; i += 5) {
      assertEquals(indicators.get(i / 5).getName(), ((Text) result.getContent().get(i)).getValue());
      assertTrue(result.getContent().get(i + 1) instanceof Br);
      assertTrue(result.getContent().get(i + 2) instanceof Br);
      assertTrue(result.getContent().get(i + 3) instanceof Br);
      assertTrue(result.getContent().get(i + 4) instanceof Br);
    }
  }

  @Test
  void parseVarWithValue_withoutMatchingText() {
    String text = "text without matching";
    List<IndicatorResponse> indicators = createListIndicatorResponse();
    Text textNode = new Text();
    textNode.setParent(new R());
    indicatorService.parseVarWithValue(textNode, text, indicators);
    R result = (R) textNode.getParent();
    assertEquals(0, result.getContent().size());
  }

  @Test
  void parseVarWithValue_withoutIndicatorResponse() {
    String text = "var";
    Text textNode = new Text();
    textNode.setParent(new R());
    indicatorService.parseVarWithValue(textNode, text, null);
    R result = (R) textNode.getParent();
    assertEquals(0, result.getContent().size());

    Text textNode2 = new Text();
    textNode2.setParent(new R());
    R result2 = (R) textNode2.getParent();
    indicatorService.parseVarWithValue(textNode2, text, Collections.emptyList());
    assertEquals(0, result2.getContent().size());
  }

  @Test
  void importIndicators() {
    //TODO this test
//        indicatorService.importIndicators(new ClassPathResource("Indicator.xlsx").getPath());

//        indicatorService.importIndicators("/home/ari/Downloads/Indicator.xlsx");
//        indicatorService.importIndicators("/home/ari/Downloads/SDGs_changed.xlsx");

  }

  @Test
  void exportIndicatorsInWorksheet() {
    List<Indicator> expectedResult = mockIndicatorList();

    when(indicatorRepository.findAllById(any())).thenReturn(expectedResult);
    ByteArrayOutputStream outputStream = indicatorService
        .exportIndicatorsInWorksheet(createListIndicatorResponse());
//        MultipartFile multipartFile = new MockMultipartFile("indicators_export.xlsx", outputStream.toByteArray());
//        List<Indicator> result = indicatorService.importIndicators(multipartFile);
//
//        // because Id in the result is null, and in the expected result it isn't.
//        for (int i = 0; i < expectedResult.size(); i++) {
//            assertEquals(expectedResult.get(i).getLevel(), result.get(i).getLevel());
//            assertEquals(expectedResult.get(i).getKeywordsList(), result.get(i).getKeywordsList());
//            assertEquals(expectedResult.get(i).getDisaggregation(), result.get(i).getDisaggregation());
//            assertEquals(expectedResult.get(i).getCrsCode(), result.get(i).getCrsCode());
//            assertEquals(expectedResult.get(i).getDescription(), result.get(i).getDescription());
//            assertEquals(expectedResult.get(i).getName(), result.get(i).getName());
//            assertEquals(expectedResult.get(i).getSdgCode(), result.get(i).getSdgCode());
//            assertEquals(expectedResult.get(i).getSource(), result.get(i).getSource());
//            assertEquals(expectedResult.get(i).getThemes(), result.get(i).getThemes());
//            assertEquals(expectedResult.get(i).getDataSource(), result.get(i).getDataSource());
//            assertEquals(expectedResult.get(i).getSourceVerification(), result.get(i).getSourceVerification());
//        }

//        try(OutputStream fileOutputStream = new FileOutputStream("thefilename.xlsx")) {
//            outputStream.writeTo(fileOutputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
  }


  List<Indicator> mockIndicatorList() {
    String keyword = "food insecurity,agriculture";
    List<Indicator> list = new ArrayList<>();

    List<String> keywordsFoodList = new ArrayList<>();
    keywordsFoodList.add("agriculture");
    keywordsFoodList.add("food");

    List<String> keywordsPolicyList = new ArrayList<>();
    keywordsPolicyList.add("policy");

    List<String> keywordsGovList = new ArrayList<>();
    keywordsGovList.add("government");

    List<String> keywordsGovPolicyList = new ArrayList<>();
    keywordsGovPolicyList.add("government policies");
    keywordsGovPolicyList.add("policy");

    list.add(Indicator.builder().id(4L).name(
        "Number of policies/strategies/laws/regulation developed/revised for digitalisation with EU support")
        .description("Digitalisation").level(mockLevels[0]).keywords("policy")
        .keywordsList(keywordsPolicyList)
        .source(mockSources.get(0)).themes(mockThemes.get(0)).sdgCode(mockSdgCodes.get(0))
        .crsCode(mockCrsCodes.get(0)).build());
    list.add(Indicator.builder().id(73L).name(
        "Number of government policies developed or revised with civil society organisation participation through EU support")
        .description("Public Sector").level(mockLevels[1]).keywords("government policies, policy")
        .keywordsList(keywordsGovPolicyList)
        .source(mockSources.get(1)).themes(mockThemes.get(1)).sdgCode(mockSdgCodes.get(1))
        .crsCode(mockCrsCodes.get(1)).build());
    list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
        .description("Public Sector").level(mockLevels[3]).keywords("government")
        .keywordsList(keywordsGovList)
        .source(mockSources.get(2)).themes(mockThemes.get(2)).sdgCode(mockSdgCodes.get(2))
        .crsCode(mockCrsCodes.get(2)).build());
    list.add(
        Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
            .description("Food & Agriculture").level(mockLevels[1]).keywords(keyword)
            .keywordsList(keywordsFoodList)
            .source(mockSources.get(3)).themes(mockThemes.get(3)).sdgCode(mockSdgCodes.get(3))
            .crsCode(mockCrsCodes.get(3)).build());

    return list;
  }


  private List<IndicatorResponse> createListIndicatorResponse() {
    List<IndicatorResponse> list = new ArrayList<>();
    for (int i = 1; i < 6; i++) {
      list.add(IndicatorResponse.builder().id(i).level("IMPACT").color("color").name("Label " + i)
          .description("Description").var("var").build());
    }
    return list;
  }

  @Test
  void getIndicators() {
    List<Indicator> expectedResult = mockIndicatorList().stream()
        .filter(
            x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId())
                && mockSources.contains(x.getSource())
                && mockSdgCodes.contains(x.getSdgCode()) && mockCrsCodes.contains(x.getCrsCode()))
        .collect(Collectors.toList());

    List<Indicator> result = indicatorService.getIndicators(Optional.of(mockThemes),
        Optional.of(mockSources), Optional.of(mockLevelsId), Optional.of(mockSdgCodes),
        Optional.of(mockCrsCodes));
    verify(indicatorRepository).findAll(any(Specification.class));
    verify(indicatorRepository, times(0)).findAll();
    assertEquals(expectedResult, result);
  }

  @Test
  void getIndicators_someFilters() {
    when(indicatorRepository.findAll(any(Specification.class))).
        thenReturn(mockIndicatorList().stream()
            .filter(x -> mockThemes.contains(x.getThemes()) && mockLevelsId
                .contains(x.getLevel().getId()) && mockSources.contains(x.getSource())
            ).collect(Collectors.toList()));

    List<Indicator> expectedResult = mockIndicatorList().stream()
        .filter(
            x -> mockThemes.contains(x.getThemes()) && mockLevelsId.contains(x.getLevel().getId())
                && mockSources.contains(x.getSource())
        ).collect(Collectors.toList());

    List<Indicator> result = indicatorService.getIndicators(Optional.of(mockThemes),
        Optional.of(mockSources), Optional.of(mockLevelsId), Optional.empty(), Optional.empty());
    verify(indicatorRepository).findAll(any(Specification.class));
    verify(indicatorRepository, times(0)).findAll();
    assertEquals(expectedResult, result);
  }

  @Test
  void getIndicators_noFilter() {
    List<Indicator> expectedResult = mockIndicatorList();
    List<Indicator> result = indicatorService
        .getIndicators(Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty());
    verify(indicatorRepository, times(0)).findAll(any(Specification.class));
    verify(indicatorRepository).findAll();
    assertEquals(expectedResult, result);
  }
}
