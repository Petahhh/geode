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
package org.apache.geode.management.internal.api;

import java.util.HashMap;
import java.util.Map;

public class ClusterManagementResult extends ClusterManagementResultBase {
  private Map<String, Status> memberStatuses = new HashMap<>();

  private Status persistenceStatus = new Status(Status.Result.NOT_APPLICABLE, null);

  public ClusterManagementResult() {
    super();
  }

  public void addMemberStatus(String member, Status status) {
    this.memberStatuses.put(member, status);
  }

  public void setClusterConfigPersisted(Status status) {
    this.persistenceStatus = status;
  }

  public Map<String, Status> getMemberStatuses() {
    return memberStatuses;
  }

  public Status getPersistenceStatus() {
    return persistenceStatus;
  }

  private boolean isSuccessfullyAppliedOnMembers() {
    if (memberStatuses.isEmpty()) {
      return false;
    }
    return memberStatuses.values().stream().allMatch(x -> x.result == Status.Result.SUCCESS);
  }

  private boolean isSuccessfullyPersisted() {
    return persistenceStatus.result == Status.Result.SUCCESS;
  }

  /**
   * - true if operation is successful on all distributed members,
   * and configuration persistence is either not applicable (in case cluster config is disabled)
   * or configuration persistence is applicable and successful
   * - false otherwise
   */
  @Override
  public Status getStatus() {
    boolean success =
        (persistenceStatus.result == Status.Result.NOT_APPLICABLE || isSuccessfullyPersisted())
            && isSuccessfullyAppliedOnMembers();
    if (success) {
      return new Status(Status.Result.SUCCESS, "Successfully applied configuration change.");
    } else {
      return new Status(Status.Result.FAILURE, "Failed to apply configuration change.");
    }
  }

}
