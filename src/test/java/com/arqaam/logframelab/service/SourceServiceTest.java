package com.arqaam.logframelab.service;

import com.arqaam.logframelab.exception.SourceNotFoundException;
import com.arqaam.logframelab.model.persistence.Source;
import com.arqaam.logframelab.repository.SourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceServiceTest {

    @Mock
    private SourceRepository sourceRepository;
    @InjectMocks
    private SourceService sourceService;
    @Test
    void getSources() {
        List<Source> expected = Collections.singletonList(new Source(1L, "Fake Source"));
        when(sourceRepository.findAll()).thenReturn(expected);
        List<Source> result = sourceService.getSources();
        assertEquals(expected, result);
        verify(sourceRepository).findAll();
    }

    @Test
    void getSourceById() {
        Long id = 1L;
        Source expectedSource = new Source(1L, "Fake Source");
        when(sourceRepository.findById(id)).thenReturn(Optional.of(expectedSource));
        Source result = sourceService.getSourceById(id);
        assertEquals(expectedSource, result);
        verify(sourceRepository).findById(id);
    }

    @Test
    void getSourceById_idNotFound() {
        assertThrows(SourceNotFoundException.class, ()-> {
            Long id = 1L;
            when(sourceRepository.findById(id)).thenReturn(Optional.empty());
            sourceService.getSourceById(id);
            verify(sourceRepository).findById(id);
        });
    }

    @Test
    void createSource() {
        String name = "New Name";
        Source expectedSource = new Source(null, name);
        when(sourceRepository.save(expectedSource)).thenReturn(expectedSource);
        Source result = sourceService.createSource(name);
        assertEquals(expectedSource, result);
        verify(sourceRepository).save(expectedSource);
    }

    @Test
    void createSource_invalidName() {
        assertThrows(IllegalArgumentException.class, ()-> {
            sourceService.createSource("");
        });
        verify(sourceRepository, times(0)).save(any());
    }

    @Test
    void updateSource() {
        Long id = 1L;
        String newName = "New Name";
        Source returnedSource = new Source(1L, "Fake Source");
        Source expectedSource = new Source(1L, newName);
        when(sourceRepository.findById(id)).thenReturn(Optional.of(returnedSource));
        when(sourceRepository.save(expectedSource)).thenReturn(expectedSource);
        Source result = sourceService.updateSource(id, newName);
        assertEquals(expectedSource, result);
        verify(sourceRepository).findById(id);
        verify(sourceRepository).save(expectedSource);
    }

    @Test
    void updateSource_invalidName() {
        assertThrows(IllegalArgumentException.class, ()-> {
            Long id = 1L;
            Source expectedSource = new Source(1L, "Fake Source");
            when(sourceRepository.findById(id)).thenReturn(Optional.of(expectedSource));
            sourceService.updateSource(1L, null);
        });
        verify(sourceRepository, times(0)).save(any());
    }

    @Test
    void deleteSourceById() {
        Long id = 1L;
        Source expectedSource = new Source(1L, "Fake Source");
        when(sourceRepository.findById(id)).thenReturn(Optional.of(expectedSource));
        Source result = sourceService.deleteSourceById(id);
        assertEquals(expectedSource, result);
        verify(sourceRepository).findById(id);
    }
}