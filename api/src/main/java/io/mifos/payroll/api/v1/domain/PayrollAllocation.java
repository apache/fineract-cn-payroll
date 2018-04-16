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
package io.mifos.payroll.api.v1.domain;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;

public class PayrollAllocation {

  @ValidIdentifier(maxLength = 34)
  private String accountNumber;
  @NotNull
  @DecimalMin("0.001")
  @DecimalMax("9999999999.99999")
  private BigDecimal amount;
  private Boolean proportional = Boolean.FALSE;

  public PayrollAllocation() {
    super();
  }

  public String getAccountNumber() {
    return this.accountNumber;
  }

  public void setAccountNumber(final String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public void setAmount(final BigDecimal amount) {
    this.amount = amount;
  }

  public Boolean getProportional() {
    return this.proportional;
  }

  public void setProportional(final Boolean proportional) {
    this.proportional = proportional;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PayrollAllocation that = (PayrollAllocation) o;

    return accountNumber != null ? accountNumber.equals(that.accountNumber) : that.accountNumber == null;
  }

  @Override
  public int hashCode() {
    return accountNumber != null ? accountNumber.hashCode() : 0;
  }
}
