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
package io.mifos.template.api.v1.client;

import io.mifos.core.api.annotation.ThrowsException;
import io.mifos.core.api.util.CustomFeignClientsConfiguration;
import io.mifos.template.api.v1.domain.Sample;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@SuppressWarnings("unused")
@FeignClient(value="template-v1", path="/template/v1", configuration = CustomFeignClientsConfiguration.class)
public interface TemplateManager {

  @RequestMapping(
          value = "/sample",
          method = RequestMethod.GET,
          produces = MediaType.ALL_VALUE,
          consumes = MediaType.APPLICATION_JSON_VALUE
  )
  List<Sample> findAllEntities();

  @RequestMapping(
          value = "/sample/{identifier}",
          method = RequestMethod.GET,
          produces = MediaType.ALL_VALUE,
          consumes = MediaType.APPLICATION_JSON_VALUE)
  Sample getEntity(@PathVariable("identifier") final String identifier);

  @RequestMapping(
      value = "/sample",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsException(status = HttpStatus.I_AM_A_TEAPOT, exception = IamATeapotException.class)
  void createEntity(final Sample sample);
}
