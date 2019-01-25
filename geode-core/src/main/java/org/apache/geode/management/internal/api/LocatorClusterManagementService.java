/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.management.internal.api;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import org.apache.geode.cache.configuration.CacheConfig;
import org.apache.geode.cache.configuration.CacheElement;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.distributed.ConfigurationPersistenceService;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.internal.DistributionManager;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.management.internal.cli.CliUtil;
import org.apache.geode.management.internal.cli.functions.CliFunctionResult;
import org.apache.geode.management.internal.cli.functions.UpdateCacheFunction;
import org.apache.geode.management.internal.configuration.mutators.ConfigurationMutator;
import org.apache.geode.management.internal.configuration.mutators.ConfigurationMutatorFactory;
import org.apache.geode.management.internal.configuration.validators.ConfigurationValidator;
import org.apache.geode.management.internal.configuration.validators.ConfigurationValidatorFactory;
import org.apache.geode.management.internal.exceptions.EntityExistsException;
import org.apache.geode.management.internal.exceptions.NoMembersException;

public class LocatorClusterManagementService implements ClusterManagementService {
  private static Logger logger = LogService.getLogger();
  private DistributionManager distributionManager;
  private ConfigurationPersistenceService persistenceService;

  public LocatorClusterManagementService(DistributionManager distributionManager,
      ConfigurationPersistenceService persistenceService) {
    this.distributionManager = distributionManager;
    this.persistenceService = persistenceService;
  }

  @Override
  public ClusterManagementResult create(CacheElement config) {
    ClusterManagementResult result = new ClusterManagementResult();
    String group = "cluster";

    // validate config fields and apply defaults if necessary
    ConfigurationValidator configurationValidator =
        (new ConfigurationValidatorFactory()).generate(config);
    CacheElement validatedConfig = configurationValidator.setDefaultsAndValidate(config);

    final boolean configurationPersistenceEnabled = persistenceService != null;

    ConfigurationMutator configurationMutator =
        (new ConfigurationMutatorFactory()).generate(validatedConfig);
    // exit early if config element already exists in cache config
    if (configurationPersistenceEnabled) {
      CacheConfig currentPersistedConfig = persistenceService.getCacheConfig(group, true);
      if (configurationMutator.exists(validatedConfig, currentPersistedConfig)) {
        throw new EntityExistsException(
            "cache element " + validatedConfig.getId() + " already exists.");
      }
    }

    // execute function on all members
    Set<DistributedMember> targetedMembers = findMembers(null, null);
    if (targetedMembers.size() == 0) {
      throw new NoMembersException("no members found to create cache element");
    }

    List<CliFunctionResult> functionResults = executeAndGetFunctionResult(
        new UpdateCacheFunction(),
        Arrays.asList(validatedConfig, UpdateCacheFunction.CacheElementOperation.ADD),
        targetedMembers);
    functionResults
        .forEach(functionResult -> result.addMemberStatus(functionResult.getMemberIdOrName(),
            new Status(functionResult.isSuccessful(), functionResult.getStatusMessage())));

    // persist configuration in cache config
    if (configurationPersistenceEnabled) {
      persistenceService.updateCacheConfig(group, cacheConfigForGroup -> {
        try {
          configurationMutator.add(config, cacheConfigForGroup);
          result.setClusterConfigPersisted(new Status(true,
              "successfully persisted config for " + group));
        } catch (Exception e) {
          String message = "failed to update cluster config for " + group;
          logger.error(message, e);
          result.setClusterConfigPersisted(new Status(false, message));
          return null;
        }

        return cacheConfigForGroup;
      });
    }

    return result;
  }

  @Override
  public ClusterManagementResult delete(CacheElement config) {
    throw new NotImplementedException();
  }

  @Override
  public ClusterManagementResult update(CacheElement config) {
    throw new NotImplementedException();
  }

  private Set<DistributedMember> findMembers(String[] groups, String[] members) {
    return CliUtil.findMembers(groups, members, distributionManager);
  }

  private List<CliFunctionResult> executeAndGetFunctionResult(Function function, Object args,
      Set<DistributedMember> targetMembers) {
    ResultCollector rc = CliUtil.executeFunction(function, args, targetMembers);
    return CliFunctionResult.cleanResults((List<?>) rc.getResult());
  }
}
