package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import org.docx4j.wml.Br;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class IndicatorServiceTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @InjectMocks
    private IndicatorService indicatorService;

    //TODO because the word file is deleted then the other tests that need that file won't run successfully
    @Test
    void extractIndicatorsFromWordFile() throws IOException {
//        when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
//        String path = new ClassPathResource("test_doc.docx").getURI().getPath();
////        Path path_ = Paths.get(path);
////        File mock = mock(File.class);
//////        doNothing().when(mock.delete());
////        when(mock.delete()).thenReturn(false);
//        File mock =new File(path);
//        mock = spy(mock);
////        File mock = mock(File.class, path);
//
////        doNothing().when(mock.delete());
////        when(mock.is)
//        when(mock.getPath()).thenReturn(path);
//        when(mock.delete()).thenReturn(false);
//        Path path_ = mock(Path.class);
//        when(path_.toFile()).thenReturn(mock);
////        Paths.get(path);
//
////        doNothing().when(path_.toFile().delete());
//        List<IndicatorResponse> result = indicatorService.extractIndicatorsFromWordFile(path_);
//        assertEquals(1, result.size());
    }

    @Test
    void checkIndicators() {
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = mockIndicatorList();
        // Test also indicators without keyword
        indicators.add(new Indicator(6L, "Name", "Description", "", new Level(), null));
        Map<Long, IndicatorResponse> mapResult = new HashMap<>();
        List<IndicatorResponse> result = new ArrayList<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult, result);

        assertEquals(3, mapResult.size());
        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(indicators.get(i+1).getName(), result.get(i).getLabel());
            assertEquals(indicators.get(i+1).getDescription(), result.get(i).getDescription());
            assertEquals(indicators.get(i+1).getLevel().getTemplateVar(), result.get(i).getVar());
        }
    }

    @Test
    void checkIndicators_withIndicatorsWithSameId() {
        List<String> keywordsPolicyList = new ArrayList<>();
        keywordsPolicyList.add("policy");
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = mockIndicatorList();
        indicators.add(new Indicator(4L,"Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support",
                "Digitalization", "policy", new Level(), keywordsPolicyList));
        Map<Long, IndicatorResponse> mapResult = new HashMap<>();
        List<IndicatorResponse> result = new ArrayList<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult, result);

        assertEquals(3, mapResult.size());
        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(indicators.get(i+1).getName(), result.get(i).getLabel());
            assertEquals(indicators.get(i+1).getDescription(), result.get(i).getDescription());
            assertEquals(indicators.get(i+1).getLevel().getTemplateVar(), result.get(i).getVar());
        }
    }

    @Test
    void checkIndicators_withoutIndicators() {
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = new ArrayList<>();
        Map<Long, IndicatorResponse> mapResult = new HashMap<>();
        List<IndicatorResponse> result = new ArrayList<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult, result);

        assertEquals(0, mapResult.size());
        assertEquals(0, result.size());
    }

    //TODO add validation for this test. Should send an exception
    @Test
    void checkIndicators_withoutWordsToScan() {
//        List<String> wordsToScan = new ArrayList<>();
//        List<Indicator> indicators = new ArrayList<>();
//        Map<Long, IndicatorResponse> mapResult = new HashMap<>();
//        List<IndicatorResponse> result = new ArrayList<>();
//        indicatorService.checkIndicators(wordsToScan, indicators, mapResult, result);
    }

    //TODO can't find the template but just prints stack trace. Need exception handling
    @Test
    void exportIndicatorsInWordFile() {
//        List<IndicatorResponse> indicators = createListIndicatorResponse();
//        ByteArrayOutputStream result = indicatorService.exportIndicatorsInWordFile(indicators);

    }

    @Test
    void parseVarWithValue_withMatchingText() {
        String text = "var";
        List<IndicatorResponse> indicators = createListIndicatorResponse();
        Text textNode = new Text();
        textNode.setParent(new R());
        indicatorService.parseVarWithValue(textNode, text, indicators);
        R result = (R) textNode.getParent();
        assertEquals(5*indicators.size(), result.getContent().size());
        for (int i = 0; i < indicators.size()*5; i+=5) {
            assertEquals(indicators.get(i/5).getLabel(), ((Text)result.getContent().get(i)).getValue());
            assertTrue(result.getContent().get(i+1) instanceof Br);
            assertTrue(result.getContent().get(i+2) instanceof Br);
            assertTrue(result.getContent().get(i+3) instanceof Br);
            assertTrue(result.getContent().get(i+4) instanceof Br);
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
    }

    private List<Indicator> createListIndicator() {
        List<String> keywordList = new ArrayList<>();
        keywordList.add("var");
        keywordList.add("template");
        keywordList.add("economy");

        List<Indicator> list = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            list.add(Indicator.builder()
                    .id((long) i)
                    .level(Level.builder()
                            .id(1L)
                            .color("color")
                            .name("IMPACT")
                            .description("Description")
                            .templateVar("templatevar").build())
                    .description("Description")
                    .name("Name")
                    .keywordsList(keywordList)
                    .build());
        }
        return list;
    }

    private List<Indicator> mockIndicatorList() {
        Level level1 = new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green");
        Level level2 = new Level(2L, "OUTCOME", "OUTCOME", "{outcomes}", "red");
        Level level3 = new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", "{otheroutcomes}", "orange");
        Level level4 = new Level(4L, "IMPACT", "IMPACT", "{impact}", "purple");
        String keyword = "food insecurity,nutrition,farming,agriculture";
        List<Indicator> list = new ArrayList<>();

        List<String> keywordsList = new ArrayList<>();
        keywordsList.add("agriculture");
        keywordsList.add("nutrition");

        List<String> keywordsPolicyList = new ArrayList<>();
        keywordsPolicyList.add("policy");

        List<String> keywordsGovList = new ArrayList<>();
        keywordsGovList.add("government");

        List<String> keywordsGovPolicyList = new ArrayList<>();
        keywordsGovPolicyList.add("government policies");
        keywordsGovPolicyList.add("policy");

        list.add(new Indicator(1L,"Number of food insecure people receiving EU assistance",
                "Food & Agriculture", keyword, level2, keywordsList));
        list.add(new Indicator(4L,"Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support",
                "Digitalization", "policy", level1, keywordsPolicyList));
        list.add(new Indicator(5L, "Revenue, excluding grants (% of GDP)",
                "Public Sector", "government", level4, keywordsGovList));
        list.add(new Indicator(73L, "Number of government policies developed or revised with civil society organisation participation through EU support",
                "Public Sector", "government policies, policy", level2, keywordsGovPolicyList));
        return list;
    }

    private List<IndicatorResponse> createListIndicatorResponse() {
        List<IndicatorResponse> list = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            list.add(new IndicatorResponse(i, "IMPACT", "color", "Label", "Description", Collections.emptyList(), "var"));
        }
        return list;
    }
}
