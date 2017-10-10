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
package io.mifos.payroll.service.internal.service.adaptor;

import com.google.common.collect.Sets;
import io.mifos.accounting.api.v1.client.AccountNotFoundException;
import io.mifos.accounting.api.v1.client.LedgerManager;
import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.accounting.api.v1.domain.Creditor;
import io.mifos.accounting.api.v1.domain.Debtor;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.core.lang.DateConverter;
import io.mifos.payroll.api.v1.domain.PayrollConfiguration;
import io.mifos.payroll.api.v1.domain.PayrollPayment;
import io.mifos.payroll.service.ServiceConstants;
import io.mifos.payroll.service.internal.repository.PayrollCollectionEntity;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountingAdaptor {

  private final Logger logger;
  private final LedgerManager ledgerManager;

  @Autowired
  public AccountingAdaptor(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                           final LedgerManager ledgerManager) {
    super();
    this.logger = logger;
    this.ledgerManager = ledgerManager;
  }

  public Optional<Account> findAccount(final String accountIdentifier) {
    try {
      final Account account = this.ledgerManager.findAccount(accountIdentifier);
      if (account.getState().equals(Account.State.OPEN.name())) {
        return Optional.of(account);
      }
    } catch (final AccountNotFoundException anfex) {
      this.logger.warn("Account {} not found.", accountIdentifier);
    }
    return Optional.empty();
  }

  public Optional<String> postPayrollPayment(final PayrollCollectionEntity payrollCollectionEntity,
                                 final PayrollPayment payrollPayment,
                                 final PayrollConfiguration payrollConfiguration) {

    final MathContext mathContextAmount = new MathContext(2, RoundingMode.HALF_EVEN);
    final MathContext mathContextPercentage = new MathContext(5, RoundingMode.HALF_EVEN);

    final JournalEntry journalEntry = new JournalEntry();
    journalEntry.setTransactionIdentifier(UUID.randomUUID().toString());
    journalEntry.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    journalEntry.setTransactionType("SALA");
    journalEntry.setClerk(payrollCollectionEntity.getCreatedBy());
    journalEntry.setNote("Payroll Distribution");

    final Debtor debtor = new Debtor();
    debtor.setAccountNumber(payrollCollectionEntity.getSourceAccountNumber());
    debtor.setAmount(payrollPayment.getSalary().toString());
    journalEntry.setDebtors(Sets.newHashSet(debtor));

    final HashSet<Creditor> creditors = new HashSet<>();
    journalEntry.setCreditors(creditors);

    payrollConfiguration.getPayrollAllocations().forEach(payrollAllocation -> {
      final Creditor allocationCreditor = new Creditor();
      allocationCreditor.setAccountNumber(payrollAllocation.getAccountNumber());
      if (!payrollAllocation.getProportional()) {
        allocationCreditor.setAmount(payrollAllocation.getAmount().toString());
      } else {
        final BigDecimal value = payrollPayment.getSalary().multiply(
            payrollAllocation.getAmount().divide(BigDecimal.valueOf(100.00D), mathContextPercentage)
        ).round(mathContextAmount);
        allocationCreditor.setAmount(value.toString());
      }
      creditors.add(allocationCreditor);
    });

    final BigDecimal currentCreditorSum =
        BigDecimal.valueOf(creditors.stream().mapToDouble(value -> Double.valueOf(value.getAmount())).sum());

    final int comparedValue = currentCreditorSum.compareTo(payrollPayment.getSalary());
    if (comparedValue > 0) {
      return Optional.of("Allocated amount would exceed posted salary.");
    }
    if (comparedValue < 0) {
      final Creditor mainCreditor = new Creditor();
      mainCreditor.setAccountNumber(payrollConfiguration.getMainAccountNumber());
      mainCreditor.setAmount(payrollPayment.getSalary().subtract(currentCreditorSum).toString());
      creditors.add(mainCreditor);
    }

    try {
      this.ledgerManager.createJournalEntry(journalEntry);
      return Optional.empty();
    } catch (final Throwable th) {
      this.logger.warn("Could not process journal entry for customer {}.", payrollPayment.getCustomerIdentifier(), th);
      return Optional.of("Error while processing journal entry.");
    }
  }
}
