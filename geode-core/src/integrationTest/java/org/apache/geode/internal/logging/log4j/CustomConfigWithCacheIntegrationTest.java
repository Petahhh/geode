/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.logging.log4j;

import static org.apache.geode.distributed.ConfigurationProperties.LOCATORS;
import static org.apache.geode.distributed.ConfigurationProperties.LOG_LEVEL;
import static org.apache.geode.distributed.ConfigurationProperties.MCAST_PORT;
import static org.apache.geode.test.util.ResourceUtils.createFileFromResource;
import static org.apache.geode.test.util.ResourceUtils.getResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.test.junit.categories.LoggingTest;

/**
 * Integration tests with custom log4j2 configuration.
 */
@Category(LoggingTest.class)
public class CustomConfigWithCacheIntegrationTest {

  private static final String CONFIG_LAYOUT_PREFIX = "CUSTOM";
  private static final String CUSTOM_REGEX_STRING =
      "CUSTOM: level=[A-Z]+ time=\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} [^ ]{3} message=.*[\\n]+throwable=.*$";
  private static final Pattern CUSTOM_REGEX = Pattern.compile(CUSTOM_REGEX_STRING, Pattern.DOTALL);

  private static String configFilePath;

  private LogWriterLogger logWriterLogger;
  private String logMessage;
  private ListAppender listAppender;

  private Cache cache;

  @ClassRule
  public static SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  @ClassRule
  public static SystemErrRule systemErrRule = new SystemErrRule().enableLog();

  @ClassRule
  public static TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public LoggerContextRule loggerContextRule = new LoggerContextRule(configFilePath);

  @BeforeClass
  public static void setUpLogConfigFile() throws Exception {
    String configFileName =
        CustomConfigWithCacheIntegrationTest.class.getSimpleName() + "_log4j2.xml";
    URL resource = getResource(configFileName);
    configFilePath = createFileFromResource(resource, temporaryFolder.getRoot(), configFileName)
        .getAbsolutePath();
  }

  @Before
  public void setUp() throws Exception {
    Properties config = new Properties();
    config.setProperty(LOCATORS, "");
    config.setProperty(MCAST_PORT, "0");
    config.setProperty(LOG_LEVEL, "info");

    cache = new CacheFactory(config).create();

    logWriterLogger = (LogWriterLogger) cache.getLogger();
    logMessage = "this is a log statement";

    assertThat(LogService.isUsingGemFireDefaultConfig()).as(LogService.getConfigurationInfo())
        .isFalse();

    listAppender = loggerContextRule.getListAppender("CUSTOM");

    systemOutRule.clearLog();
    systemErrRule.clearLog();
  }

  @After
  public void tearDown() throws Exception {
    if (cache != null) {
      cache.close();
    }
  }

  @Test
  public void cacheLogWriterMessageShouldMatchCustomConfig() {
    logWriterLogger.info(logMessage);

    LogEvent logEvent = findLogEventContaining(logMessage);

    assertThat(logEvent.getLoggerName()).isEqualTo(logWriterLogger.getName());
    assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(logEvent.getMessage().getFormattedMessage()).isEqualTo(logMessage);

    assertThat(systemOutRule.getLog()).contains(Level.INFO.name());
    assertThat(systemOutRule.getLog()).contains(logMessage);
    assertThat(systemOutRule.getLog()).contains(CONFIG_LAYOUT_PREFIX);
    assertThat(CUSTOM_REGEX.matcher(systemOutRule.getLog()).matches()).isTrue();
  }

  private LogEvent findLogEventContaining(final String logMessage) {
    List<LogEvent> logEvents = listAppender.getEvents();
    for (LogEvent logEvent : logEvents) {
      if (logEvent.getMessage().getFormattedMessage().contains(logMessage)) {
        return logEvent;
      }
    }
    throw new AssertionError(
        "Failed to find LogEvent containing " + logMessage + " in " + logEvents);
  }
}
