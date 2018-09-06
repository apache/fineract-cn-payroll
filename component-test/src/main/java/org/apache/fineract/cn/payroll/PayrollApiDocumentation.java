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
package org.apache.fineract.cn.payroll;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;

import org.apache.fineract.cn.payroll.api.v1.EventConstants;
import org.apache.fineract.cn.payroll.api.v1.domain.*;
import org.apache.fineract.cn.payroll.domain.DomainObjectGenerator;
import org.apache.fineract.cn.payroll.service.internal.repository.PayrollCollectionEntity;
import org.apache.fineract.cn.payroll.service.internal.service.adaptor.AccountingAdaptor;
import org.apache.fineract.cn.payroll.service.internal.service.adaptor.CustomerAdaptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

public class PayrollApiDocumentation extends AbstractPayrollTest {

  @Rule
  public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/doc/generated-snippets/test-payroll");

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockBean
  private CustomerAdaptor customerAdaptor;

  @MockBean
  private AccountingAdaptor accountingAdaptor;

  @Before
  public void setUp ( ) {

    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
  }

  @Test
  public void documentSetPayrollConfiguration ( ) throws Exception {

    final PayrollConfiguration payrollConfiguration = DomainObjectGenerator.getPayrollConfiguration();
    payrollConfiguration.setMainAccountNumber("12345678910");

    final PayrollAllocation savingsAllocation = new PayrollAllocation();
    savingsAllocation.setAccountNumber("9876543210");
    savingsAllocation.setAmount(BigDecimal.valueOf(5500.00D));
    savingsAllocation.setProportional(Boolean.TRUE);

    final PayrollAllocation tradeUnionAllocation = new PayrollAllocation();
    tradeUnionAllocation.setAccountNumber("24681097531");
    tradeUnionAllocation.setAmount(BigDecimal.valueOf(43.00D));
    tradeUnionAllocation.setProportional(Boolean.TRUE);

    final ArrayList <PayrollAllocation> payrollAllocations = new ArrayList <>();
    payrollConfiguration.setPayrollAllocations(payrollAllocations);
    payrollAllocations.add(savingsAllocation);
    payrollAllocations.add(tradeUnionAllocation);

    final Customer customer = new Customer();
    customer.setIdentifier("customerOne");

    this.prepareConfigurationMocks(customer.getIdentifier(), payrollConfiguration);

    Gson gson = new Gson();
    this.mockMvc.perform(put("/customers/" + customer.getIdentifier() + "/payroll")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(payrollConfiguration)))
            .andExpect(status().isAccepted())
            .andDo(document("document-set-payroll-configuration", preprocessRequest(prettyPrint()),
                    requestFields(
                            fieldWithPath("mainAccountNumber").description("Main account number"),
                            fieldWithPath("payrollAllocations[].accountNumber").description("Account from where you pay first allocation"),
                            fieldWithPath("payrollAllocations[].amount").type("Integer").description("Amount to be paid during first allocation"),
                            fieldWithPath("payrollAllocations[].proportional").type("Boolean").description("Should payments be proportional ?"),
                            fieldWithPath("payrollAllocations[1].accountNumber").description("Account from where you pay second allocation"),
                            fieldWithPath("payrollAllocations[1].amount").type("Integer").description("Amount to be paid during first allocation"),
                            fieldWithPath("payrollAllocations[1].proportional").type("Boolean").description("Should payments be proportional ?")
                    )
            ));
  }

  @Test
  public void documentFindPayrollConfiguration ( ) throws Exception {

    final PayrollConfiguration payrollConf = DomainObjectGenerator.getPayrollConfiguration();
    payrollConf.setMainAccountNumber("AB12345");

    final PayrollAllocation savingsAllocation = new PayrollAllocation();
    savingsAllocation.setAccountNumber("BAH97531");
    savingsAllocation.setAmount(BigDecimal.valueOf(73.00D));
    savingsAllocation.setProportional(Boolean.TRUE);

    final PayrollAllocation tradeUnionAllocation = new PayrollAllocation();
    tradeUnionAllocation.setAccountNumber("CAG24680");
    tradeUnionAllocation.setAmount(BigDecimal.valueOf(21.00D));
    tradeUnionAllocation.setProportional(Boolean.TRUE);

    final ArrayList <PayrollAllocation> payrollAllocations = new ArrayList <>();
    payrollConf.setPayrollAllocations(payrollAllocations);
    payrollAllocations.add(savingsAllocation);
    payrollAllocations.add(tradeUnionAllocation);

    final Customer customer = new Customer();
    customer.setIdentifier("faundCostoma");

    this.prepareConfigurationMocks(customer.getIdentifier(), payrollConf);
    super.testSubject.setPayrollConfiguration(customer.getIdentifier(), payrollConf);
    super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customer.getIdentifier());

    this.mockMvc.perform(get("/customers/" + customer.getIdentifier() + "/payroll")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-find-payroll-configuration", preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("mainAccountNumber").description("Main account number"),
                            fieldWithPath("payrollAllocations[].accountNumber").description("Account from where you pay first allocation"),
                            fieldWithPath("payrollAllocations[].amount").type("Integer").description("Amount to be paid during first allocation"),
                            fieldWithPath("payrollAllocations[].proportional").type("Boolean").description("Should payments be proportional ?"),
                            fieldWithPath("payrollAllocations[1].accountNumber").description("Account from where you pay second allocation"),
                            fieldWithPath("payrollAllocations[1].amount").type("Integer").description("Amount to be paid during first allocation"),
                            fieldWithPath("payrollAllocations[1].proportional").type("Boolean").description("Should payments be proportional ?"),
                            fieldWithPath("createdBy").description("Employee who configured payroll"),
                            fieldWithPath("createdOn").description("Date when payroll was configured"),
                            fieldWithPath("lastModifiedBy").type("String").description("Employee who last modified payroll"),
                            fieldWithPath("lastModifiedOn").type("String").description("Date when payroll was last modified")
                    )
            ));
  }

  @Test
  public void documentDistributePayments ( ) throws Exception {

    final PayrollConfiguration payrollConf = DomainObjectGenerator.getPayrollConfiguration();
    payrollConf.setMainAccountNumber("ABC09876");

    final Customer customer = new Customer();
    customer.setIdentifier("flauna");

    this.prepareDistributionMocks(customer.getIdentifier(), payrollConf);
    super.testSubject.setPayrollConfiguration(customer.getIdentifier(), payrollConf);
    super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customer.getIdentifier());

    final PayrollCollectionSheet payrollSheet = new PayrollCollectionSheet();
    payrollSheet.setSourceAccountNumber("S1R2C3A4C5C6");
    final PayrollPayment firstPayrollPayment = new PayrollPayment();
    firstPayrollPayment.setCustomerIdentifier(customer.getIdentifier());
    firstPayrollPayment.setEmployer("The Shop");
    firstPayrollPayment.setSalary(BigDecimal.valueOf(1234.56D));

    final PayrollPayment secondPayrollPayment = new PayrollPayment();
    secondPayrollPayment.setCustomerIdentifier(customer.getIdentifier());
    secondPayrollPayment.setEmployer("The Tank");
    secondPayrollPayment.setSalary(BigDecimal.valueOf(14.54D));

    payrollSheet.setPayrollPayments(Lists.newArrayList(firstPayrollPayment, secondPayrollPayment));

    final Account sourceAccount = new Account();
    sourceAccount.setState(Account.State.OPEN.name());
    Mockito
            .doAnswer(invocation -> Optional.of(sourceAccount))
            .when(this.accountingAdaptor).findAccount(Matchers.eq(payrollSheet.getSourceAccountNumber()));

    Mockito
            .doAnswer(invocation -> Optional.empty())
            .when(this.accountingAdaptor).postPayrollPayment(
            Matchers.any(PayrollCollectionEntity.class),
            Matchers.refEq(firstPayrollPayment),
            Matchers.any(PayrollConfiguration.class)
    );

    Mockito
            .doAnswer(invocation -> Optional.empty())
            .when(this.accountingAdaptor).postPayrollPayment(
            Matchers.any(PayrollCollectionEntity.class),
            Matchers.refEq(secondPayrollPayment),
            Matchers.any(PayrollConfiguration.class)
    );

    Gson gson = new Gson();
    this.mockMvc.perform(post("/distribution")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(payrollSheet)))
            .andExpect(status().isAccepted())
            .andDo(document("document-distribute-payments", preprocessRequest(prettyPrint()),
                    requestFields(
                            fieldWithPath("sourceAccountNumber").description("Account from which payments ensue"),
                            fieldWithPath("payrollPayments[].customerIdentifier").description("first customer's identifier"),
                            fieldWithPath("payrollPayments[].employer").description("first customer's employer"),
                            fieldWithPath("payrollPayments[].salary").description("first customer's salary"),
                            fieldWithPath("payrollPayments[1].customerIdentifier").description("second customer's identifier"),
                            fieldWithPath("payrollPayments[1].employer").description("second customer's employer"),
                            fieldWithPath("payrollPayments[1].salary").description("second customer's salary")
                    )));
  }

  @Test
  public void documentFetchDistributionHistory ( ) throws Exception {

    final PayrollConfiguration payrollConf = DomainObjectGenerator.getPayrollConfiguration();
    payrollConf.setMainAccountNumber("XYVZ12345");

    final Customer customer = new Customer();
    customer.setIdentifier("splundna");

    this.prepareDistributionMocks(customer.getIdentifier(), payrollConf);
    super.testSubject.setPayrollConfiguration(customer.getIdentifier(), payrollConf);
    super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customer.getIdentifier());

    final PayrollCollectionSheet payrollSheet = new PayrollCollectionSheet();
    payrollSheet.setSourceAccountNumber("S9R7C5A3C1C");
    final PayrollPayment firstPayrollPayment = new PayrollPayment();
    firstPayrollPayment.setCustomerIdentifier(customer.getIdentifier());
    firstPayrollPayment.setEmployer("Awa & Sons");
    firstPayrollPayment.setSalary(BigDecimal.valueOf(234.56D));

    final PayrollPayment secondPayrollPayment = new PayrollPayment();
    secondPayrollPayment.setCustomerIdentifier(customer.getIdentifier());
    secondPayrollPayment.setEmployer("Njeiforbi");
    secondPayrollPayment.setSalary(BigDecimal.valueOf(120.D));

    payrollSheet.setPayrollPayments(Lists.newArrayList(firstPayrollPayment, secondPayrollPayment));

    final Account sourceAccount = new Account();
    sourceAccount.setState(Account.State.OPEN.name());
    Mockito
            .doAnswer(invocation -> Optional.of(sourceAccount))
            .when(this.accountingAdaptor).findAccount(Matchers.eq(payrollSheet.getSourceAccountNumber()));

    Mockito
            .doAnswer(invocation -> Optional.empty())
            .when(this.accountingAdaptor).postPayrollPayment(
            Matchers.any(PayrollCollectionEntity.class),
            Matchers.refEq(firstPayrollPayment),
            Matchers.any(PayrollConfiguration.class)
    );

    Mockito
            .doAnswer(invocation -> Optional.empty())
            .when(this.accountingAdaptor).postPayrollPayment(
            Matchers.any(PayrollCollectionEntity.class),
            Matchers.refEq(secondPayrollPayment),
            Matchers.any(PayrollConfiguration.class)
    );

    super.testSubject.distribute(payrollSheet);
    super.eventRecorder.wait(EventConstants.POST_DISTRIBUTION, payrollSheet.getSourceAccountNumber());

    this.mockMvc.perform(get("/distribution")
            .accept(MediaType.ALL_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-fetch-distribution-history", preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("[].identifier").description("Payroll history identifier"),
                            fieldWithPath("[].sourceAccountNumber").description("Account from which payments ensue"),
                            fieldWithPath("[].createdBy").description("Employee who distributed payroll"),
                            fieldWithPath("[].createdOn").description("Date when payroll was distributed")
                    )
            ));
  }

  @Test
  public void documentFetchPayments ( ) throws Exception {

    final PayrollConfiguration payrollConf = DomainObjectGenerator.getPayrollConfiguration();
    payrollConf.setMainAccountNumber("BDK232942");

    final Customer customer = new Customer();
    customer.setIdentifier("ngone");

    this.prepareDistributionMocks(customer.getIdentifier(), payrollConf);
    super.testSubject.setPayrollConfiguration(customer.getIdentifier(), payrollConf);
    super.eventRecorder.wait(EventConstants.PUT_CONFIGURATION, customer.getIdentifier());

    final PayrollCollectionSheet payrollSheet = new PayrollCollectionSheet();
    payrollSheet.setSourceAccountNumber("O3C87O643E45C4");
    final PayrollPayment firstPayrollPayment = new PayrollPayment();
    firstPayrollPayment.setCustomerIdentifier(customer.getIdentifier());
    firstPayrollPayment.setEmployer("Nkwane");
    firstPayrollPayment.setSalary(BigDecimal.valueOf(945));

    payrollSheet.setPayrollPayments(Lists.newArrayList(firstPayrollPayment));

    final Account sourceAccount = new Account();
    sourceAccount.setState(Account.State.OPEN.name());
    Mockito
            .doAnswer(invocation -> Optional.of(sourceAccount))
            .when(this.accountingAdaptor).findAccount(Matchers.eq(payrollSheet.getSourceAccountNumber()));

    Mockito
            .doAnswer(invocation -> Optional.empty())
            .when(this.accountingAdaptor).postPayrollPayment(
            Matchers.any(PayrollCollectionEntity.class),
            Matchers.refEq(firstPayrollPayment),
            Matchers.any(PayrollConfiguration.class)
    );

    super.testSubject.distribute(payrollSheet);
    super.eventRecorder.wait(EventConstants.POST_DISTRIBUTION, payrollSheet.getSourceAccountNumber());

    final List <PayrollCollectionHistory> payrollCollectionHistories = super.testSubject.fetchDistributionHistory();
    Assert.assertTrue(payrollCollectionHistories.size() >= 1);

    final PayrollCollectionHistory payrollCollectionHistory = payrollCollectionHistories.get(0);
    final PayrollPaymentPage payrollPaymentPage =
            super.testSubject.fetchPayments(payrollCollectionHistory.getIdentifier(), 0, 10, null, null);
    Assert.assertEquals(Long.valueOf(1L), payrollPaymentPage.getTotalElements());

    this.mockMvc.perform(get("/distribution/" + payrollCollectionHistories.get(0).getIdentifier() + "/payments")
            .accept(MediaType.ALL_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-fetch-payments", preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("payrollPayments[0].customerIdentifier").description("second customer's identifier"),
                            fieldWithPath("payrollPayments[0].employer").description("second customer's employer"),
                            fieldWithPath("payrollPayments[0].salary").description("second customer's salary"),
                            fieldWithPath("payrollPayments[0].processed").description("second customer's employer"),
                            fieldWithPath("payrollPayments[0].message").description("second customer's salary"),
                            fieldWithPath("totalPages").type("Integer").description("Pages of payroll payments"),
                            fieldWithPath("totalElements").type("Integer").description("Number of payroll payments")
                    )
            ));
  }

  private void prepareConfigurationMocks (final String customerIdentifier, final PayrollConfiguration payrollConfiguration) {
    Mockito
            .doAnswer(invocation -> Optional.of(new Customer()))
            .when(this.customerAdaptor).findCustomer(Matchers.eq(customerIdentifier));

    Mockito
            .doAnswer(invocation -> Optional.of(new Account()))
            .when(this.accountingAdaptor).findAccount(Matchers.eq(payrollConfiguration.getMainAccountNumber()));

    payrollConfiguration.getPayrollAllocations().forEach(payrollAllocation ->
            Mockito
                    .doAnswer(invocation -> Optional.of(new Account()))
                    .when(this.accountingAdaptor).findAccount(Matchers.eq(payrollAllocation.getAccountNumber()))
    );
  }

  private void prepareDistributionMocks (final String customerIdentifier, final PayrollConfiguration payrollConfiguration) {
    Mockito
            .doAnswer(invocation -> Optional.of(new Customer()))
            .when(this.customerAdaptor).findCustomer(Matchers.eq(customerIdentifier));

    final Account mainAccount = new Account();
    mainAccount.setState(Account.State.OPEN.name());
    Mockito
            .doAnswer(invocation -> Optional.of(mainAccount))
            .when(this.accountingAdaptor).findAccount(Matchers.eq(payrollConfiguration.getMainAccountNumber()));

    payrollConfiguration.getPayrollAllocations().forEach(payrollAllocation -> {
      final Account allocatedAccount = new Account();
      allocatedAccount.setState(Account.State.OPEN.name());
      Mockito
              .doAnswer(invocation -> Optional.of(allocatedAccount))
              .when(this.accountingAdaptor).findAccount(Matchers.eq(payrollAllocation.getAccountNumber()));
    });
  }
}
