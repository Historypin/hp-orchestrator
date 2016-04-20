package sk.eea.td.rest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.Pin;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.rest.model.HistorypinTransformDTO;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class HistorypinStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(HistorypinStoreService.class);

    @Value("${historypin.user}")
    private String user;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Stores contents of file into HP.
     *
     * @param projectId ID of project to save pins to.
     * @param file      File to parse.
     * @return True if there we no failed pin uploads, false if otherwise.
     * @throws IOException
     */
    public boolean store(Long projectId, Path file, HPClient hpClient) throws IOException {
        final HistorypinTransformDTO transformation = objectMapper.readValue(file.toFile(), HistorypinTransformDTO.class);
        int failedPins = 0;
        for (HistorypinTransformDTO.Record record : transformation.getRecords()) {
            final Pin pin = record.getPin();
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
        LOG.debug("Successfully extracted and uploaded {} pins from file '{}'.", transformation.getRecords().size() - failedPins, file);
        return failedPins <= 0;
    }

}
