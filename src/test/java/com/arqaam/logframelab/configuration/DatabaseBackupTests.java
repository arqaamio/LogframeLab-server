package com.arqaam.logframelab.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@SpringBootTest
@ActiveProfiles(profiles = {"dev", "test"})
public class DatabaseBackupTests {

	@Autowired
	private DatabaseBackupConfiguration databaseBackupConfiguration;

	@Value("${backup.location}")
	private String databaseBackupLocation;

	@Test
	void backupTest() throws InterruptedException {
		databaseBackupConfiguration.defaultBackupProcess();

		String backupLocation = System.getProperty("user.home") + File.separator + databaseBackupLocation;
		File backupLocationPath = new File(backupLocation);

		assertThat(backupLocationPath.listFiles(), not(emptyArray()));

		Arrays.stream(Objects.requireNonNull(backupLocationPath.listFiles())).forEach(File::delete);
		backupLocationPath.deleteOnExit();
	}
}
