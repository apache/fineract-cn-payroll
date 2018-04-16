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
package io.mifos.payroll.service.internal.command.handler;

import io.mifos.payroll.api.v1.EventConstants;
import io.mifos.payroll.api.v1.domain.PayrollCollectionSheet;
import io.mifos.payroll.service.ServiceConstants;
import io.mifos.payroll.service.internal.command.DistributePayrollCommand;
import io.mifos.payroll.service.internal.repository.PayrollCollectionEntity;
import io.mifos.payroll.service.internal.repository.PayrollCollectionRepository;
import io.mifos.payroll.service.internal.repository.PayrollPaymentEntity;
import io.mifos.payroll.service.internal.repository.PayrollPaymentRepository;
import io.mifos.payroll.service.internal.service.PayrollConfigurationService;
import io.mifos.payroll.service.internal.service.adaptor.AccountingAdaptor;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

@Aggregate
public class PayrollDistributionAggregate {

  private final Logger logger;
  private final PayrollConfigurationService payrollConfigurationService;
  private final PayrollCollectionRepository payrollCollectionRepository;
  private final PayrollPaymentRepository payrollPaymentRepository;
  private final AccountingAdaptor accountingAdaptor;

  @Autowired
  public PayrollDistributionAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                      PayrollConfigurationService payrollConfigurationService,
                                      final PayrollCollectionRepository payrollCollectionRepository,
                                      final PayrollPaymentRepository payrollPaymentRepository,
                                      final AccountingAdaptor accountingAdaptor) {
    super();
    this.logger = logger;
    this.payrollConfigurationService = payrollConfigurationService;
    this.payrollCollectionRepository = payrollCollectionRepository;
    this.payrollPaymentRepository = payrollPaymentRepository;
    this.accountingAdaptor = accountingAdaptor;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.POST_DISTRIBUTION)
  public String process(final DistributePayrollCommand distributePayrollCommand) {
    final PayrollCollectionSheet payrollCollectionSheet = distributePayrollCommand.payrollCollectionSheet();

    final PayrollCollectionEntity payrollCollectionEntity = new PayrollCollectionEntity();
    payrollCollectionEntity.setIdentifier(RandomStringUtils.randomAlphanumeric(32));
    payrollCollectionEntity.setSourceAccountNumber(payrollCollectionSheet.getSourceAccountNumber());
    payrollCollectionEntity.setCreatedBy(UserContextHolder.checkedGetUser());
    payrollCollectionEntity.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));

    final PayrollCollectionEntity savedPayrollCollectionEntity = this.payrollCollectionRepository.save(payrollCollectionEntity);

    payrollCollectionSheet.getPayrollPayments().forEach(payrollPayment ->
        this.payrollConfigurationService
            .findPayrollConfiguration(payrollPayment.getCustomerIdentifier())
            .ifPresent(payrollConfiguration -> {
              final PayrollPaymentEntity payrollPaymentEntity = new PayrollPaymentEntity();
              payrollPaymentEntity.setPayrollCollection(savedPayrollCollectionEntity);
              payrollPaymentEntity.setCustomerIdentifier(payrollPayment.getCustomerIdentifier());
              payrollPaymentEntity.setEmployer(payrollPayment.getEmployer());
              payrollPaymentEntity.setSalary(payrollPayment.getSalary());

              final Optional<String> optionalErrorMessage =
                  this.accountingAdaptor.postPayrollPayment(savedPayrollCollectionEntity, payrollPayment, payrollConfiguration);
              if (optionalErrorMessage.isPresent()) {
                payrollPaymentEntity.setMessage(optionalErrorMessage.get());
                payrollPaymentEntity.setProcessed(Boolean.FALSE);
              } else {
                payrollPaymentEntity.setProcessed(Boolean.TRUE);
              }
              this.payrollPaymentRepository.save(payrollPaymentEntity);
            })
    );

    return payrollCollectionSheet.getSourceAccountNumber();
  }
}
