package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
import com.arqaam.logframelab.repository.*;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndicatorsManagementServiceTest {

    @Mock
    IndicatorService indicatorService;

    @Mock
    LevelRepository levelRepository;

    @Mock
    CRSCodeRepository crsCodeRepository;
    @Mock
    SDGCodeRepository sdgCodeRepository;
    @Mock
    SourceRepository sourceRepository;

    @Mock
    IndicatorRepository indicatorRepository;

    @InjectMocks
    IndicatorsManagementService service;

    @BeforeEach
    void setup (){
    }

    @Test
    void getIndicators() {
    }

    @Test
    void saveIndicator() {
        IndicatorRequestDto indicatorRequestDto = IndicatorRequestDto.builder().build();
        service.saveIndicator(indicatorRequestDto);
        verify(levelRepository).findById(indicatorRequestDto.getLevelId());
        verify(indicatorRepository).save(any());
    }

    @Test
    void deleteIndicator() {
    }

    @Test
    void processFileWithTempIndicators() throws IOException {
        MockMultipartFile file = new MockMultipartFile("Indicators.xlsx", "Indicators.xlsx",
                MediaType.APPLICATION_OCTET_STREAM.toString(),
                new ClassPathResource("Indicators.xlsx").getInputStream());
        Indicator indicator2 = Indicator.builder().name("Fake Name 2").build();
        List<Indicator> extractedIndicators = new ArrayList<>();
        extractedIndicators.add(Indicator.builder().name("Fake Name").description("New description").build());
        extractedIndicators.add(indicator2);

        List<Indicator> indicators = Collections.singletonList(Indicator.builder().id(1L).name("Fake Name").description("Old description").build());
        when(indicatorService.extractIndicatorFromFile(file)).thenReturn(extractedIndicators);
        when(indicatorRepository.findAllByNameIn(any())).thenReturn(indicators);
        service.processFileWithTempIndicators(file);

        verify(indicatorService).extractIndicatorFromFile(file);
        verify(indicatorRepository, times(2)).exists(any());
        verify(indicatorRepository).findAllByNameIn(any());
        List<Indicator> expected = new ArrayList<>();
        expected.add(Indicator.builder().id(1L).name("Fake Name").description("New description").temp(true).build());
        expected.add(indicator2);
        verify(indicatorRepository).saveAll(expected);
    }

    @Test
    void getIndicatorsForApproval() {
    }

    @Test
    void processTempIndicatorsApproval() {
        List<IndicatorApprovalRequestDto.Approval> approvalList = new ArrayList<>();
        approvalList.add(new IndicatorApprovalRequestDto.Approval(1L, true));
        approvalList.add(new IndicatorApprovalRequestDto.Approval(2L, false));
        IndicatorApprovalRequestDto dto = new IndicatorApprovalRequestDto(approvalList);
        service.processTempIndicatorsApproval(dto);
        verify(indicatorRepository, times(1)).updateToApproved(any());
        verify(indicatorRepository, times(1)).deleteDisapprovedByIds(any());
    }

    @Test
    void processTempIndicatorsApproval_emptyDto() {
        IndicatorApprovalRequestDto dto = new IndicatorApprovalRequestDto(new ArrayList<>());
        service.processTempIndicatorsApproval(dto);
        verify(indicatorRepository, times(0)).updateToApproved(new ArrayList<>());
        verify(indicatorRepository, times(0)).deleteDisapprovedByIds(new ArrayList<>());
    }

    @Test
    void indicatorExists() {
    }

    @Test
    void getIndicator() {
    }
}