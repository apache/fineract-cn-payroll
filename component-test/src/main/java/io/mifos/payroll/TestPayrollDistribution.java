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

import com.google.common.collect.Lists;
import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.payroll.api.v1.EventConstants;
import io.mifos.payroll.api.v1.client.PayrollPaymentValidationException;
import io.mifos.payroll.api.v1.domain.PayrollAllocation;
import io.mifos.payroll.api.v1.domain.PayrollCollectionHistory;
import io.mifos.payroll.api.v1.domain.PayrollCollectionSheet;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import io.mifos.payroll.api.v1.domain.PayrollPayment;
import io.mifos.payroll.api.v1.domain.PayrollPaymentPage;
import io.mifos.payroll.domain.DomainObjectGenerator;
import io.mifos.payroll.service.internal.repository.PayrollCollectionEntity;
import io.mifos.payroll.service.internal.service.adaptor.AccountingAdaptor;
import io.mifos.payroll.service.internal.service.adaptor.CustomerAdaptor;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class TestPayrollDistribution extends AbstractPayrollTest {

  @MockBean
  private CustomerAdaptor customerAdaptorSpy;
  @MockBean
  private AccountingAdaptor accountingAdaptorSpy;

  public TestPayrollDistribution() {
    super();
  }

  @Test
  public void shouldDistributePayments() throws Exception {
    final String customerIdentifier = RandomStringUtils.randomAlphanumeric(32);
    final PayrollConfiguration payrollConfiguration = DomainObjectGenerator.getPayrollConfiguration();
    this.prepareMocks(customerIdentifier, payrollConfiguration);

    super.testSubject.setPayrollConfiguration(customerIdentifier, payrollConfiguration);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customerIdentifier));

    final PayrollCollectionSheet payrollCollectionSheet = new PayrollCollectionSheet();
    payrollCollectionSheet.setSourceAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    final PayrollPayment payrollPayment = new PayrollPayment();
    payrollPayment.setCustomerIdentifier(customerIdentifier);
    payrollPayment.setEmployer("ACME, Inc.");
    payrollPayment.setSalary(BigDecimal.valueOf(1234.56D));
    payrollCollectionSheet.setPayrollPayments(Lists.newArrayList(payrollPayment));

    final Account sourceAccount = new Account();
    sourceAccount.setState(Account.State.OPEN.name());
    Mockito
        .doAnswer(invocation -> Optional.of(sourceAccount))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollCollectionSheet.getSourceAccountNumber()));

    Mockito
        .doAnswer(invocation -> Optional.empty())
        .when(this.accountingAdaptorSpy).postPayrollPayment(
        Matchers.any(PayrollCollectionEntity.class),
        Matchers.refEq(payrollPayment),
        Matchers.any(PayrollConfiguration.class)
    );

    super.testSubject.distribute(payrollCollectionSheet);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_DISTRIBUTION, payrollCollectionSheet.getSourceAccountNumber()));

    final List<PayrollCollectionHistory> payrollCollectionHistories = super.testSubject.fetchDistributionHistory();
    Assert.assertEquals(1, payrollCollectionHistories.size());

    final PayrollCollectionHistory payrollCollectionHistory = payrollCollectionHistories.get(0);
    final PayrollPaymentPage payrollPaymentPage =
        super.testSubject.fetchPayments(payrollCollectionHistory.getIdentifier(), 0, 10, null, null);
    Assert.assertEquals(Long.valueOf(1L), payrollPaymentPage.getTotalElements());

    final PayrollPayment fetchedPayrollPayment = payrollPaymentPage.getPayrollPayments().get(0);
    Assert.assertTrue(fetchedPayrollPayment.getProcessed());
  }

  @Test(expected = PayrollPaymentValidationException.class)
  public void shouldNotDistributePaymentsAllocatedAccountClosed() throws Exception {
    final String customerIdentifier = RandomStringUtils.randomAlphanumeric(32);
    final PayrollConfiguration payrollConfiguration = DomainObjectGenerator.getPayrollConfiguration();
    this.prepareMocks(customerIdentifier, payrollConfiguration);

    final PayrollAllocation invalidPayrollAllocation = new PayrollAllocation();
    invalidPayrollAllocation.setAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    invalidPayrollAllocation.setProportional(Boolean.FALSE);
    invalidPayrollAllocation.setAmount(BigDecimal.valueOf(200.00D));
    payrollConfiguration.getPayrollAllocations().add(invalidPayrollAllocation);

    final Account invalidPayrollAccount = new Account();
    invalidPayrollAccount.setState(Account.State.CLOSED.name());
    Mockito
        .doAnswer(invocation -> Optional.of(invalidPayrollAccount))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(invalidPayrollAllocation.getAccountNumber()));

    super.testSubject.setPayrollConfiguration(customerIdentifier, payrollConfiguration);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customerIdentifier));

    final PayrollCollectionSheet payrollCollectionSheet = new PayrollCollectionSheet();
    payrollCollectionSheet.setSourceAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    final PayrollPayment payrollPayment = new PayrollPayment();
    payrollPayment.setCustomerIdentifier(customerIdentifier);
    payrollPayment.setEmployer("ACME, Inc.");
    payrollPayment.setSalary(BigDecimal.valueOf(1234.56D));
    payrollCollectionSheet.setPayrollPayments(Lists.newArrayList(payrollPayment));

    final Account sourceAccount = new Account();
    sourceAccount.setState(Account.State.OPEN.name());
    Mockito
        .doAnswer(invocation -> Optional.of(sourceAccount))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollCollectionSheet.getSourceAccountNumber()));

    Mockito
        .doAnswer(invocation -> Optional.empty())
        .when(this.accountingAdaptorSpy).postPayrollPayment(
        Matchers.any(PayrollCollectionEntity.class),
        Matchers.refEq(payrollPayment),
        Matchers.any(PayrollConfiguration.class)
    );

    super.testSubject.distribute(payrollCollectionSheet);
  }

  @Test(expected = PayrollPaymentValidationException.class)
  public void shouldNotDistributePaymentsSourceAccountClosed() throws Exception {
    final String customerIdentifier = RandomStringUtils.randomAlphanumeric(32);
    final PayrollConfiguration payrollConfiguration = DomainObjectGenerator.getPayrollConfiguration();
    this.prepareMocks(customerIdentifier, payrollConfiguration);

    super.testSubject.setPayrollConfiguration(customerIdentifier, payrollConfiguration);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customerIdentifier));

    final PayrollCollectionSheet payrollCollectionSheet = new PayrollCollectionSheet();
    payrollCollectionSheet.setSourceAccountNumber(RandomStringUtils.randomAlphanumeric(34));
    final PayrollPayment payrollPayment = new PayrollPayment();
    payrollPayment.setCustomerIdentifier(customerIdentifier);
    payrollPayment.setEmployer("ACME, Inc.");
    payrollPayment.setSalary(BigDecimal.valueOf(1234.56D));
    payrollCollectionSheet.setPayrollPayments(Lists.newArrayList(payrollPayment));

    final Account sourceAccount = new Account();
    sourceAccount.setState(Account.State.CLOSED.name());
    Mockito
        .doAnswer(invocation -> Optional.of(sourceAccount))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollCollectionSheet.getSourceAccountNumber()));

    Mockito
        .doAnswer(invocation -> Optional.empty())
        .when(this.accountingAdaptorSpy).postPayrollPayment(
        Matchers.any(PayrollCollectionEntity.class),
        Matchers.refEq(payrollPayment),
        Matchers.any(PayrollConfiguration.class)
    );

    super.testSubject.distribute(payrollCollectionSheet);
  }

  private void prepareMocks(final String customerIdentifier, final PayrollConfiguration payrollConfiguration) {
    Mockito
        .doAnswer(invocation -> Optional.of(new Customer()))
        .when(this.customerAdaptorSpy).findCustomer(Matchers.eq(customerIdentifier));

    final Account mainAccount = new Account();
    mainAccount.setState(Account.State.OPEN.name());
    Mockito
        .doAnswer(invocation -> Optional.of(mainAccount))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollConfiguration.getMainAccountNumber()));

    payrollConfiguration.getPayrollAllocations().forEach(payrollAllocation -> {
      final Account allocatedAccount = new Account();
      allocatedAccount.setState(Account.State.OPEN.name());
      Mockito
          .doAnswer(invocation -> Optional.of(allocatedAccount))
          .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollAllocation.getAccountNumber()));
    });
  }
}
