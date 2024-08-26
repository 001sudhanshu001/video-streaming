package com.learn.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileNameUtil {

    public static String addDateAndUUIDToFileName(String originalFileName) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueID = UUID.randomUUID().toString();

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return originalFileName + "_" + currentDate + "_" + uniqueID;
        }

        String baseName = originalFileName.substring(0, dotIndex);
        String extension = originalFileName.substring(dotIndex);

        return baseName + "_" + currentDate + "_" + uniqueID + extension;
    }
}