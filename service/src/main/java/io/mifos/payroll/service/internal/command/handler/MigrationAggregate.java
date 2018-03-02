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
package io.mifos.payroll.service.internal.command.handler;

import io.mifos.accounting.api.v1.EventConstants;
import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.ApplicationName;
import io.mifos.core.mariadb.domain.FlywayFactoryBean;
import io.mifos.payroll.service.internal.command.MigrateServiceCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

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
