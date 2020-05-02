package com.arqaam.logframelab.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class IndicatorRepositoryTests {

	private final Logger logger = Logger.getLogger(IndicatorRepositoryTests.class.getName());

	@Autowired
	private IndicatorRepository indicatorRepository;

	@Test
	public void toSqlConditionsTest() {
		Map<String, Collection<Object>> filterOptions = new HashMap<>();

		List<Object> themes = new ArrayList<>();
		themes.add("theme1");
		themes.add("theme2");
		filterOptions.put("themes", themes);

		List<Object> levels = new ArrayList<>();
		levels.add(1);
		levels.add(2);
		filterOptions.put("levels", levels);
		String result = indicatorRepository.toSqlConditions(filterOptions);

		logger.info(result);
	}
}
