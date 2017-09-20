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
import io.mifos.payroll.api.v1.domain.PayrollCollectionHistory;
import io.mifos.payroll.api.v1.domain.PayrollCollectionSheet;
import io.mifos.payroll.api.v1.domain.PayrollPaymentPage;
import io.mifos.payroll.service.ServiceConstants;
import io.mifos.payroll.service.internal.command.DistributePayrollCommand;
import io.mifos.payroll.service.internal.service.PayrollConfigurationService;
import io.mifos.payroll.service.internal.service.PayrollDistributionService;
import io.mifos.payroll.service.rest.util.PageableBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/distribution")
public class PayrollDistributionRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final PayrollDistributionService payrollDistributionService;
  private final PayrollConfigurationService payrollConfigurationService;

  @Autowired
  public PayrollDistributionRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                           final CommandGateway commandGateway,
                                           final PayrollDistributionService payrollDistributionService,
                                           final PayrollConfigurationService payrollConfigurationService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.payrollDistributionService = payrollDistributionService;
    this.payrollConfigurationService = payrollConfigurationService;
  }

  @Permittables({
      @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DISTRIBUTION)
  })
  @RequestMapping(
      method = RequestMethod.POST,
      consumes = {
          MediaType.APPLICATION_JSON_VALUE
      },
      produces = {
          MediaType.APPLICATION_JSON_VALUE
      }
  )
  @ResponseBody
  public ResponseEntity<Void> distribute(@RequestBody @Valid final PayrollCollectionSheet payrollCollectionSheet) {

    this.payrollConfigurationService.findAccount(payrollCollectionSheet.getSourceAccountNumber())
        .orElseThrow(() -> ServiceException.notFound("Account {0} not available.", payrollCollectionSheet.getSourceAccountNumber()));

    if (payrollCollectionSheet.getPayrollPayments()
        .stream().anyMatch(payrollPayment ->
              !this.payrollConfigurationService.findPayrollConfiguration(payrollPayment.getCustomerIdentifier()).isPresent())) {
      throw ServiceException.conflict("Payroll configuration for certain customers not available.");
    }

    this.commandGateway.process(new DistributePayrollCommand(payrollCollectionSheet));

    return ResponseEntity.accepted().build();
  }

  @Permittables({
      @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DISTRIBUTION)
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
  public ResponseEntity<List<PayrollCollectionHistory>> fetchDistributionHistory() {
    return ResponseEntity.ok(this.payrollDistributionService.fetchHistory());
  }

  @Permittables({
      @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DISTRIBUTION)
  })
  @RequestMapping(
      value = "/{identifier}/payments",
      method = RequestMethod.GET,
      consumes = {
          MediaType.ALL_VALUE
      },
      produces = {
          MediaType.APPLICATION_JSON_VALUE
      }
  )
  @ResponseBody
  ResponseEntity<PayrollPaymentPage> fetchPayments(
      @PathVariable("identifier") final String identifier,
      @RequestParam(value = "pageIndex", required = false) final Integer pageIndex,
      @RequestParam(value = "size", required = false) final Integer size,
      @RequestParam(value = "sortColumn", required = false) final String sortColumn,
      @RequestParam(value = "sortDirection", required = false) final String sortDirection) {
    this.payrollDistributionService.findDistribution(identifier)
        .orElseThrow(() -> ServiceException.notFound("Payroll distribution {0} not found."));

    return ResponseEntity.ok(this.payrollDistributionService
        .fetchPayments(identifier, PageableBuilder.create(pageIndex, size, sortColumn, sortDirection)));
  }
}
