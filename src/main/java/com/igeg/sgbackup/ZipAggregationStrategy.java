package com.igeg.sgbackup;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        try {
            ByteArrayOutputStream baos;
            ZipOutputStream zos;

            if (oldExchange == null) {
                // First file in aggregation
                baos = new ByteArrayOutputStream();
                zos = new ZipOutputStream(baos);
            } else {
                // Subsequent files in aggregation
                baos = (ByteArrayOutputStream) oldExchange.getIn().getBody();
                zos = new ZipOutputStream(baos);
            }

            // Add the new file to the ZIP
            String filename = newExchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
            ZipEntry entry = new ZipEntry(filename);
            zos.putNextEntry(entry);
            zos.write(newExchange.getIn().getBody(byte[].class));
            zos.closeEntry();

            // Prepare for next aggregation, if any
            if (newExchange.getPattern().isOutCapable()) {
                newExchange.getMessage().setBody(baos);
            } else {
                newExchange.getIn().setBody(baos);
            }

            // Close the ZIP output stream for the last file
            String completedBy = newExchange.getProperty(Exchange.AGGREGATED_COMPLETED_BY, String.class);
            if (completedBy != null && completedBy.equals("size")) {
                zos.close();
            }

            return newExchange;

        } catch (IOException e) {
            throw new RuntimeException("Error while aggregating ZIP", e);
        }
    }
}
