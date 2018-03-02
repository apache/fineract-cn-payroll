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
package io.mifos.payroll;


import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.listener.EnableEventRecording;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.payroll.api.v1.EventConstants;
import io.mifos.payroll.api.v1.client.PayrollManager;
import io.mifos.payroll.service.PayrollServiceConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {
        AbstractPayrollTest.TestConfiguration.class
    }
)
public class AbstractPayrollTest extends SuiteTestEnvironment {

  @Configuration
  @EnableEventRecording
  @EnableFeignClients(basePackages = {
      "io.mifos.payroll.api.v1.client"
  })
  @RibbonClient(name = SuiteTestEnvironment.APP_NAME)
  @ComponentScan(
      basePackages = {
          "io.mifos.payroll.listener"
      }
  )
  @Import({
      PayrollServiceConfiguration.class
  })
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }
  }

  static final String TEST_USER = "mage";

  @ClassRule
  public final static TenantDataStoreContextTestRule tenantDataStoreContext =
      TenantDataStoreContextTestRule.forRandomTenantName(SuiteTestEnvironment.cassandraInitializer,
          SuiteTestEnvironment.mariaDBInitializer);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
      = new TenantApplicationSecurityEnvironmentTestRule(SuiteTestEnvironment.testEnvironment, this::waitForInitialize);

  @Autowired
  EventRecorder eventRecorder;

  private AutoUserContext userContext;

  @Autowired
  PayrollManager testSubject;

  public AbstractPayrollTest() {
    super();
  }

  @Before
  public void prepareTest() {
    this.userContext = this.tenantApplicationSecurityEnvironment.createAutoUserContext(AbstractPayrollTest.TEST_USER);
  }

  @After
  public void cleanupTest() {
    userContext.close();
  }

  public boolean waitForInitialize() {
    try {
      return this.eventRecorder.wait(EventConstants.INITIALIZE, SuiteTestEnvironment.APP_VERSION);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

}
