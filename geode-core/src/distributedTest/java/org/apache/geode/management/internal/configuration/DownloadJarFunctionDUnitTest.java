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

package org.apache.geode.management.internal.configuration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.apache.geode.test.compiler.ClassBuilder;
import org.apache.geode.test.dunit.IgnoredException;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.rules.ClusterStartupRule;
import org.apache.geode.test.dunit.rules.MemberVM;
import org.apache.geode.test.junit.rules.GfshCommandRule;
import org.apache.geode.test.junit.rules.LocatorStarterRule;
import org.apache.geode.test.junit.rules.ServerStarterRule;


public class DownloadJarFunctionDUnitTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ClusterStartupRule clusterStartupRule = new ClusterStartupRule();

  @Rule
  public GfshCommandRule gfsh = new GfshCommandRule();

  private static MemberVM locatorWithJMX, locatorWithoutJMX;

  @Before
  public void before() throws Exception {
    locatorWithoutJMX =
        clusterStartupRule.startLocatorVM(0, p -> p.withProperty("jmx-manager", "false"));
    locatorWithJMX = clusterStartupRule.startLocatorVM(1, locatorWithoutJMX.getPort());
  }

  @Test
  public void downloadJarOnServerThrowsErrorWhenJMXIsOff() throws Exception {
    IgnoredException.addIgnoredException("IllegalStateException");

    gfsh.connectAndVerify(locatorWithJMX.getPort(), GfshCommandRule.PortType.locator);

    String clusterJar = createJarFileWithClass("Cluster", "cluster.jar", temporaryFolder.getRoot());
    gfsh.executeAndAssertThat("deploy --jar=" + clusterJar).statusIsSuccess();

    locatorWithJMX.stop();
    int locatorPort = locatorWithoutJMX.getPort();

    VM server = VM.getVM(2);
    server.invoke(() -> {
      ServerStarterRule serverRule = new ServerStarterRule();
      serverRule.withConnectionToLocator(locatorPort).withAutoStart();
      assertThatThrownBy(() -> serverRule.before()).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("JMX Management agent is not available. ");
    });
  }

  @Test
  public void downloadJarOnLocatorThrowsErrorWhenJMXIsOff() throws Exception {
    IgnoredException.addIgnoredException("IllegalStateException");

    gfsh.connectAndVerify(locatorWithJMX.getPort(), GfshCommandRule.PortType.locator);

    String clusterJar = createJarFileWithClass("Cluster", "cluster.jar", temporaryFolder.getRoot());
    gfsh.executeAndAssertThat("deploy --jar=" + clusterJar).statusIsSuccess();

    locatorWithJMX.stop();
    int locatorPort = locatorWithoutJMX.getPort();

    VM locator = VM.getVM(2);
    locator.invoke(() -> {
      LocatorStarterRule locatorRule = new LocatorStarterRule();
      locatorRule.withConnectionToLocator(locatorPort).withAutoStart();
      assertThatThrownBy(() -> locatorRule.before()).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("JMX Management agent is not available. ");
    });
  }

  private String createJarFileWithClass(String className, String jarName, File dir)
      throws IOException {
    File jarFile = new File(dir, jarName);
    new ClassBuilder().writeJarFromName(className, jarFile);
    return jarFile.getCanonicalPath();
  }
}
