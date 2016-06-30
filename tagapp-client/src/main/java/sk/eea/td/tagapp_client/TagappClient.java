/**
 * 
 */
package sk.eea.td.tagapp_client;

import javax.ws.rs.core.Response;

/**
 * TaggApp connector.
 * @author Maros Strmensky
 *
 */
public interface TagappClient {

    Response harvestTags(String resumptionToken);

    Response harvestTags(String from, String until, String batchId);

    Response startEnrichment(String batchId);

    Response removeBatch(String batchId);

    Response addTag(TagDTO tagDto) throws Exception;

    Response addCulturalObject(CulturalObjectDTO culturalObject) throws Exception;

}
