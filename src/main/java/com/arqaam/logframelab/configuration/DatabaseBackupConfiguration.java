package com.arqaam.logframelab.configuration;

import com.arqaam.logframelab.util.Logging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
public class DatabaseBackupConfiguration implements Logging {

    private final SimpleDateFormat fileDateFormatter = new SimpleDateFormat("dd-MM-yyyy_hh:mm");

    @Value("${backup.database-name}")
    private String databaseName;

    @Value("${backup.filename.fixed-part}")
    private String databaseBackupFileName;

    @Value("${backup.location}")
    private String databaseBackupLocation;

    @Value("${backup.command}")
    private String command;

    @Value("${spring.datasource.username}")
    private String databaseUser;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${backup.maximum-files-to-keep}")
    private int maxToKeep;

    @Scheduled(cron = "${backup.schedule}")
    public void defaultBackupProcess() throws InterruptedException {
        String backupTime = fileDateFormatter.format(new Date());

        setupBackupLocation();

        String backupFilename = databaseBackupLocation + databaseBackupFileName + "_" + backupTime + ".sql";

        String commandToExecute = String.format(command, databaseUser, databasePassword, databaseName, backupFilename);

        Process runtimeProcess = null;
        try {
            runtimeProcess = Runtime.getRuntime().exec(commandToExecute);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int processComplete = 0;
        try {
            if (runtimeProcess != null) {
                processComplete = runtimeProcess.waitFor();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        if (processComplete == 0) {
            logger().info("Backup completed at {}", new Date());
        } else {
            logger().warn("Backup failure at {}", new Date());
        }
    }

    @Scheduled(cron = "${backup.cleanup}")
    public void cleanupStaleBackups() {
        File location = new File(databaseBackupLocation);

        if (Objects.requireNonNull(location.listFiles()).length > maxToKeep) {
            List<File> sortedByLastModified = Arrays.stream(location.listFiles())
                    .sorted(Comparator.comparing(File::lastModified).reversed())
                    .collect(Collectors.toList());

            sortedByLastModified.stream().skip(maxToKeep).forEach(File::delete);
        }
    }

    private void setupBackupLocation() {
        File backupLocation = new File(databaseBackupLocation);
        if (!backupLocation.exists()) {
            backupLocation.mkdirs();
        }

        if (!databaseBackupLocation.endsWith(File.separator)) {
            databaseBackupLocation = databaseBackupLocation + File.separator;
        }
    }
}
