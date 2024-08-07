
package io.fabric8.kubernetes.api.model;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.authorization.v1.ResourceAttributes;
import io.fabric8.kubernetes.api.model.version.Info;
import io.fabric8.openshift.api.model.console.v1.ConsoleCLIDownload;
import io.fabric8.openshift.api.model.console.v1.ConsoleCLIDownloadList;
import io.fabric8.openshift.api.model.console.v1.ConsoleExternalLogLink;
import io.fabric8.openshift.api.model.console.v1.ConsoleExternalLogLinkList;
import io.fabric8.openshift.api.model.console.v1.ConsoleLink;
import io.fabric8.openshift.api.model.console.v1.ConsoleLinkList;
import io.fabric8.openshift.api.model.console.v1.ConsoleNotification;
import io.fabric8.openshift.api.model.console.v1.ConsoleNotificationList;
import io.fabric8.openshift.api.model.console.v1.ConsoleQuickStart;
import io.fabric8.openshift.api.model.console.v1.ConsoleQuickStartList;
import io.fabric8.openshift.api.model.console.v1.ConsoleYAMLSample;
import io.fabric8.openshift.api.model.console.v1.ConsoleYAMLSampleList;
import io.fabric8.openshift.api.model.console.v1alpha1.ConsolePluginList;

@Generated("jsonschema2pojo")
public class ValidationSchema {

    private APIGroup aPIGroup;
    private APIGroupList aPIGroupList;
    private KubernetesList baseKubernetesList;
    private ConsoleCLIDownload consoleCLIDownload;
    private ConsoleCLIDownloadList consoleCLIDownloadList;
    private ConsoleExternalLogLink consoleExternalLogLink;
    private ConsoleExternalLogLinkList consoleExternalLogLinkList;
    private ConsoleLink consoleLink;
    private ConsoleLinkList consoleLinkList;
    private ConsoleNotification consoleNotification;
    private ConsoleNotificationList consoleNotificationList;
    private ConsolePluginList consolePlugin;
    private ConsolePluginList consolePluginList;
    private ConsoleQuickStart consoleQuickStart;
    private ConsoleQuickStartList consoleQuickStartList;
    private ConsoleYAMLSample consoleYAMLSample;
    private ConsoleYAMLSampleList consoleYAMLSampleList;
    private Info info;
    private ObjectMeta objectMeta;
    private Patch patch;
    private ResourceAttributes resourceAttributes;
    private Status status;
    private String time;
    private TypeMeta typeMeta;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public ValidationSchema() {
    }

    public ValidationSchema(APIGroup aPIGroup, APIGroupList aPIGroupList, KubernetesList baseKubernetesList, ConsoleCLIDownload consoleCLIDownload, ConsoleCLIDownloadList consoleCLIDownloadList, ConsoleExternalLogLink consoleExternalLogLink, ConsoleExternalLogLinkList consoleExternalLogLinkList, ConsoleLink consoleLink, ConsoleLinkList consoleLinkList, ConsoleNotification consoleNotification, ConsoleNotificationList consoleNotificationList, ConsolePluginList consolePlugin, ConsolePluginList consolePluginList, ConsoleQuickStart consoleQuickStart, ConsoleQuickStartList consoleQuickStartList, ConsoleYAMLSample consoleYAMLSample, ConsoleYAMLSampleList consoleYAMLSampleList, Info info, ObjectMeta objectMeta, Patch patch, ResourceAttributes resourceAttributes, Status status, String time, TypeMeta typeMeta) {
        super();
        this.aPIGroup = aPIGroup;
        this.aPIGroupList = aPIGroupList;
        this.baseKubernetesList = baseKubernetesList;
        this.consoleCLIDownload = consoleCLIDownload;
        this.consoleCLIDownloadList = consoleCLIDownloadList;
        this.consoleExternalLogLink = consoleExternalLogLink;
        this.consoleExternalLogLinkList = consoleExternalLogLinkList;
        this.consoleLink = consoleLink;
        this.consoleLinkList = consoleLinkList;
        this.consoleNotification = consoleNotification;
        this.consoleNotificationList = consoleNotificationList;
        this.consolePlugin = consolePlugin;
        this.consolePluginList = consolePluginList;
        this.consoleQuickStart = consoleQuickStart;
        this.consoleQuickStartList = consoleQuickStartList;
        this.consoleYAMLSample = consoleYAMLSample;
        this.consoleYAMLSampleList = consoleYAMLSampleList;
        this.info = info;
        this.objectMeta = objectMeta;
        this.patch = patch;
        this.resourceAttributes = resourceAttributes;
        this.status = status;
        this.time = time;
        this.typeMeta = typeMeta;
    }

