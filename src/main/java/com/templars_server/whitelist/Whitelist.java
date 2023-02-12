package com.templars_server.whitelist;

import org.apache.log4j.helpers.FileWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Whitelist {

    private static final Logger LOG = LoggerFactory.getLogger(Whitelist.class);
    private static final int IP_OCTET_COUNT = 4;
    private static final String IP_OCTET_DELIMITER = "\\.";
    private static final String IP_WILDCARD = "*";

    private FileWatchdog fileWatchdog;
    private List<String> content;

    public void load(String filename) throws IOException {
        if (fileWatchdog != null) {
            fileWatchdog.interrupt();
        }

        File file = new File(filename);
        if (file.isDirectory()) {
            throw new IOException("Couldn't open whitelist " + filename + ", file is a directory");
        }

        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Couldn't create new whitelist " + filename);
        }

        fileWatchdog = new FileWatchdog(filename) {
            @Override
            protected void doOnChange() {
                try {
                    onChange(Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOG.error("Couldn't read file " + filename + "during onChange", e);
                }
            }
        };
        fileWatchdog.start();
    }

    public boolean contains(String value) {
        return content.stream().map((ip) -> {
                    String[] ipSplit = ip.split(IP_OCTET_DELIMITER);
                    String[] valueSplit = value.split(IP_OCTET_DELIMITER);

                    if (ipSplit.length > IP_OCTET_COUNT || valueSplit.length < IP_OCTET_COUNT) {
                        return false;
                    }

                    for (int i=0; i<ipSplit.length; i++) {
                        if (ipSplit[i].equals(IP_WILDCARD)) {
                            continue;
                        }

                        if (!ipSplit[i].equals(valueSplit[i])) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList())
                .contains(true);
    }

    void setContent(List<String> content) {
        this.content = content;
    }

    private void onChange(List<String> content) {
        this.content = content;
    }
}
