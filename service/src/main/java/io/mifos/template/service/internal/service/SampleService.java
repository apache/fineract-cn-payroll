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
package io.mifos.template.service.internal.service;

import io.mifos.template.api.v1.domain.Sample;
import io.mifos.template.service.internal.mapper.SampleMapper;
import io.mifos.template.service.internal.repository.SampleJpaEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SampleService {

  private final SampleJpaEntityRepository sampleJpaEntityRepository;

  @Autowired
  public SampleService(final SampleJpaEntityRepository sampleJpaEntityRepository) {
    super();
    this.sampleJpaEntityRepository = sampleJpaEntityRepository;
  }

  public List<Sample> findAllEntities() {
    return SampleMapper.map(this.sampleJpaEntityRepository.findAll());
  }

  public Optional<Sample> findByIdentifier(final String identifier) {
    return this.sampleJpaEntityRepository.findByIdentifier(identifier).map(SampleMapper::map);
  }
}
