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
package io.fabric8.kubernetes.client;

import io.fabric8.kubernetes.api.model.admissionregistration.v1.MutatingWebhookConfiguration;
import io.fabric8.kubernetes.api.model.admissionregistration.v1.MutatingWebhookConfigurationList;
import io.fabric8.kubernetes.api.model.admissionregistration.v1.ValidatingAdmissionPolicy;
import io.fabric8.kubernetes.api.model.admissionregistration.v1.ValidatingAdmissionPolicyBinding;
import io.fabric8.kubernetes.api.model.admissionregistration.v1.ValidatingAdmissionPolicyBindingList;
import io.fabric8.kubernetes.api.model.admissionregistration.v1.ValidatingAdmissionPolicyList;
import io.fabric8.kubernetes.api.model.admissionregistration.v1.ValidatingWebhookConfiguration;
import io.fabric8.kubernetes.api.model.admissionregistration.v1.ValidatingWebhookConfigurationList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public interface V1AdmissionRegistrationAPIGroupDSL extends Client {
  NonNamespaceOperation<ValidatingWebhookConfiguration, ValidatingWebhookConfigurationList, Resource<ValidatingWebhookConfiguration>> validatingWebhookConfigurations();

  NonNamespaceOperation<MutatingWebhookConfiguration, MutatingWebhookConfigurationList, Resource<MutatingWebhookConfiguration>> mutatingWebhookConfigurations();

  /**
   * DSL entrypoint for admissionregistration.k8s.io/v1 ValidatingAdmissionPolicyBinding.
   * ValidatingAdmissionPolicyBinding binds the ValidatingAdmissionPolicy with paramerized resources.
   * ValidatingAdmissionPolicyBinding and parameter CRDs together define how cluster administrators configure policies for
   * clusters.
   *
   * @return NonNamespaceOperation for ValidatingAdmissionPolicyBinding
   */
  NonNamespaceOperation<ValidatingAdmissionPolicyBinding, ValidatingAdmissionPolicyBindingList, Resource<ValidatingAdmissionPolicyBinding>> validatingAdmissionPolicyBindings();

  /**
   * DSL entrypoint for admissionregistration.k8s.io/v1 ValidatingAdmissionPolicy.
   * ValidatingAdmissionPolicy describes the definition of an admission validation policy that accepts or rejects an object
   * without changing it.
   *
   * @return NonNamespaceOperation for ValidatingAdmissionPolicy
   */
  NonNamespaceOperation<ValidatingAdmissionPolicy, ValidatingAdmissionPolicyList, Resource<ValidatingAdmissionPolicy>> validatingAdmissionPolicies();
}
