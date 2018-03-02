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
package io.mifos.payroll.service.internal.service;

import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.payroll.api.v1.domain.PayrollAllocation;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import io.mifos.payroll.service.ServiceConstants;
import io.mifos.payroll.service.internal.mapper.PayrollAllocationMapper;
import io.mifos.payroll.service.internal.mapper.PayrollConfigurationMapper;
import io.mifos.payroll.service.internal.repository.PayrollAllocationRepository;
import io.mifos.payroll.service.internal.repository.PayrollConfigurationRepository;
import io.mifos.payroll.service.internal.service.adaptor.AccountingAdaptor;
import io.mifos.payroll.service.internal.service.adaptor.CustomerAdaptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollConfigurationService {

  private final Logger logger;
  private final PayrollConfigurationRepository payrollConfigurationRepository;
  private final PayrollAllocationRepository payrollAllocationRepository;
  private final CustomerAdaptor customerAdaptor;
  private final AccountingAdaptor accountingAdaptor;

  @Autowired
  public PayrollConfigurationService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                     final PayrollConfigurationRepository payrollConfigurationRepository,
                                     final PayrollAllocationRepository payrollAllocationRepository,
                                     final CustomerAdaptor customerAdaptor,
                                     final AccountingAdaptor accountingAdaptor) {
    super();
    this.logger = logger;
    this.payrollConfigurationRepository = payrollConfigurationRepository;
    this.payrollAllocationRepository = payrollAllocationRepository;
    this.customerAdaptor = customerAdaptor;
    this.accountingAdaptor = accountingAdaptor;
  }

  public Optional<Customer> findCustomer(final String customerIdentifier) {
    return this.customerAdaptor.findCustomer(customerIdentifier);
  }

  public Optional<Account> findAccount(final String accountIdentifier) {
    return this.accountingAdaptor.findAccount(accountIdentifier);
  }

  public Optional<PayrollConfiguration> findPayrollConfiguration(final String customerIdentifier) {
    return this.payrollConfigurationRepository
        .findByCustomerIdentifier(customerIdentifier)
        .map(payrollConfigurationEntity -> {
          final PayrollConfiguration payrollConfiguration = PayrollConfigurationMapper.map(payrollConfigurationEntity);

          payrollConfiguration.setPayrollAllocations(
              this.payrollAllocationRepository.findByPayrollConfiguration(payrollConfigurationEntity)
                  .stream()
                  .map(PayrollAllocationMapper::map)
                  .sorted(Comparator.comparing(PayrollAllocation::getAccountNumber))
              .collect(Collectors.toList())
          );

          return payrollConfiguration;
        });
  }
}
