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

import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
public class IndicatorServiceTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @InjectMocks
    private IndicatorService indicatorService;

    @Test
    void extractIndicatorsFormWordFile() {
        when(indicatorRepository.findAll()).thenReturn(createListIndicator());
    }

    @Test
    void checkIndicators() {
        List<String> wordsToScan = Arrays.asList("words", "to", "scan", "var");
        List<Indicator> indicators = createListIndicator();
        Map<Long, IndicatorResponse> mapResult = new HashMap<>();
        List<IndicatorResponse> result = new ArrayList();
        indicatorService.checkIndicators(wordsToScan, indicators, mapResult, result);

//        assertEquals("", 15, result.size());
    }

    @Test
    void exportIndicatorsInWordFile() {
//        List<IndicatorResponse> indicators = createListIndicatorResponse();
//        ByteArrayOutputStream result = indicatorService.exportIndicatorsInWordFile(indicators);
//        result.
    }

    @Test
    void parseVarWithValue_withMatchingText() {
        String text = "var";
        List<IndicatorResponse> indicators = createListIndicatorResponse();
        Text textNode = new Text();
        textNode.setParent(new R());
        indicatorService.parseVarWithValue(textNode, text, indicators);
        R result = (R) textNode.getParent();
        assertEquals("The length should be 5*NumberofIndicators", 5*indicators.size(), result.getContent().size());
        for (int i = 0; i < indicators.size()*5; i+=5) {
            assertEquals("", indicators.get(i/5).getLabel(), ((Text)result.getContent().get(i)).getValue());
            assertTrue("", result.getContent().get(i+1) instanceof Br);
            assertTrue("", result.getContent().get(i+2) instanceof Br);
            assertTrue("", result.getContent().get(i+3) instanceof Br);
            assertTrue("", result.getContent().get(i+4) instanceof Br);
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
        assertEquals("Without matching text, the size should be 0", 0, result.getContent().size());
    }

    @Test
    void parseVarWithValue_withoutIndicatorResponse() {
        String text = "var";
        Text textNode = new Text();
        textNode.setParent(new R());
        indicatorService.parseVarWithValue(textNode, text, null);
        R result = (R) textNode.getParent();
        assertEquals("Without indicator response, the size should be 0", 0, result.getContent().size());

        Text textNode2 = new Text();
        textNode2.setParent(new R());
        R result2 = (R) textNode2.getParent();
        indicatorService.parseVarWithValue(textNode2, text, Collections.emptyList());
        assertEquals("With indicator response with no items, the size should be 0", 0, result2.getContent().size());
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
    private List<IndicatorResponse> createListIndicatorResponse() {
        List<IndicatorResponse> list = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            list.add(new IndicatorResponse(i, "IMPACT", "color", "Label", "Description", Collections.emptyList(), "var"));
        }
        return list;
    }
}
