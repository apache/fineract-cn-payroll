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
package io.mifos.payroll;

import com.google.common.collect.Sets;
import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.payroll.api.v1.EventConstants;
import io.mifos.payroll.api.v1.domain.PayrollAllocation;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import io.mifos.payroll.domain.DomainObjectGenerator;
import io.mifos.payroll.service.internal.service.adaptor.AccountingAdaptor;
import io.mifos.payroll.service.internal.service.adaptor.CustomerAdaptor;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Optional;

public class TestPayrollConfiguration extends AbstractPayrollTest {

  @MockBean
  private CustomerAdaptor customerAdaptorSpy;
  @MockBean
  private AccountingAdaptor accountingAdaptorSpy;

  public TestPayrollConfiguration() {
    super();
  }

  @Test
  public void shouldCreatePayrollDistribution() throws Exception {
    final String customerIdentifier = RandomStringUtils.randomAlphanumeric(32);
    final PayrollConfiguration payrollConfiguration = DomainObjectGenerator.getPayrollConfiguration();
    this.prepareMocks(customerIdentifier, payrollConfiguration);

    super.testSubject.setPayrollConfiguration(customerIdentifier, payrollConfiguration);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customerIdentifier));
  }

  @Test
  public void shouldUpdatePayrollDistribution() throws Exception {
    final String customerIdentifier = RandomStringUtils.randomAlphanumeric(32);
    final PayrollConfiguration payrollConfiguration = DomainObjectGenerator.getPayrollConfiguration();
    this.prepareMocks(customerIdentifier, payrollConfiguration);

    super.testSubject.setPayrollConfiguration(customerIdentifier, payrollConfiguration);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customerIdentifier));

    final PayrollAllocation newPayrollAllocation = new PayrollAllocation();
    payrollConfiguration.setPayrollAllocations(Sets.newHashSet(newPayrollAllocation));
    newPayrollAllocation.setAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    newPayrollAllocation.setAmount(BigDecimal.valueOf(15.00D));
    newPayrollAllocation.setProportional(Boolean.FALSE);

    Mockito
        .doAnswer(invocation -> Optional.of(new Account()))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(newPayrollAllocation.getAccountNumber()));

    super.testSubject.setPayrollConfiguration(customerIdentifier, payrollConfiguration);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customerIdentifier));

    Thread.sleep(500L);

    final PayrollConfiguration fetchedPayrollConfiguration =
        super.testSubject.findPayrollConfiguration(customerIdentifier);

    Assert.assertNotNull(fetchedPayrollConfiguration.getLastModifiedBy());
    Assert.assertNotNull(fetchedPayrollConfiguration.getLastModifiedOn());
    Assert.assertEquals(1, fetchedPayrollConfiguration.getPayrollAllocations().size());

    final Optional<PayrollAllocation> optionalPayrollAllocation =
        fetchedPayrollConfiguration.getPayrollAllocations().stream().findFirst();

    Assert.assertTrue(optionalPayrollAllocation.isPresent());

    this.comparePayrollAllocations(newPayrollAllocation, optionalPayrollAllocation.get());
  }

  private void prepareMocks(final String customerIdentifier, final PayrollConfiguration payrollConfiguration) {
    Mockito
        .doAnswer(invocation -> Optional.of(new Customer()))
        .when(this.customerAdaptorSpy).findCustomer(Matchers.eq(customerIdentifier));

    Mockito
        .doAnswer(invocation -> Optional.of(new Account()))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollConfiguration.getMainAccountNumber()));

    payrollConfiguration.getPayrollAllocations().forEach(payrollAllocation ->
        Mockito
            .doAnswer(invocation -> Optional.of(new Account()))
            .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollAllocation.getAccountNumber()))
    );
  }

  private void comparePayrollAllocations(final PayrollAllocation expected, final PayrollAllocation actual) {
    Assert.assertEquals(expected.getAccountNumber(), actual.getAccountNumber());
    Assert.assertTrue(expected.getAmount().compareTo(actual.getAmount()) == 0);
    Assert.assertEquals(expected.getProportional(), actual.getProportional());
  }
}
