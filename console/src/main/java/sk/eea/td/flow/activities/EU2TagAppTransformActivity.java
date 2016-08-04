/**
 * 
 */
package sk.eea.td.flow.activities;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.Connector;
import sk.eea.td.tagapp_client.CulturalObjectDTO;
import sk.eea.td.util.PathUtils;

/**
 * @author Maros Strmensky
 *
 */

public class EU2TagAppTransformActivity extends AbstractTransformActivity {

    private static final Logger LOG = LoggerFactory.getLogger(EU2TagAppTransformActivity.class);

    @Autowired
    ObjectMapper objectMapper;
    
    @Override
    public String getName() {
        return EU2TagAppTransformActivity.class.getSimpleName();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Path transform(Connector source, Path file, Path outputDir, AbstractJobRun context) throws IOException {
        if(Connector.EUROPEANA != source){
            LOG.info("Not procesing file {}. Input should be from {}",file.getFileName(), source);
            return outputDir;
        }
        HashMap<ParamKey, String> params = new HashMap<ParamKey, String>();
        context.getReadOnlyParams().forEach(param -> params.put(param.getKey(), param.getValue()));
        Path targetFile = PathUtils.createUniqueFilename(outputDir, context.getJob().getTarget().getFormatCode());
//        transformListItem(file, targetFile, params);
        transformRecordItem(file, targetFile, params);
        return outputDir;
    }

    @SuppressWarnings("unused")
    private void transformListItem(Path sourceFile, Path targetFile, HashMap<ParamKey, String> params)
            throws IOException, JsonProcessingException, JsonGenerationException, JsonMappingException {
        JsonNode rootNode = objectMapper.readTree(sourceFile.toFile());
        JsonNode items = rootNode.get("items");
        if(!items.isArray())
            throw new IOException(MessageFormat.format("Invalid input structure of file {0}",sourceFile));
        for(Iterator<JsonNode> i = items.iterator(); i.hasNext();){
            JsonNode element = i.next();
            CulturalObjectDTO dto = new CulturalObjectDTO();
            String lang = findValue(element, "dcTypeLangAware.def");
            String objectType = findValue(element, "type");
            if(!objectType.equalsIgnoreCase("IMAGE"))
                continue;
            dto.setAuthor(findValue(element,"edmAgentLabel[0].def"));
            HashMap<String, String> descriptions = new HashMap<String, String>();
            descriptions.put(lang, findValue(element,"dcDescriptionLangAware.def"));
            dto.setDescription(descriptions);
            dto.setExternalId(findValue(element,"id"));
            dto.setExternalUrl(findValue(element,"edmIsShownAt[0]"));
            dto.setExternalSource(findValue(element,"edmIsShownBy[0]"));
            targetFile.toFile().createNewFile();
            objectMapper.writeValue(targetFile.toFile(), dto);
        }
    }

    private void transformRecordItem(Path sourceFile, Path targetFile, HashMap<ParamKey, String> params)
            throws IOException, JsonProcessingException, JsonGenerationException, JsonMappingException {
        JsonNode element = objectMapper.readTree(sourceFile.toFile());
        CulturalObjectDTO dto = new CulturalObjectDTO();
        String lang = findValue(element, "object.europeanaAggregation.edmLanguage.def");
        String objectType = findValue(element, "object.type");
        if(objectType == null || !objectType.equalsIgnoreCase("IMAGE"))
            return;
        dto.setAuthor(findValue(element,"object.proxies[0].dcCreator.def[0]"));
        HashMap<String, String> descriptions = new HashMap<String, String>();
        descriptions.put(lang, findValue(element,"object.title[0]"));
        dto.setDescription(descriptions);
        dto.setExternalId(findValue(element,"object.about"));
        dto.setExternalUrl(findValue(element,"object.aggregations[0].edmIsShownAt"));
        dto.setExternalSource(findValue(element,"object.aggregations[0].edmIsShownBy"));
        targetFile.toFile().createNewFile();
        objectMapper.writeValue(targetFile.toFile(), dto);
    }

    protected JsonNode findNode(JsonNode object, String path) throws IOException{
        if(path.equals("")){
            return object;
        }
        String[] pathElements = path.split("\\.",2);
        if(pathElements[0].matches(".*\\[\\d*\\]")){
            String element = pathElements[0].replaceAll("\\[\\d+\\]$", "");
            if(object.get(element) != null){
                int position = Integer.valueOf(pathElements[0].replaceAll(".*\\[(\\d+)\\]$", "$1"));
                if(object.get(element).isArray()){
                    return findNode(object.get(element).get(position), pathElements.length < 2 ? "" : pathElements[1]);
                }else{
                    throw new IOException(MessageFormat.format("Node {0} is not array", pathElements[0]));
                }
            }else{
                return null;
            }
        }else{
            if(object.get(pathElements[0]) != null){
                return findNode(object.get(pathElements[0]), pathElements.length < 2 ? "" : pathElements[1]);
            }else{
                return null;
            }
        }            
    }
    
    protected String findValue(JsonNode object, String path) throws IOException {
        JsonNode node = findNode(object, path);
        if(node == null)
            return null;
        if(node.isArray()){
            StringBuilder result = new StringBuilder();
            node.elements().forEachRemaining(child -> result.append(child.textValue()).append(" "));
            return result.toString().trim();
        }else{
            return node.textValue();
        }
    }
}
