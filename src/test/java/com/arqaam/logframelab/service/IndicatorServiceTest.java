package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.IndicatorResponse;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.model.persistence.Level;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class IndicatorServiceTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private LevelRepository levelRepository;

    @InjectMocks
    private IndicatorService indicatorService;

    private final static Level[] mockLevels = new Level[]{
        new Level(1L, "OUTPUT", "OUTPUT", "{output}", "green", 3),
        new Level(2L, "OUTCOME", "OUTCOME", "{outcomes}", "red", 2),
        new Level(3L, "OTHER_OUTCOMES", "OTHER OUTCOMES", "{otheroutcomes}", "orange", 4),
        new Level(4L, "IMPACT", "IMPACT", "{impact}", "purple", 1)
    };

    @BeforeEach
    void setup(){
        when(levelRepository.findAll()).thenReturn(Arrays.asList(mockLevels));
        when(levelRepository.findAllByOrderByPriority()).thenReturn(Arrays.stream(mockLevels).sorted().collect(Collectors.toList()));
    }

    @Test
    void extractIndicatorsFromWordFile() throws IOException {
        when(indicatorRepository.findAll()).thenReturn(mockIndicatorList());
        List<IndicatorResponse> expectedResult = new ArrayList<>();
        expectedResult.add(IndicatorResponse.builder().build());
        MultipartFile file = new MockMultipartFile("test_doc.docx", new ClassPathResource("test_doc.docx").getInputStream());
        List<IndicatorResponse> result = indicatorService.extractIndicatorsFromWordFile(file, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for(IndicatorResponse response : result){
            System.out.println(response);
        }
//        assertEquals(1, result.size());
    }

    @Test
    void checkIndicators() {
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = mockIndicatorList();
        // Test also indicators without keyword
        indicators.add(Indicator.builder().id(6L).name("Name").description("Description").build());
        Map<Long, Indicator> mapResult = new HashMap<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

        assertEquals(3, mapResult.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(indicators.get(i+1), mapResult.values().toArray()[i]);
        }
    }

    @Test
    void checkIndicators_withIndicatorsWithSameId() {
        List<String> keywordsPolicyList = new ArrayList<>();
        keywordsPolicyList.add("policy");
        List<String> wordsToScan = Arrays.asList("food", "government", "policy", "retirement");
        List<Indicator> indicators = mockIndicatorList();
        indicators.add(Indicator.builder().id(4L).name("Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
                .description("Digitalisation").keywords("policy").keywordsList(keywordsPolicyList).build());
        Map<Long, Indicator> mapResult = new HashMap<>();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult);

        assertEquals(3, mapResult.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(indicators.get(i+1), mapResult.values().toArray()[i]);
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

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(result.toByteArray()));
        List<Object> textNodes = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath("//w:t", true);
        boolean valid = false;
        int c = 0;
        for (Object obj : textNodes) {
            String currentText = ((Text) ((JAXBElement) obj).getValue()).getValue();
            if(currentText.equals(indicators.get(c).getLabel())) {
                c++;
                if(c == indicators.size()){
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
        //TODO this test
//        indicatorService.importIndicators(new ClassPathResource("Indicator.xlsx").getPath());

//        indicatorService.importIndicators("/home/ari/Downloads/Indicator.xlsx");
//        indicatorService.importIndicators("/home/ari/Downloads/SDGs_changed.xlsx");

    }


    private List<Indicator> mockIndicatorList() {
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

        list.add(Indicator.builder().id(1L).name("Number of food insecure people receiving EU assistance")
                .description("Food & Agriculture").keywords(keyword).level(mockLevels[1]).keywordsList(keywordsList).build());
        list.add(Indicator.builder().id(4L).name("Number of policies/strategies/laws/regulation developed/revised for digitalization with EU support")
                .description("Digitalisation").keywords("policy").level(mockLevels[0]).keywordsList(keywordsPolicyList).build());
        list.add(Indicator.builder().id(5L).name("Revenue, excluding grants (% of GDP)")
                .description("Public Sector").keywords("government").level(mockLevels[3]).keywordsList(keywordsGovList).build());
        list.add(Indicator.builder().id(73L).name("Number of government policies developed or revised with civil society organisation participation through EU support")
                .description("Public Sector").keywords("government policies, policy").level(mockLevels[1]).keywordsList(keywordsGovPolicyList).build());

        return list;
    }

    private List<IndicatorResponse> createListIndicatorResponse() {
        List<IndicatorResponse> list = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            list.add(IndicatorResponse.builder().id(i).level("IMPACT").color("color").label("Label "+i)
                    .description("Description").var("var").build());
        }
        return list;
    }
}
