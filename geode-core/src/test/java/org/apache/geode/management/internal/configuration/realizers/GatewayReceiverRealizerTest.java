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
 *
 */
package org.apache.geode.management.internal.configuration.realizers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.configuration.GatewayReceiverConfig;
import org.apache.geode.cache.wan.GatewayReceiver;
import org.apache.geode.cache.wan.GatewayReceiverFactory;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.management.api.RealizationResult;

public class GatewayReceiverRealizerTest {
  private InternalCache cache;
  private GatewayReceiverConfig gatewayReceiverConfig;
  private GatewayReceiverRealizer gatewayReceiverRealizer;

  @Before
  public void setup() {
    cache = mock(InternalCache.class);
    gatewayReceiverConfig = new GatewayReceiverConfig();
    gatewayReceiverRealizer = new GatewayReceiverRealizer();
  }

  @Test
  public void testSuccessWhenCreatingGatewayReceiver() {
    GatewayReceiverFactory gatewayReceiverFactory = mock(GatewayReceiverFactory.class);
    when(cache.createGatewayReceiverFactory()).thenReturn(gatewayReceiverFactory);
    GatewayReceiver gatewayReceiver = mock(GatewayReceiver.class);
    when(gatewayReceiverFactory.create()).thenReturn(gatewayReceiver);

    RealizationResult realizationResult =
        gatewayReceiverRealizer.create(gatewayReceiverConfig, cache);
    assertThat(realizationResult.isSuccess()).isTrue();
  }

  @Test
  public void testFailureWhenCreatingGatewayReceiver() {
    GatewayReceiverFactory gatewayReceiverFactory = mock(GatewayReceiverFactory.class);
    when(cache.createGatewayReceiverFactory()).thenReturn(gatewayReceiverFactory);
    when(gatewayReceiverFactory.create()).thenThrow(new RuntimeException());

    RealizationResult realizationResult =
        gatewayReceiverRealizer.create(gatewayReceiverConfig, cache);
    assertThat(realizationResult.isSuccess()).isFalse();
  }

  @Test
  public void testReturnTrueOnExistsCall() {
    GatewayReceiver gatewayReceiver = mock(GatewayReceiver.class);
    Set<GatewayReceiver> gatewayReceivers = Collections.singleton(gatewayReceiver);
    when(cache.getGatewayReceivers()).thenReturn(gatewayReceivers);

    assertThat(gatewayReceiverRealizer.exists(null, cache)).isEqualTo(true);
  }

  @Test
  public void testSuccessOnGet() {
    GatewayReceiverRealizer gatewayReceiverRealizer = new GatewayReceiverRealizer();
    assertThat(gatewayReceiverRealizer.get(null, null)).isNull();
  }

  @Test(expected = NotImplementedException.class)
  public void testUpdate() {
    gatewayReceiverRealizer.update(null, null);
  }

  @Test(expected = NotImplementedException.class)
  public void testDelete() {
    gatewayReceiverRealizer.delete(null, null);
  }
}
