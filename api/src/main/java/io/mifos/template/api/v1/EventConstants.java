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
package io.mifos.template.api.v1;

@SuppressWarnings("unused")
public interface EventConstants {

  String DESTINATION = "template-v1";
  String SELECTOR_NAME = "action";
  String INITIALIZE = "initialize";
  String POST_SAMPLE = "post-sample";
  String SELECTOR_INITIALIZE = SELECTOR_NAME + " = '" + INITIALIZE + "'";
  String SELECTOR_POST_SAMPLE = SELECTOR_NAME + " = '" + POST_SAMPLE + "'";
}
