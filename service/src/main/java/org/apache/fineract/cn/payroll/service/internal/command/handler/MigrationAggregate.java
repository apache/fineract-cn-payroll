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
package org.apache.fineract.cn.payroll.service.internal.command.handler;

import org.apache.fineract.cn.payroll.service.internal.command.MigrateServiceCommand;
import javax.sql.DataSource;
import org.apache.fineract.cn.accounting.api.v1.EventConstants;
import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.apache.fineract.cn.lang.ApplicationName;
import org.apache.fineract.cn.mariadb.domain.FlywayFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Aggregate
public class MigrationAggregate {

  private final DataSource dataSource;
  private final FlywayFactoryBean flywayFactoryBean;
  private final ApplicationName applicationName;

  @Autowired
  public MigrationAggregate(@SuppressWarnings("SpringJavaAutowiringInspection") final DataSource dataSource,
                            @SuppressWarnings("SpringJavaAutowiringInspection") final FlywayFactoryBean flywayFactoryBean,
                            final ApplicationName applicationName) {
    super();
    this.dataSource = dataSource;
    this.flywayFactoryBean = flywayFactoryBean;
    this.applicationName = applicationName;
  }

  @SuppressWarnings("unused")
  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.INITIALIZE)
  public String initialize(final MigrateServiceCommand migrateServiceCommand) {
    this.flywayFactoryBean.create(this.dataSource).migrate();
    return this.applicationName.getVersionString();
  }
}
