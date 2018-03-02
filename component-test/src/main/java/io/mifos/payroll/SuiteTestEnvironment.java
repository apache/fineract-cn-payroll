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

import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import io.mifos.core.test.fixture.mariadb.MariaDBInitializer;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.RunExternalResourceOnce;
import org.junit.rules.TestRule;

/**
 * This contains the database resources required by the test.  They are in a separate
 * class so that the test suite can initialize them before the classes it calls. This
 * makes test runs faster and prevents tests from "stepping on each other's toes" when
 * initializing and de-initializing external resources.
 */
public class SuiteTestEnvironment {
  static final String APP_VERSION = "1";
  static final String APP_NAME = "payroll-v" + APP_VERSION;

  static final TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  static final CassandraInitializer cassandraInitializer = new CassandraInitializer();
  static final MariaDBInitializer mariaDBInitializer = new MariaDBInitializer();

  @ClassRule
  public static TestRule orderClassRules = RuleChain
      .outerRule(new RunExternalResourceOnce(testEnvironment))
      .around(new RunExternalResourceOnce(cassandraInitializer))
      .around(new RunExternalResourceOnce(mariaDBInitializer));
}
