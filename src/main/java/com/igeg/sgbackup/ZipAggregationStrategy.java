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
            ByteArrayOutputStream byteArrayOutputStream;
            ZipOutputStream zipOutputStream;

            if (oldExchange == null) {
                // First file in aggregation
                byteArrayOutputStream = new ByteArrayOutputStream();
                zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            } else {
                // Subsequent files in aggregation
                byteArrayOutputStream = oldExchange.getIn().getBody(ByteArrayOutputStream.class);
                zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            }

            // Add the new file to the ZIP
            String filename = newExchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
            ZipEntry entry = new ZipEntry(filename);

            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write(newExchange.getIn().getBody(byte[].class));
            zipOutputStream.closeEntry();

            // Prepare for next aggregation, if any
            if (newExchange.getPattern().isOutCapable()) {
                newExchange.getMessage().setBody(byteArrayOutputStream);
            } else {
                newExchange.getIn().setBody(byteArrayOutputStream);
            }

            // Close the ZIP output stream for the last file
            String completedBy = newExchange.getProperty(Exchange.AGGREGATED_COMPLETED_BY, String.class);
            if (completedBy != null && completedBy.equals("size")) {
                zipOutputStream.close();
            }

            return newExchange;

        } catch (IOException e) {
            throw new RuntimeException("Error while aggregating ZIP", e);
        }
    }
}
