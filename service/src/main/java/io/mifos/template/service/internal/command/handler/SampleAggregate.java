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
package io.mifos.template.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.template.api.v1.EventConstants;
import io.mifos.template.service.internal.command.SampleCommand;
import io.mifos.template.service.internal.repository.SampleJpaEntity;
import io.mifos.template.service.internal.repository.SampleJpaEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("unused")
@Aggregate
public class SampleAggregate {

  private final SampleJpaEntityRepository sampleJpaEntityRepository;

  @Autowired
  public SampleAggregate(final SampleJpaEntityRepository sampleJpaEntityRepository) {
    super();
    this.sampleJpaEntityRepository = sampleJpaEntityRepository;
  }

  @CommandHandler
  @Transactional
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.POST_SAMPLE)
  public String sample(final SampleCommand sampleCommand) {

    final SampleJpaEntity entity = new SampleJpaEntity();
    entity.setIdentifier(sampleCommand.sample().getIdentifier());
    entity.setPayload(sampleCommand.sample().getPayload());
    this.sampleJpaEntityRepository.save(entity);

    return sampleCommand.sample().getIdentifier();
  }
}
