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
package io.mifos.payroll.domain;

import io.mifos.payroll.api.v1.domain.PayrollAllocation;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;

public class DomainObjectGenerator {

  private DomainObjectGenerator() {
    super();
  }

  public static PayrollConfiguration getPayrollConfiguration() {
    final PayrollConfiguration payrollConfiguration = new PayrollConfiguration();
    payrollConfiguration.setMainAccountNumber(RandomStringUtils.randomAlphanumeric(34));

    final ArrayList<PayrollAllocation> payrollAllocations = new ArrayList<>();
    payrollConfiguration.setPayrollAllocations(payrollAllocations);

    final PayrollAllocation savingsAllocation = new PayrollAllocation();
    payrollAllocations.add(savingsAllocation);
    savingsAllocation.setAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    savingsAllocation.setAmount(BigDecimal.valueOf(5.00D));
    savingsAllocation.setProportional(Boolean.TRUE);

    return payrollConfiguration;
  }
}
