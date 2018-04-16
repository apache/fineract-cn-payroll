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
package io.mifos.payroll.service.rest;

import io.mifos.payroll.api.v1.PermittableGroupIds;
import io.mifos.payroll.api.v1.domain.PayrollAllocation;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import io.mifos.payroll.service.ServiceConstants;
import io.mifos.payroll.service.internal.command.PutPayrollConfigurationCommand;
import io.mifos.payroll.service.internal.service.PayrollConfigurationService;
import java.util.List;
import javax.validation.Valid;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.anubis.annotation.Permittables;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers/{identifier}/payroll")
public class PayrollConfigurationRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final PayrollConfigurationService payrollConfigurationService;

  @Autowired
  public PayrollConfigurationRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                            final CommandGateway commandGateway,
                                            final PayrollConfigurationService payrollConfigurationService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.payrollConfigurationService = payrollConfigurationService;
  }

  @Permittables({
      @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CONFIGURATION)
  })
  @RequestMapping(
      method = RequestMethod.PUT,
      consumes = {
          MediaType.APPLICATION_JSON_VALUE
      },
      produces = {
          MediaType.APPLICATION_JSON_VALUE
      }
  )
  @ResponseBody
  public ResponseEntity<Void> setPayrollConfiguration(@PathVariable(value = "identifier") final String customerIdentifier,
                                                     @RequestBody @Valid final PayrollConfiguration payrollConfiguration) {
    this.payrollConfigurationService.findCustomer(customerIdentifier)
        .orElseThrow(() -> ServiceException
            .notFound("Customer {0} not available.", customerIdentifier)
    );

    this.payrollConfigurationService.findAccount(payrollConfiguration.getMainAccountNumber())
        .orElseThrow(() -> ServiceException.notFound("Main account {0} not available.", payrollConfiguration.getMainAccountNumber()));

    if (payrollConfiguration.getPayrollAllocations() != null) {

      final List<PayrollAllocation> payrollAllocations = payrollConfiguration.getPayrollAllocations();

      if (payrollAllocations.stream().anyMatch(payrollAllocation ->
          payrollAllocation.getAccountNumber().equals(payrollConfiguration.getMainAccountNumber()))) {
        throw ServiceException.conflict("Main account should not be used in allocations.");
      }

      if (payrollAllocations.stream().anyMatch(payrollAllocation ->
          !this.payrollConfigurationService.findAccount(payrollAllocation.getAccountNumber()).isPresent())) {
        throw ServiceException.notFound("Certain allocated accounts not available.");
      }
    }

    this.commandGateway.process(new PutPayrollConfigurationCommand(customerIdentifier, payrollConfiguration));

    return ResponseEntity.accepted().build();
  }

  @Permittables({
      @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CONFIGURATION)
  })
  @RequestMapping(
      method = RequestMethod.GET,
      consumes = {
          MediaType.ALL_VALUE
      },
      produces = {
          MediaType.APPLICATION_JSON_VALUE
      }
  )
  @ResponseBody
  PayrollConfiguration findPayrollConfiguration(@PathVariable(value = "identifier") final String customerIdentifier) {
    return this.payrollConfigurationService.findPayrollConfiguration(customerIdentifier)
        .orElseThrow(() -> ServiceException.notFound("Payroll configuration for customer {0} not found.", customerIdentifier));
  }
}
