/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.payroll.service.rest;

import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.anubis.annotation.Permittables;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.ServiceException;
import io.mifos.payroll.api.v1.PermittableGroupIds;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import io.mifos.payroll.service.ServiceConstants;
import io.mifos.payroll.service.internal.command.PutPayrollConfigurationCommand;
import io.mifos.payroll.service.internal.service.PayrollConfigurationService;
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

import javax.validation.Valid;

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
        .orElseThrow(() -> ServiceException.notFound("Customer {0} not found.", customerIdentifier)
    );

    this.payrollConfigurationService.findAccount(payrollConfiguration.getMainAccountNumber())
        .orElseThrow(() -> ServiceException.notFound("Main account {0} not found.", payrollConfiguration.getMainAccountNumber()));

    if (payrollConfiguration.getPayrollAllocations() != null
        && payrollConfiguration.getPayrollAllocations()
        .stream()
        .filter(payrollAllocation ->
          !this.payrollConfigurationService.findAccount(payrollAllocation.getAccountNumber()).isPresent()
        ).count() > 0L) {
      throw ServiceException.notFound("Certain allocated accounts not found.");
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
