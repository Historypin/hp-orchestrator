package sk.eea.td.flow.activities;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.unbescape.csv.CsvEscape;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.eea.td.console.model.AbstractJobRun;
import sk.eea.td.console.model.BlobReadOnlyParam;
import sk.eea.td.console.model.ParamKey;
import sk.eea.td.console.model.dto.ReviewDTO;
import sk.eea.td.flow.FlowException;
import sk.eea.td.util.PathUtils;

public class PrepareCSVActivity implements Activity {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareCSVActivity.class);

    @Value("${storage.directory}")
    private String outputDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ActivityAction execute(AbstractJobRun context) throws FlowException {
        try {
            Path csvParentDir = PathUtils.getJobRunPath(Paths.get(outputDirectory), String.valueOf(context.getId()));
            Files.createDirectories(csvParentDir);
            Path csvFile = PathUtils.createUniqueFilename(csvParentDir, "csv");

            try (Writer out = new StringWriter()) {
                
                Path approvedDirPath = PathUtils.getApprovedStorePath(Paths.get(outputDirectory), context);
                for (File approvedFile : approvedDirPath.toFile().listFiles()) {
                    ReviewDTO reviewDTO = objectMapper.readValue(approvedFile, ReviewDTO.class);
                    
                    out.write(CsvEscape.escapeCsv(reviewDTO.getExternalId()));
                    out.write(',');
                    out.write(CsvEscape.escapeCsv(StringUtils.join(reviewDTO.getApprovedTags(), ',')));
                    out.write("\n");
                }
                
                context.addReadOnlyParam(new BlobReadOnlyParam(ParamKey.EMAIL_ATTACHMENT, csvFile.getFileName().toString(), out.toString().getBytes("UTF-8")));
            }            
        } catch (Exception e) {
            LOG.error("", e);
            throw new FlowException("Exception raised during prepare CSV action", e);
        }

        return ActivityAction.CONTINUE;
    }

    @Override
    public String getName() {
        return PrepareCSVActivity.class.getSimpleName();
    }
}
