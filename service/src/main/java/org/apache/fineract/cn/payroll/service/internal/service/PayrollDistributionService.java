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
package org.apache.fineract.cn.payroll.service.internal.service;

import org.apache.fineract.cn.payroll.api.v1.domain.PayrollCollectionHistory;
import org.apache.fineract.cn.payroll.api.v1.domain.PayrollPaymentPage;
import org.apache.fineract.cn.payroll.service.ServiceConstants;
import org.apache.fineract.cn.payroll.service.internal.mapper.PayrollPaymentMapper;
import org.apache.fineract.cn.payroll.service.internal.repository.PayrollCollectionEntity;
import org.apache.fineract.cn.payroll.service.internal.repository.PayrollCollectionRepository;
import org.apache.fineract.cn.payroll.service.internal.repository.PayrollPaymentEntity;
import org.apache.fineract.cn.payroll.service.internal.repository.PayrollPaymentRepository;
import org.apache.fineract.cn.payroll.service.internal.service.adaptor.AccountingAdaptor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cn.lang.DateConverter;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PayrollDistributionService {

  private final Logger logger;
  private final PayrollCollectionRepository payrollCollectionRepository;
  private final PayrollPaymentRepository payrollPaymentRepository;
  private final AccountingAdaptor accountingAdaptor;

  @Autowired
  public PayrollDistributionService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                    final PayrollCollectionRepository payrollCollectionRepository,
                                    final PayrollPaymentRepository payrollPaymentRepository,
                                    final AccountingAdaptor accountingAdaptor) {
    super();
    this.logger = logger;
    this.payrollCollectionRepository = payrollCollectionRepository;
    this.payrollPaymentRepository = payrollPaymentRepository;
    this.accountingAdaptor = accountingAdaptor;
  }


  public List<PayrollCollectionHistory> fetchHistory() {
    return this.payrollCollectionRepository.findAllByOrderByCreatedOnDesc()
        .stream()
        .map(this::mapPayrollCollection)
        .collect(Collectors.toList());
  }

  public Optional<PayrollCollectionHistory> findDistribution(final String identifier) {
    return this.payrollCollectionRepository.findByIdentifier(identifier)
        .map(this::mapPayrollCollection);
  }

  public PayrollPaymentPage fetchPayments(final String identifier, final Pageable pageable) {
    final PayrollPaymentPage payrollPaymentPage = new PayrollPaymentPage();

    final PayrollCollectionEntity payrollCollectionEntity =
        this.payrollCollectionRepository.findByIdentifier(identifier).orElseThrow(
            () -> ServiceException.notFound("Payroll distribution {0} not found.", identifier)
        );

    final Page<PayrollPaymentEntity> pagedEntities =
        this.payrollPaymentRepository.findByPayrollCollection(payrollCollectionEntity, pageable);
    payrollPaymentPage.setTotalElements(pagedEntities.getTotalElements());
    payrollPaymentPage.setTotalPages(pagedEntities.getTotalPages());
    pagedEntities.forEach(
        payrollPaymentEntity -> payrollPaymentPage.add(PayrollPaymentMapper.map(payrollPaymentEntity))
    );

    return payrollPaymentPage;
  }

  private PayrollCollectionHistory mapPayrollCollection(final PayrollCollectionEntity payrollCollectionEntity) {
    final PayrollCollectionHistory payrollCollectionHistory = new PayrollCollectionHistory();
    payrollCollectionHistory.setIdentifier(payrollCollectionEntity.getIdentifier());
    payrollCollectionHistory.setSourceAccountNumber(payrollCollectionEntity.getSourceAccountNumber());
    payrollCollectionHistory.setCreatedBy(payrollCollectionEntity.getCreatedBy());
    payrollCollectionHistory.setCreatedOn(DateConverter.toIsoString(payrollCollectionEntity.getCreatedOn()));
    return payrollCollectionHistory;
  }
}
