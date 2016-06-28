package sk.eea.td.rest.service;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.Location;
import sk.eea.td.hp_client.api.Pin;
import sk.eea.td.hp_client.api.Project;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.rest.model.HistorypinTransformDTO;

public class HistorypinStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(HistorypinStoreService.class);

    private HPClient hpClient;

    private Long hpUser;

    private ObjectMapper objectMapper;

    private HistorypinStoreService(HPClient hpClient, Long hpUser) {
        this.hpClient = hpClient;
        this.hpUser = hpUser;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Stores contents of file into HP.
     *
     * @param projectId ID of project to save pins to.
     * @param file      File to parse.
     * @return True if there we no failed pin uploads, false if otherwise.
     * @throws IOException
     */
    public boolean storeToProject(Long projectId, Path file) throws IOException {
        final HistorypinTransformDTO transformation = objectMapper.readValue(file.toFile(), HistorypinTransformDTO.class);
        int failedPins = 0;
        for (Pin pin : transformation.getPins()) {
            try {
                final SaveResponseDTO response = hpClient.createPin(projectId, pin);
                if (response.getId() != null) {
                    LOG.debug("Record with remote ID: '{}' was created. Target ID is: '{}'", pin.getRemoteId(), response.getId());
                } else {
                    failedPins++;
                    LOG.error("Failed to create record with remote ID: '{}'. Reason: target_id='{}' errorMessages='{}'", pin.getRemoteId(), response.getId(), response.getErrors());
                }
            } catch (Exception e) {
                failedPins++;
                LOG.error("Failed to create record with remote ID: '{}'. Exception'", pin.getRemoteId(), e);
            }
        }
        LOG.debug("Successfully extracted and uploaded {} pins from file '{}'.", transformation.getPins().size() - failedPins, file);
        return failedPins <= 0;
    }

    public Long createProject(String projectName, Location projectLocation) {
        final SaveResponseDTO response = hpClient.createProject(hpUser, new Project(projectName, projectLocation));
        // verify that project is created
        if (!response.getErrors().isEmpty()) {
            throw new IllegalStateException("Could not create collection with name: " + projectName + " in Historypin API. Reason: " + response.getErrors().toString());
        } else if (response.getId() == null) {
            throw new IllegalStateException("Could not create collection with name: " + projectName + " in Historypin API. Reason: projectId is null");
        } else {
            LOG.debug("Created new project in Historypin with ID: {}.", response.getId());
            return response.getId();
        }
    }

    public static HistorypinStoreService getInstance(HPClient hpClient, Long hpUser) {
        return new HistorypinStoreService(hpClient, hpUser);
    }

}
