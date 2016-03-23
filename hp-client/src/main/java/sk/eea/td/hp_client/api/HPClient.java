package sk.eea.td.hp_client.api;

import sk.eea.td.hp_client.dto.SaveResponseDTO;

import javax.ws.rs.core.Response;

public interface HPClient {

    Response getPin(Long pinId);

    Response getPins(String projectSlug);

    Response getProjectDetail(String projectSlug);

    SaveResponseDTO createProject(String title, Long user, String lat, String lng, String range);

    SaveResponseDTO createPin(String caption, Long projectId, String lat, String lng, String range, String date, License license, PinnerType pinnerType, String content);

    SaveResponseDTO createPin(String caption, String description, Long projectId, String rawLocation, String date, License license, PinnerType pinnerType, String content);

    Response deleteProject(Long projectId);

    Response deletePin(Long pinId);

    void deleteAllPins(Long user);

    void deleteAllProjects(Long user);
}
