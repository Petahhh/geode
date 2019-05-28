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

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import org.apache.geode.cache.configuration.CacheConfig;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.management.configuration.GatewayReceiverConfig;
import org.apache.geode.management.configuration.RuntimeCacheElement;

public class GatewayReceiverConfigManager implements ConfigurationManager<GatewayReceiverConfig> {
  final private InternalCache cache;

  public GatewayReceiverConfigManager(InternalCache cache) {
    this.cache = cache;
  }

  @Override
  public void add(GatewayReceiverConfig config, CacheConfig existing) {
    throw new NotImplementedException("Not implemented");
  }

  @Override
  public void update(GatewayReceiverConfig config, CacheConfig existing) {
    throw new NotImplementedException("Not implemented");
  }

  @Override
  public void delete(GatewayReceiverConfig config, CacheConfig existing) {
    throw new NotImplementedException("Not implemented");
  }

  @Override
  public List<? extends RuntimeCacheElement> list(GatewayReceiverConfig filterConfig,
                                                      CacheConfig existing) {

    //TODO / Open Question: What object does Geode use to represent information about GWR?
    // This will impact the type of return object for ths method
    return null;
  }
}
