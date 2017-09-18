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
import io.mifos.payroll.api.v1.domain.PayrollCollectionHistory;
import io.mifos.payroll.api.v1.domain.PayrollCollectionSheet;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import io.mifos.payroll.api.v1.domain.PayrollPayment;
import io.mifos.payroll.api.v1.domain.PayrollPaymentPage;
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

    Mockito
        .doAnswer(invocation -> Optional.of(new Account()))
        .when(this.accountingAdaptorSpy).findAccount(Matchers.eq(payrollCollectionSheet.getSourceAccountNumber()));

    super.testSubject.distribute(payrollCollectionSheet);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_DISTRIBUTION, payrollCollectionSheet.getSourceAccountNumber()));

    final List<PayrollCollectionHistory> payrollCollectionHistories = super.testSubject.fetchDistributionHistory();
    Assert.assertEquals(1, payrollCollectionHistories.size());

    final PayrollCollectionHistory payrollCollectionHistory = payrollCollectionHistories.get(0);
    final PayrollPaymentPage payrollPaymentPage =
        super.testSubject.fetchPayments(payrollCollectionHistory.getIdentifier(), 0, 10, null, null);
    Assert.assertEquals(Long.valueOf(1L), payrollPaymentPage.getTotalElements());
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

}
