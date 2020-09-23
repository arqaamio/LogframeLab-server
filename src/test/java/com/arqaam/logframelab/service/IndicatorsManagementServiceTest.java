package com.arqaam.logframelab.service;

import com.arqaam.logframelab.controller.dto.IndicatorApprovalRequestDto;
import com.arqaam.logframelab.controller.dto.IndicatorRequestDto;
import com.arqaam.logframelab.model.persistence.Indicator;
import com.arqaam.logframelab.repository.IndicatorRepository;
import com.arqaam.logframelab.repository.LevelRepository;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndicatorsManagementServiceTest {

    @Mock
    IndicatorService indicatorService;

    @Mock
    LevelRepository levelRepository;

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
        List<Indicator> indicators = new ArrayList<>();
        indicators.add(Indicator.builder().name("Fake Name").build());
        indicators.add(Indicator.builder().name("Fake Name 2").build());

        when(indicatorService.extractIndicatorFromFile(file)).thenReturn(indicators);
        service.processFileWithTempIndicators(file);

        verify(indicatorService).extractIndicatorFromFile(file);
        verify(indicatorRepository, times(2)).exists(any());
        verify(indicatorRepository).findAllByNameIn(any());
        verify(indicatorRepository).saveAll(any());
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