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
package org.apache.fineract.cn.payroll.service.internal.service.adaptor;

import org.apache.fineract.cn.payroll.service.ServiceConstants;
import java.util.Optional;
import org.apache.fineract.cn.customer.api.v1.client.CustomerManager;
import org.apache.fineract.cn.customer.api.v1.client.CustomerNotFoundException;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CustomerAdaptor {

  private final Logger logger;
  private final CustomerManager customerManager;

  @Autowired
  public CustomerAdaptor(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                         final CustomerManager customerManager) {
    super();
    this.logger = logger;
    this.customerManager = customerManager;
  }

  public Optional<Customer> findCustomer(final String customerIdentifier) {
    try {
      final Customer customer = this.customerManager.findCustomer(customerIdentifier);
      if (customer.getCurrentState().equals(Customer.State.ACTIVE.name())) {
        return Optional.of(customer);
      }
    } catch (final CustomerNotFoundException cnfex) {
      this.logger.warn("Customer {} not found.", customerIdentifier);
    }
    return Optional.empty();
  }
}
