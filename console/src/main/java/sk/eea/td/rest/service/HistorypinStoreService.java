package sk.eea.td.rest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.eea.td.hp_client.api.HPClient;
import sk.eea.td.hp_client.api.License;
import sk.eea.td.hp_client.api.PinnerType;
import sk.eea.td.hp_client.dto.SaveResponseDTO;
import sk.eea.td.rest.model.HistorypinTransformDTO;

import java.io.IOException;
import java.nio.file.Path;

import static sk.eea.td.util.DateUtils.parseHistoryPinDate;

@Component
public class HistorypinStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(HistorypinStoreService.class);

    @Value("${historypin.user}")
    private String user;

    @Autowired
    private HPClient hpClient;

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
    public boolean store(Long projectId, Path file) throws IOException {
        final HistorypinTransformDTO transformation = objectMapper.readValue(file.toFile(), HistorypinTransformDTO.class);
        int failedPins = 0;
        for (HistorypinTransformDTO.Record record : transformation.getRecords()) {
            HistorypinTransformDTO.Pin pin = record.getPin();
            SaveResponseDTO response = hpClient.createPin(
                    pin.getCaption(),
                    pin.getLandingPage(),
                    projectId,
                    pin.getCountry(),
                    parseHistoryPinDate(pin.getDate()),
                    License.getByKey(pin.getLicense()),
                    PinnerType.PHOTO,
                    pin.getPreview()
            );

            HistorypinTransformDTO.Remote remote = record.getRemote();
            if (response.getId() != null) {
                LOG.debug("Record with remote ID: '{}' was created. Target ID is: '{}'", remote.getId(), response.getId());
            } else {
                failedPins++;
                LOG.error("Failed to create record with remote ID: '{}'. Reason: target_id='{}' errorMessages='{}'", remote.getId(), response.getId(), response.getErrors());
            }
        }
        LOG.debug("Successfully extracted and uploaded {} pins from file '{}'.", transformation.getRecords().size(), file);
        return failedPins <= 0;
    }

}
