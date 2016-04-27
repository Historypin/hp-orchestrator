package sk.eea.td.hp_client.api;

import sk.eea.td.hp_client.dto.PlacesResponseDTO;
import sk.eea.td.hp_client.dto.SaveResponseDTO;

import javax.ws.rs.core.Response;

public interface HPClient {

    Response getPin(Long pinId);

    Response getPins(String projectSlug);

    Response getProjectDetail(String projectSlug);

    SaveResponseDTO createProject(Long userId, Project project);

    SaveResponseDTO createPin(Long projectId, Pin pin);

    Response deleteProject(Long projectId);

    Response deletePin(Long pinId);

    PlacesResponseDTO getPlaces(String countrySlug);

    void deleteAllPins(Long user);

    void deleteAllProjects(Long user);
}
