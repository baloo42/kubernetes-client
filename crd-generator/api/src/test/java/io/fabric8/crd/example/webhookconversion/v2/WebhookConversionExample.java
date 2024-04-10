/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.crd.example.webhookconversion.v2;

import io.fabric8.crd.generator.annotation.WebhookConversion;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("sample.fabric8.io")
@Version("v2")
@Kind("WebhookConversion")
@WebhookConversion(versions = { "v2", "v1" }, serviceName = "conversion-webhook", serviceNamespace = "my-namespace")
public class WebhookConversionExample extends CustomResource<WebhookConversionSpec, Void> {
}
