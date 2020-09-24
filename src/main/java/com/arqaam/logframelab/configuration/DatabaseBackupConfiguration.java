package com.arqaam.logframelab.configuration;

import com.arqaam.logframelab.util.Logging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "backup", name="enabled", havingValue="true", matchIfMissing = true)
public class DatabaseBackupConfiguration implements Logging {

	private static final SimpleDateFormat fileDateFormatter = new SimpleDateFormat("dd-MM-yyyy_hh:mm");

	private static final AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().asyncCredentialUpdateEnabled(true).build();

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

	@Value("${backup.aws.s3-bucket.name}")
	private String awsS3BucketName;

	@Scheduled(cron = "${backup.schedule}")
	public void defaultBackupProcess() throws InterruptedException {
		String backupTime = fileDateFormatter.format(new Date());

        setupBackupLocation();

		String filename = databaseBackupFileName + "_" + backupTime + ".sql";

		String backupFilename = System.getProperty("user.home") + File.separator + databaseBackupLocation + filename;

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

		PutObjectResponse putObjectResponse;

		if (processComplete == 0) {
			logger().info("Backup to file system completed at {}. Proceeding with S3 backup...", new Date());

			S3Client amazonS3Client = S3Client.builder().region(Region.EU_WEST_1).credentialsProvider(credentialsProvider).build();

			Path uploadFrom = FileSystems.getDefault().getPath(backupFilename);

			PutObjectRequest putObjectRequest = PutObjectRequest.builder().key(filename).bucket(awsS3BucketName).build();

			putObjectResponse = amazonS3Client.putObject(putObjectRequest, uploadFrom);

			logger().info("Backup to S3 completed at {}, with info {}", new Date(), putObjectResponse.toString());
		} else {
			logger().error("Backup failure at {} with error {}", new Date(), processComplete);
		}
	}

    @Scheduled(cron = "${backup.cleanup}")
    public void cleanupStaleBackups() {
        File location = new File( System.getProperty("user.home") + File.separator + databaseBackupLocation);
        logger().info("Backup clean up starting at {}", new Date());

        if (Objects.requireNonNull(location.listFiles()).length > maxToKeep) {
            List<File> sortedByLastModified = Arrays.stream(location.listFiles())
                    .sorted(Comparator.comparing(File::lastModified).reversed())
                    .collect(Collectors.toList());

            sortedByLastModified.stream().skip(maxToKeep).forEach(File::delete);
        }
    }

    private void setupBackupLocation() {
        File backupLocation = new File( System.getProperty("user.home") + File.separator + databaseBackupLocation);
        if (!backupLocation.exists()) {
            backupLocation.mkdirs();
        }

        if (!databaseBackupLocation.endsWith(File.separator)) {
            databaseBackupLocation = databaseBackupLocation + File.separator;
        }
    }
}
