package com.igeg.sgbackup;

import java.util.ArrayList;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFile;

public class ArrayAggregationStrategy implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        try {
            ArrayList<GenericFile<?>> files;

            // First time we process an exchange we need to create the array list to hold
            // the files
            if (oldExchange == null) {
                files = new ArrayList<>();
            } else {
                // After the first time we can get the array list from the previous exchange
                files = oldExchange.getIn().getBody(ArrayList.class);
            }

            // Add the file from the current exchange to the array list
            files.add(newExchange.getIn().getBody(GenericFile.class));

            // Put the array list back on the exchange
            newExchange.getIn().setBody(files);

            return newExchange;
        } catch (Exception e) {
            throw new RuntimeException("Error while aggregating the Files", e);
        }
    }
}
