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

package org.apache.geode.management.internal.configuration.mutators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.configuration.CacheConfig;
import org.apache.geode.cache.wan.GatewayReceiver;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.management.DistributedSystemMXBean;
import org.apache.geode.management.GatewayReceiverMXBean;
import org.apache.geode.management.ManagementService;
import org.apache.geode.management.configuration.GatewayReceiverConfig;
import org.apache.geode.management.internal.SystemManagementService;

public class GatewayReceiverConfigManagerTest {
  private GatewayReceiverConfigManager gatewayReceiverConfigManager;
  private GatewayReceiverConfig mockGatewayReceiverConfig;
  private CacheConfig mockCacheConfig;
  private InternalCache cache;

  @Before
  public void setUp() throws Exception {
    cache = mock(InternalCache.class);
    gatewayReceiverConfigManager = new GatewayReceiverConfigManager(cache);
    mockGatewayReceiverConfig = mock(GatewayReceiverConfig.class);
    mockCacheConfig = mock(CacheConfig.class);
//    distributionManager = mock(DistributionManager.class);
//    membershipManager = mock(MembershipManager.class);
//    coordinator = mock(DistributedMember.class);
//    cacheServerInfo = mock(CacheServerInfo.class);
//    filter = new MemberConfig();
//    internalDistributedMember = mock(InternalDistributedMember.class);
//    internalDistributedMemberMatch = mock(InternalDistributedMember.class);
//
//    when(internalDistributedMember.getName()).thenReturn("no-match");
//    when(cache.getDistributionManager()).thenReturn(distributionManager);
  }

  @Test
  public void cacheHasGatewayReceiver() {
    HashSet<GatewayReceiver> gatewayReceiver = new HashSet<>();
    gatewayReceiver.add(mock(GatewayReceiver.class));
    when(cache.getGatewayReceivers()).thenReturn(gatewayReceiver);
    List<?>
        gateways =
        gatewayReceiverConfigManager.list(new GatewayReceiverConfig(), mockCacheConfig);
    assertThat(gateways).hasSize(1);
  }

  @Test
  public void cacheHasNoGatewayReceiver() {
    List<?>
        gateways =
        gatewayReceiverConfigManager.list(new GatewayReceiverConfig(), mockCacheConfig);
    assertThat(gateways).isEmpty();

  }

  @Test
  public void testGetGatewayReceiverMXBeans() {
    SystemManagementService managementService = mock(SystemManagementService.class);
    when(gatewayReceiverConfigManager.getSystemManagementService()).thenReturn(managementService);
    DistributedSystemMXBean distributedSystemMXBean = mock(DistributedSystemMXBean.class);
    when(managementService.getDistributedSystemMXBean()).thenReturn(distributedSystemMXBean);
    GatewayReceiverMXBean gatewayReceiverMXBean = mock(GatewayReceiverMXBean.class);
    when(gatewayReceiverConfigManager.getGatewayReceiverMXBeans()).thenReturn(Arrays.asList(gatewayReceiverMXBean));
    assertThat(gatewayReceiverConfigManager.getGatewayReceiverMXBeans().size()).isEqualTo(1);
  }

  @Test(expected = NotImplementedException.class)
  public void addNotImplemented() {
    gatewayReceiverConfigManager.add(mockGatewayReceiverConfig, mockCacheConfig);
  }

  @Test(expected = NotImplementedException.class)
  public void updateNotImplemented() {
    gatewayReceiverConfigManager.update(mockGatewayReceiverConfig, mockCacheConfig);
  }

  @Test(expected = NotImplementedException.class)
  public void deleteNotImplemented() {
    gatewayReceiverConfigManager.delete(mockGatewayReceiverConfig, mockCacheConfig);
  }




}