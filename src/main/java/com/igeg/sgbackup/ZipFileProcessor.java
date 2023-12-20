package com.igeg.sgbackup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;

public class ZipFileProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        ArrayList<GenericFile<?>> fileList = exchange.getIn().getBody(ArrayList.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (GenericFile<?> genericFile : fileList) {
            ZipEntry zipEntry = new ZipEntry(genericFile.getFileName());
            zipOutputStream.putNextEntry(zipEntry);

            File file = (File) genericFile.getFile();
            try (InputStream inputStream = new FileInputStream(file)) {
                byte[] bytes = inputStream.readAllBytes();
                zipOutputStream.write(bytes);
            }

            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
        exchange.getIn().setBody(byteArrayOutputStream.toByteArray());
    }
}