    @JsonProperty("APIGroup")
    public APIGroup getAPIGroup() {
        return aPIGroup;
    }

    @JsonProperty("APIGroup")
    public void setAPIGroup(APIGroup aPIGroup) {
        this.aPIGroup = aPIGroup;
    }

    @JsonProperty("APIGroupList")
    public APIGroupList getAPIGroupList() {
        return aPIGroupList;
    }

    @JsonProperty("APIGroupList")
    public void setAPIGroupList(APIGroupList aPIGroupList) {
        this.aPIGroupList = aPIGroupList;
    }

    @JsonProperty("BaseKubernetesList")
    public KubernetesList getBaseKubernetesList() {
        return baseKubernetesList;
    }

    @JsonProperty("BaseKubernetesList")
    public void setBaseKubernetesList(KubernetesList baseKubernetesList) {
        this.baseKubernetesList = baseKubernetesList;
    }

    @JsonProperty("ConsoleCLIDownload")
    public ConsoleCLIDownload getConsoleCLIDownload() {
        return consoleCLIDownload;
    }

    @JsonProperty("ConsoleCLIDownload")
    public void setConsoleCLIDownload(ConsoleCLIDownload consoleCLIDownload) {
        this.consoleCLIDownload = consoleCLIDownload;
    }

    @JsonProperty("ConsoleCLIDownloadList")
    public ConsoleCLIDownloadList getConsoleCLIDownloadList() {
        return consoleCLIDownloadList;
    }

    @JsonProperty("ConsoleCLIDownloadList")
    public void setConsoleCLIDownloadList(ConsoleCLIDownloadList consoleCLIDownloadList) {
        this.consoleCLIDownloadList = consoleCLIDownloadList;
    }

    @JsonProperty("ConsoleExternalLogLink")
    public ConsoleExternalLogLink getConsoleExternalLogLink() {
        return consoleExternalLogLink;
    }

    @JsonProperty("ConsoleExternalLogLink")
    public void setConsoleExternalLogLink(ConsoleExternalLogLink consoleExternalLogLink) {
        this.consoleExternalLogLink = consoleExternalLogLink;
    }

    @JsonProperty("ConsoleExternalLogLinkList")
    public ConsoleExternalLogLinkList getConsoleExternalLogLinkList() {
        return consoleExternalLogLinkList;
    }

    @JsonProperty("ConsoleExternalLogLinkList")
    public void setConsoleExternalLogLinkList(ConsoleExternalLogLinkList consoleExternalLogLinkList) {
        this.consoleExternalLogLinkList = consoleExternalLogLinkList;
    }

    @JsonProperty("ConsoleLink")
    public ConsoleLink getConsoleLink() {
        return consoleLink;
    }

    @JsonProperty("ConsoleLink")
    public void setConsoleLink(ConsoleLink consoleLink) {
        this.consoleLink = consoleLink;
    }

    @JsonProperty("ConsoleLinkList")
    public ConsoleLinkList getConsoleLinkList() {
        return consoleLinkList;
    }

    @JsonProperty("ConsoleLinkList")
    public void setConsoleLinkList(ConsoleLinkList consoleLinkList) {
        this.consoleLinkList = consoleLinkList;
    }

    @JsonProperty("ConsoleNotification")
    public ConsoleNotification getConsoleNotification() {
        return consoleNotification;
    }

    @JsonProperty("ConsoleNotification")
    public void setConsoleNotification(ConsoleNotification consoleNotification) {
        this.consoleNotification = consoleNotification;
    }

