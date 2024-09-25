
package io.fabric8.openshift.api.model.config.v1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.builder.Editable;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ca",
    "claims",
    "clientID",
    "clientSecret",
    "extraAuthorizeParameters",
    "extraScopes",
    "issuer"
})
@ToString
@EqualsAndHashCode
@Accessors(prefix = {
    "_",
    ""
})
@Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, lazyCollectionInitEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
    @BuildableReference(ObjectMeta.class),
    @BuildableReference(LabelSelector.class),
    @BuildableReference(Container.class),
    @BuildableReference(PodTemplateSpec.class),
    @BuildableReference(ResourceRequirements.class),
    @BuildableReference(IntOrString.class),
    @BuildableReference(ObjectReference.class),
    @BuildableReference(LocalObjectReference.class),
    @BuildableReference(PersistentVolumeClaim.class)
})
@Generated("jsonschema2pojo")
public class OAuthSpecIPOpenID implements Editable<OAuthSpecIPOpenIDBuilder> , KubernetesResource
{

    @JsonProperty("ca")
    private OAuthSpecIPOpenIDCa ca;
    @JsonProperty("claims")
    private OAuthSpecIPOpenIDClaims claims;
    @JsonProperty("clientID")
    private String clientID;
    @JsonProperty("clientSecret")
    private OAuthSpecIPOpenIDClientSecret clientSecret;
    @JsonProperty("extraAuthorizeParameters")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> extraAuthorizeParameters = new LinkedHashMap<>();
    @JsonProperty("extraScopes")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> extraScopes = new ArrayList<>();
    @JsonProperty("issuer")
    private String issuer;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public OAuthSpecIPOpenID() {
    }

    public OAuthSpecIPOpenID(OAuthSpecIPOpenIDCa ca, OAuthSpecIPOpenIDClaims claims, String clientID, OAuthSpecIPOpenIDClientSecret clientSecret, Map<String, String> extraAuthorizeParameters, List<String> extraScopes, String issuer) {
        super();
        this.ca = ca;
        this.claims = claims;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.extraAuthorizeParameters = extraAuthorizeParameters;
        this.extraScopes = extraScopes;
        this.issuer = issuer;
    }

    @JsonProperty("ca")
    public OAuthSpecIPOpenIDCa getCa() {
        return ca;
    }

    @JsonProperty("ca")
    public void setCa(OAuthSpecIPOpenIDCa ca) {
        this.ca = ca;
    }

    @JsonProperty("claims")
    public OAuthSpecIPOpenIDClaims getClaims() {
        return claims;
    }

    @JsonProperty("claims")
    public void setClaims(OAuthSpecIPOpenIDClaims claims) {
        this.claims = claims;
    }

    @JsonProperty("clientID")
    public String getClientID() {
        return clientID;
    }

    @JsonProperty("clientID")
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    @JsonProperty("clientSecret")
    public OAuthSpecIPOpenIDClientSecret getClientSecret() {
        return clientSecret;
    }

    @JsonProperty("clientSecret")
    public void setClientSecret(OAuthSpecIPOpenIDClientSecret clientSecret) {
        this.clientSecret = clientSecret;
    }

    @JsonProperty("extraAuthorizeParameters")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getExtraAuthorizeParameters() {
        return extraAuthorizeParameters;
    }

    @JsonProperty("extraAuthorizeParameters")
    public void setExtraAuthorizeParameters(Map<String, String> extraAuthorizeParameters) {
        this.extraAuthorizeParameters = extraAuthorizeParameters;
    }

    @JsonProperty("extraScopes")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<String> getExtraScopes() {
        return extraScopes;
    }

    @JsonProperty("extraScopes")
    public void setExtraScopes(List<String> extraScopes) {
        this.extraScopes = extraScopes;
    }

    @JsonProperty("issuer")
    public String getIssuer() {
        return issuer;
    }

    @JsonProperty("issuer")
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @JsonIgnore
    public OAuthSpecIPOpenIDBuilder edit() {
        return new OAuthSpecIPOpenIDBuilder(this);
    }

    @JsonIgnore
    public OAuthSpecIPOpenIDBuilder toBuilder() {
        return edit();
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

}