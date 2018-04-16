/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.payroll.service.internal.mapper;

import org.apache.fineract.cn.payroll.api.v1.domain.PayrollAllocation;
import org.apache.fineract.cn.payroll.service.internal.repository.PayrollAllocationEntity;

public class PayrollAllocationMapper {

  private PayrollAllocationMapper() {
    super();
  }

  public static PayrollAllocation map(final PayrollAllocationEntity payrollAllocationEntity) {
    final PayrollAllocation payrollAllocation = new PayrollAllocation();
    payrollAllocation.setAccountNumber(payrollAllocationEntity.getAccountNumber());
    payrollAllocation.setAmount(payrollAllocationEntity.getAmount());
    payrollAllocation.setProportional(payrollAllocationEntity.getProportional());
    return payrollAllocation;
  }

  public static PayrollAllocationEntity map(final PayrollAllocation payrollAllocation) {
    final PayrollAllocationEntity payrollAllocationEntity = new PayrollAllocationEntity();
    payrollAllocationEntity.setAccountNumber(payrollAllocation.getAccountNumber());
    payrollAllocationEntity.setAmount(payrollAllocation.getAmount());
    payrollAllocationEntity.setProportional(payrollAllocation.getProportional());
    return payrollAllocationEntity;
  }
}