    @JsonProperty("ConsoleNotificationList")
    public ConsoleNotificationList getConsoleNotificationList() {
        return consoleNotificationList;
    }

    @JsonProperty("ConsoleNotificationList")
    public void setConsoleNotificationList(ConsoleNotificationList consoleNotificationList) {
        this.consoleNotificationList = consoleNotificationList;
    }

    @JsonProperty("ConsolePlugin")
    public ConsolePluginList getConsolePlugin() {
        return consolePlugin;
    }

    @JsonProperty("ConsolePlugin")
    public void setConsolePlugin(ConsolePluginList consolePlugin) {
        this.consolePlugin = consolePlugin;
    }

    @JsonProperty("ConsolePluginList")
    public ConsolePluginList getConsolePluginList() {
        return consolePluginList;
    }

    @JsonProperty("ConsolePluginList")
    public void setConsolePluginList(ConsolePluginList consolePluginList) {
        this.consolePluginList = consolePluginList;
    }

    @JsonProperty("ConsoleQuickStart")
    public ConsoleQuickStart getConsoleQuickStart() {
        return consoleQuickStart;
    }

    @JsonProperty("ConsoleQuickStart")
    public void setConsoleQuickStart(ConsoleQuickStart consoleQuickStart) {
        this.consoleQuickStart = consoleQuickStart;
    }

    @JsonProperty("ConsoleQuickStartList")
    public ConsoleQuickStartList getConsoleQuickStartList() {
        return consoleQuickStartList;
    }

    @JsonProperty("ConsoleQuickStartList")
    public void setConsoleQuickStartList(ConsoleQuickStartList consoleQuickStartList) {
        this.consoleQuickStartList = consoleQuickStartList;
    }

    @JsonProperty("ConsoleYAMLSample")
    public ConsoleYAMLSample getConsoleYAMLSample() {
        return consoleYAMLSample;
    }

    @JsonProperty("ConsoleYAMLSample")
    public void setConsoleYAMLSample(ConsoleYAMLSample consoleYAMLSample) {
        this.consoleYAMLSample = consoleYAMLSample;
    }

    @JsonProperty("ConsoleYAMLSampleList")
    public ConsoleYAMLSampleList getConsoleYAMLSampleList() {
        return consoleYAMLSampleList;
    }

    @JsonProperty("ConsoleYAMLSampleList")
    public void setConsoleYAMLSampleList(ConsoleYAMLSampleList consoleYAMLSampleList) {
        this.consoleYAMLSampleList = consoleYAMLSampleList;
    }

    @JsonProperty("Info")
    public Info getInfo() {
        return info;
    }

    @JsonProperty("Info")
    public void setInfo(Info info) {
        this.info = info;
    }

    @JsonProperty("ObjectMeta")
    public ObjectMeta getObjectMeta() {
        return objectMeta;
    }

    @JsonProperty("ObjectMeta")
    public void setObjectMeta(ObjectMeta objectMeta) {
        this.objectMeta = objectMeta;
    }

    @JsonProperty("Patch")
    public Patch getPatch() {
        return patch;
    }

    @JsonProperty("Patch")
    public void setPatch(Patch patch) {
        this.patch = patch;
    }

    @JsonProperty("ResourceAttributes")
    public ResourceAttributes getResourceAttributes() {
        return resourceAttributes;
    }

    @JsonProperty("ResourceAttributes")
    public void setResourceAttributes(ResourceAttributes resourceAttributes) {
        this.resourceAttributes = resourceAttributes;
    }

    @JsonProperty("Status")
    public Status getStatus() {
        return status;
    }

    @JsonProperty("Status")
    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonProperty("Time")
    public String getTime() {
        return time;
    }

    @JsonProperty("Time")
    public void setTime(String time) {
        this.time = time;
    }

    @JsonProperty("TypeMeta")
    public TypeMeta getTypeMeta() {
        return typeMeta;
    }

    @JsonProperty("TypeMeta")
    public void setTypeMeta(TypeMeta typeMeta) {
        this.typeMeta = typeMeta;
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
