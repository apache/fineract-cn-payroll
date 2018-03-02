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
package io.mifos.payroll.listener;

import io.mifos.core.lang.config.TenantHeaderFilter;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.payroll.api.v1.EventConstants;
import io.mifos.payroll.service.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class PayrollDistributionListener {

  private final Logger logger;
  private final EventRecorder eventRecorder;

  @Autowired
  public PayrollDistributionListener(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                     final EventRecorder eventRecorder) {
    super();
    this.logger = logger;
    this.eventRecorder = eventRecorder;
  }

  @JmsListener(
      subscription = EventConstants.DESTINATION,
      destination = EventConstants.DESTINATION,
      selector = EventConstants.SELECTOR_POST_DISTRIBUTION
  )
  public void onPostCollection(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                               final String payload) {
    this.logger.info("Payment distribution with source account {0} processed.", payload);
    this.eventRecorder.event(tenant, EventConstants.POST_DISTRIBUTION, payload, String.class);
  }
}
