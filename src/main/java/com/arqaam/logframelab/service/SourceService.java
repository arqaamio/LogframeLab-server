package com.arqaam.logframelab.service;

import com.arqaam.logframelab.model.persistence.Source;
import com.arqaam.logframelab.exception.SourceNotFoundException;
import com.arqaam.logframelab.repository.SourceRepository;
import com.arqaam.logframelab.util.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SourceService implements Logging {
    @Autowired
    private SourceRepository sourceRepository;

    /**
     * Retrieves all sources
     * @return List of sources
     */
    public List<Source> getSources(){
        logger().info("Retrieving all sources");
        return sourceRepository.findAll();
    }

    /**
     * Retrieves the source with the id
     * @param id Id of the source
     * @return Source found with the id
     */
    public Source getSourceById(Long id){
        logger().info("Searching for source with id: {}", id);
        Optional<Source> source = sourceRepository.findById(id);
        if(source.isEmpty()) {
            logger().error("Failed to find source with id: {}", id);
            throw new SourceNotFoundException();
        }
        return source.get();
    }

    /**
     * Creates a new source and saves it into the database
     * @param name Name of the new source
     * @return New source
     */
    public Source createSource(String name){
        logger().info("Creating source with name: {}", name);
        validateSource(name);
        Source source = new Source();
        source.setName(name.trim());
        return sourceRepository.save(source);
    }

    /**
     * Updates the source with the id
     * @param id Id of the source to be update
     * @param name Name of the source to be updated to
     * @return Source updated
     */
    public Source updateSource(Long id, String name){
        logger().info("Updating source with id: {}", id);
        Source source = getSourceById(id);
        validateSource(name);
        source.setName(name.trim());
        return sourceRepository.save(source);
    }

    /**
     * Deletes source with id and returns it
     * @param id Id of the source to be deleted
     * @return Deleted source
     */
    public Source deleteSourceById(Long id){
        logger().info("Deleting source with id: {}", id);
        Source source = getSourceById(id);
        sourceRepository.delete(source);
        return source;
    }

    /**
     * Validates source
     * @param name Name of the source to be validated
     */
    private void validateSource(String name) {
        if(name == null || name.trim().isEmpty()) {
            logger().error("Invalid value for name. Name: {}", name);
            throw new IllegalArgumentException();
        }
    }
}
