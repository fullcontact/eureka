/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.netflix.appinfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.inject.ProvidedBy;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.discovery.converters.Auto;
import com.netflix.discovery.converters.EurekaJacksonCodec.InstanceInfoSerializer;
import com.netflix.discovery.provider.Serializer;
import com.netflix.discovery.util.StringCache;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class that holds information required for registration with
 * <tt>Eureka Server</tt> and to be discovered by other components.
 * <p>
 * <code>@Auto</code> annotated fields are serialized as is; Other fields are
 * serialized as specified by the <code>@Serializer</code>.
 * </p>
 *
 * @author Karthik Ranganathan, Greg Kim
 *
 */
@ProvidedBy(EurekaConfigBasedInstanceInfoProvider.class)
@Serializer("com.netflix.discovery.converters.EntityBodyConverter")
@XStreamAlias("instance")
@JsonRootName("instance")
public class InstanceInfo {
    private static final Logger logger = LoggerFactory.getLogger(InstanceInfo.class);
    private static final Pattern VIP_ATTRIBUTES_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    public static final int DEFAULT_PORT = 7001;
    public static final int DEFAULT_SECURE_PORT = 7002;
    public static final int DEFAULT_COUNTRY_ID = 1; // US

    private volatile String appName;
    @Auto
    private volatile String appGroupName;

    private volatile String ipAddr;
    private volatile String sid = "na";

    private volatile int port = DEFAULT_PORT;
    private volatile int securePort = DEFAULT_SECURE_PORT;

    @Auto
    private volatile String homePageUrl;
    @Auto
    private volatile String statusPageUrl;
    @Auto
    private volatile String healthCheckUrl;
    @Auto
    private volatile String secureHealthCheckUrl;
    @Auto
    private volatile String vipAddress;
    @Auto
    private volatile String secureVipAddress;
    @XStreamOmitField
    private String statusPageRelativeUrl;
    @XStreamOmitField
    private String statusPageExplicitUrl;
    @XStreamOmitField
    private String healthCheckRelativeUrl;
    @XStreamOmitField
    private String healthCheckSecureExplicitUrl;
    @XStreamOmitField
    private String vipAddressUnresolved;
    @XStreamOmitField
    private String secureVipAddressUnresolved;
    @XStreamOmitField
    private String healthCheckExplicitUrl;
    @Deprecated
    private volatile int countryId = DEFAULT_COUNTRY_ID; // Defaults to US
    private volatile boolean isSecurePortEnabled = false;
    private volatile boolean isUnsecurePortEnabled = true;
    private volatile DataCenterInfo dataCenterInfo;
    private volatile String hostName;
    private volatile InstanceStatus status = InstanceStatus.UP;
    private volatile InstanceStatus overriddenstatus = InstanceStatus.UNKNOWN;
    @XStreamOmitField
    private volatile boolean isInstanceInfoDirty = false;
    private volatile LeaseInfo leaseInfo;
    @Auto
    private volatile Boolean isCoordinatingDiscoveryServer = Boolean.FALSE;
    @XStreamAlias("metadata")
    private volatile Map<String, String> metadata = new ConcurrentHashMap<String, String>();
    @Auto
    private volatile Long lastUpdatedTimestamp = System.currentTimeMillis();
    @Auto
    private volatile Long lastDirtyTimestamp = System.currentTimeMillis();
    @Auto
    private volatile ActionType actionType;
    @Auto
    private volatile String asgName;
    private String version = "unknown";

    private InstanceInfo() {
    }

    @JsonCreator
    public InstanceInfo(
            @JsonProperty("app") String appName,
            @JsonProperty("appGroupName") String appGroupName,
            @JsonProperty("ipAddr") String ipAddr,
            @JsonProperty("sid") String sid,
            @JsonProperty("port") int port,
            @JsonProperty("portEnabled") boolean portEnabled,
            @JsonProperty("securePort") int securePort,
            @JsonProperty("securePortEnabled") boolean securePortEnabled,
            @JsonProperty("homePageUrl") String homePageUrl,
            @JsonProperty("statusPageUrl") String statusPageUrl,
            @JsonProperty("healthCheckUrl") String healthCheckUrl,
            @JsonProperty("secureHealthCheckUrl") String secureHealthCheckUrl,
            @JsonProperty("vipAddress") String vipAddress,
            @JsonProperty("secureVipAddress") String secureVipAddress,
            @JsonProperty("countryId") int countryId,
            @JsonProperty("dataCenterInfo") DataCenterInfo dataCenterInfo,
            @JsonProperty("hostName") String hostName,
            @JsonProperty("status") InstanceStatus status,
            @JsonProperty("overriddenstatus") InstanceStatus overriddenstatus,
            @JsonProperty("leaseInfo") LeaseInfo leaseInfo,
            @JsonProperty("isCoordinatingDiscoveryServer") Boolean isCoordinatingDiscoveryServer,
            @JsonProperty("metadata") HashMap<String, String> metadata,
            @JsonProperty("lastUpdatedTimestamp") Long lastUpdatedTimestamp,
            @JsonProperty("lastDirtyTimestamp") Long lastDirtyTimestamp,
            @JsonProperty("actionType") ActionType actionType,
            @JsonProperty("asgName") String asgName) {
        this.appName = StringCache.intern(appName);
        this.appGroupName = StringCache.intern(appGroupName);
        this.ipAddr = ipAddr;
        this.sid = sid;
        this.port = port;
        this.isUnsecurePortEnabled = portEnabled;
        this.securePort = securePort;
        this.isSecurePortEnabled = securePortEnabled;
        this.homePageUrl = homePageUrl;
        this.statusPageUrl = statusPageUrl;
        this.healthCheckUrl = healthCheckUrl;
        this.secureHealthCheckUrl = secureHealthCheckUrl;
        this.vipAddress = StringCache.intern(vipAddress);
        this.secureVipAddress = StringCache.intern(secureVipAddress);
        this.countryId = countryId;
        this.dataCenterInfo = dataCenterInfo;
        this.hostName = hostName;
        this.status = status;
        this.overriddenstatus = overriddenstatus;
        this.leaseInfo = leaseInfo;
        this.isCoordinatingDiscoveryServer = isCoordinatingDiscoveryServer;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.lastDirtyTimestamp = lastDirtyTimestamp;
        this.actionType = actionType;
        this.asgName = StringCache.intern(asgName);

        // for compatibility
        if (metadata == null) {
            this.metadata = Collections.emptyMap();
        } else if (metadata.size() == 1) {
            this.metadata = removeMetadataMapLegacyValues(metadata);
        } else {
            this.metadata = metadata;
        }
    }

    private Map<String, String> removeMetadataMapLegacyValues(Map<String, String> metadata) {
        if (InstanceInfoSerializer.METADATA_COMPATIBILITY_VALUE.equals(metadata.get(InstanceInfoSerializer.METADATA_COMPATIBILITY_KEY))) {
            // TODO this else if can be removed once the server no longer uses legacy json
            metadata.remove(InstanceInfoSerializer.METADATA_COMPATIBILITY_KEY);
        } else if (InstanceInfoSerializer.METADATA_COMPATIBILITY_VALUE.equals(metadata.get("class"))) {
            // TODO this else if can be removed once the server no longer uses legacy xml
            metadata.remove("class");
        }
        return metadata;
    }

    /**
     *
     * shallow copy constructor.
     *
     * @param ii The object to copy
     */
    public InstanceInfo(InstanceInfo ii) {
        this.appName = ii.appName;
        this.appGroupName = ii.appGroupName;
        this.ipAddr = ii.ipAddr;
        this.sid = ii.sid;

        this.port = ii.port;
        this.securePort = ii.securePort;

        this.homePageUrl = ii.homePageUrl;
        this.statusPageUrl = ii.statusPageUrl;
        this.healthCheckUrl = ii.healthCheckUrl;
        this.secureHealthCheckUrl = ii.secureHealthCheckUrl;

        this.vipAddress = ii.vipAddress;
        this.secureVipAddress = ii.secureVipAddress;
        this.statusPageRelativeUrl = ii.statusPageRelativeUrl;
        this.statusPageExplicitUrl = ii.statusPageExplicitUrl;

        this.healthCheckRelativeUrl = ii.healthCheckRelativeUrl;
        this.healthCheckSecureExplicitUrl = ii.healthCheckSecureExplicitUrl;

        this.vipAddressUnresolved = ii.vipAddressUnresolved;
        this.secureVipAddressUnresolved = ii.secureVipAddressUnresolved;

        this.healthCheckExplicitUrl = ii.healthCheckExplicitUrl;

        this.countryId = ii.countryId;
        this.isSecurePortEnabled = ii.isSecurePortEnabled;
        this.isUnsecurePortEnabled = ii.isUnsecurePortEnabled;

        this.dataCenterInfo = ii.dataCenterInfo;

        this.hostName = ii.hostName;

        this.status = ii.status;
        this.overriddenstatus = ii.overriddenstatus;

        this.isInstanceInfoDirty = ii.isInstanceInfoDirty;

        this.leaseInfo = ii.leaseInfo;

        this.isCoordinatingDiscoveryServer = ii.isCoordinatingDiscoveryServer;

        this.metadata = ii.metadata;

        this.lastUpdatedTimestamp = ii.lastUpdatedTimestamp;
        this.lastDirtyTimestamp = ii.lastDirtyTimestamp;

        this.actionType = ii.actionType;

        this.asgName = ii.asgName;

        this.version = ii.version;
    }


    public enum InstanceStatus {
        UP, // Ready to receive traffic
        DOWN, // Do not send traffic- healthcheck callback failed
        STARTING, // Just about starting- initializations to be done - do not
        // send traffic
        OUT_OF_SERVICE, // Intentionally shutdown for traffic
        UNKNOWN;

        public static InstanceStatus toEnum(String s) {
            for (InstanceStatus e : InstanceStatus.values()) {
                if (e.name().equalsIgnoreCase(s)) {
                    return e;
                }
            }
            return UNKNOWN;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InstanceInfo other = (InstanceInfo) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    public enum PortType {
        SECURE, UNSECURE
    }

    public static final class Builder {
        private static final String COLON = ":";
        private static final String HTTPS_PROTOCOL = "https://";
        private static final String HTTP_PROTOCOL = "http://";

        @XStreamOmitField
        private InstanceInfo result;

        private String namespace;

        private Builder() {
            result = new InstanceInfo();
        }

        public Builder(InstanceInfo instanceInfo) {
            result = instanceInfo;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Set the application name of the instance.This is mostly used in
         * querying of instances.
         *
         * @param appName
         *            the application name.
         * @return the instance info builder.
         */
        public Builder setAppName(String appName) {
            result.appName = StringCache.intern(appName.toUpperCase(Locale.ROOT));
            return this;
        }

        public Builder setAppGroupName(String appGroupName) {
            if (appGroupName != null) {
                result.appGroupName = appGroupName.toUpperCase(Locale.ROOT);
            } else {
                result.appGroupName = null;
            }
            return this;
        }

        /**
         * Sets the fully qualified hostname of this running instance.This is
         * mostly used in constructing the {@link java.net.URL} for communicating with
         * the instance.
         *
         * @param hostName
         *            the host name of the instance.
         * @return the {@link InstanceInfo} builder.
         */
        public Builder setHostName(String hostName) {
            String existingHostName = result.hostName;
            result.hostName = hostName;
            if ((existingHostName != null)
                    && !(hostName.equals(existingHostName))) {
                refreshStatusPageUrl().refreshHealthCheckUrl()
                        .refreshVIPAddress().refreshSecureVIPAddress();
            }
            return this;
        }

        /**
         * Sets the status of the instances.If the status is UP, that is when
         * the instance is ready to service requests.
         *
         * @param status
         *            the {@link InstanceStatus} of the instance.
         * @return the {@link InstanceInfo} builder.
         */
        public Builder setStatus(InstanceStatus status) {
            result.status = status;
            return this;
        }

        /**
         * Sets the status overridden by some other external process.This is
         * mostly used in putting an instance out of service to block traffic to
         * it.
         *
         * @param status
         *            the overridden {@link InstanceStatus} of the instance.
         * @return @return the {@link InstanceInfo} builder.
         */
        public Builder setOverriddenStatus(InstanceStatus status) {
            result.overriddenstatus = status;
            return this;
        }

        /**
         * Sets the ip address of this running instance.
         *
         * @param ip
         *            the ip address of the instance.
         * @return the {@link InstanceInfo} builder.
         */
        public Builder setIPAddr(String ip) {
            result.ipAddr = ip;
            return this;
        }

        /**
         * Sets the identity of this application instance.
         *
         * @param sid
         *            the sid of the instance.
         * @return the {@link InstanceInfo} builder.
         */
        @Deprecated
        public Builder setSID(String sid) {
            result.sid = sid;
            return this;
        }

        /**
         * Sets the port number that is used to service requests.
         *
         * @param port
         *            the port number that is used to service requests.
         * @return the {@link InstanceInfo} builder.
         */
        public Builder setPort(int port) {
            result.port = port;
            return this;
        }

        /**
         * Sets the secure port that is used to service requests.
         *
         * @param port
         *            the secure port that is used to service requests.
         * @return the {@link InstanceInfo} builder.
         */
        public Builder setSecurePort(int port) {
            result.securePort = port;
            return this;
        }

        /**
         * Enabled or disable secure/non-secure ports .
         *
         * @param type
         *            Secure or Non-Secure.
         * @param isEnabled
         *            true if enabled.
         * @return the instance builder.
         */
        public Builder enablePort(PortType type, boolean isEnabled) {
            if (type == PortType.SECURE) {
                result.isSecurePortEnabled = isEnabled;
            } else {
                result.isUnsecurePortEnabled = isEnabled;
            }
            return this;
        }

        @Deprecated
        public Builder setCountryId(int id) {
            result.countryId = id;
            return this;
        }

        /**
         * Sets the absolute home page {@link java.net.URL} for this instance. The users
         * can provide the <code>homePageUrlPath</code> if the home page resides
         * in the same instance talking to discovery, else in the cases where
         * the instance is a proxy for some other server, it can provide the
         * full {@link java.net.URL}. If the full {@link java.net.URL} is provided it takes
         * precedence.
         *
         * <p>
         * The full {@link java.net.URL} should follow the format
         * http://${netflix.appinfo.hostname}:7001/ where the value
         * ${netflix.appinfo.hostname} is replaced at runtime.
         * </p>
         *
         * @param relativeUrl
         *            the relative url path of the home page.
         *
         * @param explicitUrl
         *            - The full {@link java.net.URL} for the home page
         * @return the instance builder.
         */
        public Builder setHomePageUrl(String relativeUrl, String explicitUrl) {
            String hostNameInterpolationExpression = "${" + namespace + "hostname}";
            if (explicitUrl != null) {
                result.homePageUrl = explicitUrl.replace(
                        hostNameInterpolationExpression, result.hostName);
            } else if (relativeUrl != null) {
                result.homePageUrl = HTTP_PROTOCOL + result.hostName + COLON
                        + result.port + relativeUrl;
            }
            return this;
        }

        /**
         * {@link #setHomePageUrl(String, String)} has complex logic that should not be invoked when
         * we deserialize {@link InstanceInfo} object, or home page URL is formatted already by the client.
         */
        public Builder setHomePageUrlForDeser(String homePageUrl) {
            result.homePageUrl = homePageUrl;
            return this;
        }

        /**
         * Sets the absolute status page {@link java.net.URL} for this instance. The
         * users can provide the <code>statusPageUrlPath</code> if the status
         * page resides in the same instance talking to discovery, else in the
         * cases where the instance is a proxy for some other server, it can
         * provide the full {@link java.net.URL}. If the full {@link java.net.URL} is provided it
         * takes precedence.
         *
         * <p>
         * The full {@link java.net.URL} should follow the format
         * http://${netflix.appinfo.hostname}:7001/Status where the value
         * ${netflix.appinfo.hostname} is replaced at runtime.
         * </p>
         *
         * @param relativeUrl
         *            - The {@link java.net.URL} path for status page for this instance
         * @param explicitUrl
         *            - The full {@link java.net.URL} for the status page
         * @return - Builder instance
         */
        public Builder setStatusPageUrl(String relativeUrl, String explicitUrl) {
            String hostNameInterpolationExpression = "${" + namespace + "hostname}";
            result.statusPageRelativeUrl = relativeUrl;
            result.statusPageExplicitUrl = explicitUrl;
            if (explicitUrl != null) {
                result.statusPageUrl = explicitUrl.replace(
                        hostNameInterpolationExpression, result.hostName);
            } else if (relativeUrl != null) {
                result.statusPageUrl = HTTP_PROTOCOL + result.hostName + COLON
                        + result.port + relativeUrl;
            }
            return this;
        }

        /**
         * {@link #setStatusPageUrl(String, String)} has complex logic that should not be invoked when
         * we deserialize {@link InstanceInfo} object, or status page URL is formatted already by the client.
         */
        public Builder setStatusPageUrlForDeser(String statusPageUrl) {
            result.statusPageUrl = statusPageUrl;
            return this;
        }

        /**
         * Sets the absolute health check {@link java.net.URL} for this instance for both
         * secure and non-secure communication The users can provide the
         * <code>healthCheckUrlPath</code> if the healthcheck page resides in
         * the same instance talking to discovery, else in the cases where the
         * instance is a proxy for some other server, it can provide the full
         * {@link java.net.URL}. If the full {@link java.net.URL} is provided it takes precedence.
         *
         * <p>
         * The full {@link java.net.URL} should follow the format
         * http://${netflix.appinfo.hostname}:7001/healthcheck where the value
         * ${netflix.appinfo.hostname} is replaced at runtime.
         * </p>
         *
         * @param relativeUrl
         *            - The {@link java.net.URL} path for healthcheck page for this
         *            instance.
         * @param explicitUrl
         *            - The full {@link java.net.URL} for the healthcheck page.
         * @param secureExplicitUrl
         *            the full secure explicit url of the healthcheck page.
         * @return the instance builder
         */
        public Builder setHealthCheckUrls(String relativeUrl,
                                          String explicitUrl, String secureExplicitUrl) {
            String hostNameInterpolationExpression = "${" + namespace + "hostname}";
            result.healthCheckRelativeUrl = relativeUrl;
            result.healthCheckExplicitUrl = explicitUrl;
            result.healthCheckSecureExplicitUrl = secureExplicitUrl;
            if (explicitUrl != null) {
                result.healthCheckUrl = explicitUrl.replace(
                        hostNameInterpolationExpression, result.hostName);
            } else if (result.isUnsecurePortEnabled) {
                result.healthCheckUrl = HTTP_PROTOCOL + result.hostName + COLON
                        + result.port + relativeUrl;
            }

            if (secureExplicitUrl != null) {
                result.secureHealthCheckUrl = secureExplicitUrl.replace(
                        hostNameInterpolationExpression, result.hostName);
            } else if (result.isSecurePortEnabled) {
                result.secureHealthCheckUrl = HTTPS_PROTOCOL + result.hostName
                        + COLON + result.securePort + relativeUrl;
            }
            return this;
        }

        /**
         * {@link #setHealthCheckUrls(String, String, String)} has complex logic that should not be invoked when
         * we deserialize {@link InstanceInfo} object, or health check URLs are formatted already by the client.
         */
        public Builder setHealthCheckUrlsForDeser(String healthCheckUrl, String secureHealthCheckUrl) {
            if (healthCheckUrl != null) {
                result.healthCheckUrl = healthCheckUrl;
            }
            if (secureHealthCheckUrl != null) {
                result.secureHealthCheckUrl = secureHealthCheckUrl;
            }
            return this;
        }

        /**
         * Sets the Virtual Internet Protocol address for this instance. The
         * address should follow the format <code><hostname:port></code> This
         * address needs to be resolved into a real address for communicating
         * with this instance.
         *
         * @param vipAddress
         *            - The Virtual Internet Protocol address of this instance.
         * @return the instance builder.
         */
        public Builder setVIPAddress(String vipAddress) {
            result.vipAddressUnresolved = StringCache.intern(vipAddress);
            result.vipAddress = StringCache.intern(resolveDeploymentContextBasedVipAddresses(vipAddress));
            return this;
        }

        /**
         * Setter used during deserialization process, that does not do macro expansion on the provided value.
         */
        public Builder setVIPAddressDeser(String vipAddress) {
            result.vipAddress = StringCache.intern(vipAddress);
            return this;
        }

        /**
         * Sets the Secure Virtual Internet Protocol address for this instance.
         * The address should follow the format <hostname:port> This address
         * needs to be resolved into a real address for communicating with this
         * instance.
         *
         * @param secureVIPAddress
         *            the secure VIP address of the instance.
         *
         * @return - Builder instance
         */
        public Builder setSecureVIPAddress(String secureVIPAddress) {
            result.secureVipAddressUnresolved = StringCache.intern(secureVIPAddress);
            result.secureVipAddress = StringCache.intern(resolveDeploymentContextBasedVipAddresses(secureVIPAddress));
            return this;
        }

        /**
         * Setter used during deserialization process, that does not do macro expansion on the provided value.
         */
        public Builder setSecureVIPAddressDeser(String secureVIPAddress) {
            result.secureVipAddress = StringCache.intern(secureVIPAddress);
            return this;
        }

        /**
         * Sets the datacenter information.
         *
         * @param datacenter
         *            the datacenter information for where this instance is
         *            running.
         * @return the {@link InstanceInfo} builder.
         */
        public Builder setDataCenterInfo(DataCenterInfo datacenter) {
            result.dataCenterInfo = datacenter;
            return this;
        }

        /**
         * Set the instance lease information.
         *
         * @param info
         *            the lease information for this instance.
         */
        public Builder setLeaseInfo(LeaseInfo info) {
            result.leaseInfo = info;
            return this;
        }

        /**
         * Add arbitrary metadata to running instance.
         *
         * @param key
         *            The key of the metadata.
         * @param val
         *            The value of the metadata.
         * @return the {@link InstanceInfo} builder.
         */
        public Builder add(String key, String val) {
            result.metadata.put(key, val);
            return this;
        }

        /**
         * Replace the existing metadata map with a new one.
         *
         * @param mt
         *            the new metadata name.
         * @return instance info builder.
         */
        public Builder setMetadata(Map<String, String> mt) {
            result.metadata = mt;
            return this;
        }

        /**
         * Returns the encapsulated instance info even it it is not built fully.
         *
         * @return the existing information about the instance.
         */
        public InstanceInfo getRawInstance() {
            return result;
        }

        /**
         * Build the {@link InstanceInfo} object.
         *
         * @return the {@link InstanceInfo} that was built based on the
         *         information supplied.
         */
        public InstanceInfo build() {
            if (!isInitialized()) {
                throw new IllegalStateException("name is required!");
            }
            return result;
        }

        public boolean isInitialized() {
            return (result.appName != null);
        }

        /**
         * Sets the AWS ASG name for this instance.
         *
         * @param asgName
         *            the asg name for this instance.
         * @return the instance info builder.
         */
        public Builder setASGName(String asgName) {
            result.asgName = StringCache.intern(asgName);
            return this;
        }

        private Builder refreshStatusPageUrl() {
            setStatusPageUrl(result.statusPageRelativeUrl,
                    result.statusPageExplicitUrl);
            return this;
        }

        private Builder refreshHealthCheckUrl() {
            setHealthCheckUrls(result.healthCheckRelativeUrl,
                    result.healthCheckExplicitUrl,
                    result.healthCheckSecureExplicitUrl);
            return this;
        }

        private Builder refreshSecureVIPAddress() {
            setSecureVIPAddress(result.secureVipAddressUnresolved);
            return this;
        }

        private Builder refreshVIPAddress() {
            setVIPAddress(result.vipAddressUnresolved);
            return this;
        }

        public Builder setIsCoordinatingDiscoveryServer(boolean isCoordinatingDiscoveryServer) {
            result.isCoordinatingDiscoveryServer = isCoordinatingDiscoveryServer;
            return this;
        }

        public Builder setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
            result.lastUpdatedTimestamp = lastUpdatedTimestamp;
            return this;
        }

        public Builder setLastDirtyTimestamp(long lastDirtyTimestamp) {
            result.lastDirtyTimestamp = lastDirtyTimestamp;
            return this;
        }

        public Builder setActionType(ActionType actionType) {
            result.actionType = actionType;
            return this;
        }

        public Builder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }
    }

    /**
     * Return the name of the application registering with discovery.
     *
     * @return the string denoting the application name.
     */
    @JsonProperty("app")
    public String getAppName() {
        return appName;
    }

    public String getAppGroupName() {
        return appGroupName;
    }

    /**
     * Returns the fully qualified hostname of this running instance.
     *
     * @return the hostname.
     */
    public String getHostName() {
        return hostName;
    }

    @Deprecated
    public void setSID(String sid) {
        this.sid = sid;
        setIsDirty();
    }

    @Deprecated
    @JsonIgnore
    public String getSID() {
        return sid;
    }

    /**
     *
     * Returns the unique id of the instance.
     *
     * @return the unique id.
     */
    @JsonIgnore
    public String getId() {
        if (dataCenterInfo instanceof UniqueIdentifier) {
            return ((UniqueIdentifier) dataCenterInfo).getId();
        } else {
            return hostName;
        }
    }

    /**
     * Returns the ip address of the instance.
     *
     * @return - the ip address, in AWS scenario it is a private IP.
     */
    @JsonProperty("ipAddr")
    public String getIPAddr() {
        return ipAddr;
    }

    /**
     *
     * Returns the port number that is used for servicing requests.
     *
     * @return - the non-secure port number.
     */
    @JsonIgnore
    public int getPort() {
        return port;
    }

    /**
     * Returns the status of the instance.
     *
     * @return the status indicating whether the instance can handle requests.
     */
    public InstanceStatus getStatus() {
        return status;
    }

    /**
     * Returns the overridden status if any of the instance.
     *
     * @return the status indicating whether an external process has changed the
     *         status.
     */
    public InstanceStatus getOverriddenStatus() {
        return overriddenstatus;
    }

    /**
     * Returns data center information identifying if it is AWS or not.
     *
     * @return the data center information.
     */
    public DataCenterInfo getDataCenterInfo() {
        return dataCenterInfo;
    }

    /**
     * Returns the lease information regarding when it expires.
     *
     * @return the lease information of this instance.
     */
    public LeaseInfo getLeaseInfo() {
        return leaseInfo;
    }

    /**
     * Sets the lease information regarding when it expires.
     *
     * @param info
     *            the lease information of this instance.
     */
    public void setLeaseInfo(LeaseInfo info) {
        leaseInfo = info;
    }

    /**
     * Returns all application specific metadata set on the instance.
     *
     * @return application specific metadata.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Deprecated
    public int getCountryId() {
        return countryId;
    }

    /**
     * Returns the secure port that is used for servicing requests.
     *
     * @return the secure port.
     */
    @JsonIgnore
    public int getSecurePort() {
        return securePort;
    }

    /**
     * Checks whether a port is enabled for traffic or not.
     *
     * @param type
     *            indicates whether it is secure or non-secure port.
     * @return true if the port is enabled, false otherwise.
     */
    @JsonIgnore
    public boolean isPortEnabled(PortType type) {
        if (type == PortType.SECURE) {
            return isSecurePortEnabled;
        } else {
            return isUnsecurePortEnabled;
        }
    }

    /**
     * Returns the time elapsed since epoch since the instance status has been
     * last updated.
     *
     * @return the time elapsed since epoch since the instance has been last
     *         updated.
     */
    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    /**
     * Set the update time for this instance when the status was update.
     */
    public void setLastUpdatedTimestamp() {
        this.lastUpdatedTimestamp = System.currentTimeMillis();
    }

    /**
     * Gets the home page {@link java.net.URL} set for this instance.
     *
     * @return home page {@link java.net.URL}
     */
    public String getHomePageUrl() {
        return homePageUrl;
    }

    /**
     * Gets the status page {@link java.net.URL} set for this instance.
     *
     * @return status page {@link java.net.URL}
     */
    public String getStatusPageUrl() {
        return statusPageUrl;
    }

    /**
     * Gets the absolute URLs for the health check page for both secure and
     * non-secure protocols. If the port is not enabled then the URL is
     * excluded.
     *
     * @return A Set containing the string representation of health check urls
     *         for secure and non secure protocols
     */
    @JsonIgnore
    public Set<String> getHealthCheckUrls() {
        Set<String> healthCheckUrlSet = new LinkedHashSet<String>();
        if (this.isUnsecurePortEnabled && healthCheckUrl != null && !healthCheckUrl.isEmpty()) {
            healthCheckUrlSet.add(healthCheckUrl);
        }
        if (this.isSecurePortEnabled && secureHealthCheckUrl != null && !secureHealthCheckUrl.isEmpty()) {
            healthCheckUrlSet.add(secureHealthCheckUrl);
        }
        return healthCheckUrlSet;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public String getSecureHealthCheckUrl() {
        return secureHealthCheckUrl;
    }

    /**
     * Gets the Virtual Internet Protocol address for this instance. Defaults to
     * hostname if not specified.
     *
     * @return - The Virtual Internet Protocol address
     */
    @JsonProperty("vipAddress")
    public String getVIPAddress() {
        return vipAddress;
    }

    /**
     * Get the Secure Virtual Internet Protocol address for this instance.
     * Defaults to hostname if not specified.
     *
     * @return - The Secure Virtual Internet Protocol address.
     */
    public String getSecureVipAddress() {
        return secureVipAddress;
    }

    /**
     * Gets the last time stamp when this instance was touched.
     *
     * @return last timestamp when this instance was touched.
     */
    public Long getLastDirtyTimestamp() {
        return lastDirtyTimestamp;
    }

    /**
     * Set the time indicating that the instance was touched.
     *
     * @param lastDirtyTimestamp
     *            time when the instance was touched.
     */
    public void setLastDirtyTimestamp(Long lastDirtyTimestamp) {
        this.lastDirtyTimestamp = lastDirtyTimestamp;
    }

    /**
     * Set the status for this instance.
     *
     * @param status status for this instance.
     * @return the prev status if a different status from the current was set, null otherwise
     */
    public synchronized InstanceStatus setStatus(InstanceStatus status) {
        if (this.status != status) {
            InstanceStatus prev = this.status;
            this.status = status;
            setIsDirty();
            return prev;
        }
        return null;
    }

    /**
     * Set the status for this instance without updating the dirty timestamp.
     *
     * @param status
     *            status for this instance.
     */
    public synchronized void setStatusWithoutDirty(InstanceStatus status) {
        if (this.status != status) {
            this.status = status;
        }
    }

    /**
     * Sets the overridden status for this instance.Normally set by an external
     * process to disable instance from taking traffic.
     *
     * @param status
     *            overridden status for this instance.
     */
    public synchronized void setOverriddenStatus(InstanceStatus status) {
        if (this.overriddenstatus != status) {
            this.overriddenstatus = status;
        }
    }

    /**
     * Returns whether any state changed so that {@link com.netflix.discovery.EurekaClient} can
     * check whether to retransmit info or not on the next heartbeat.
     *
     * @return true if the {@link InstanceInfo} is dirty, false otherwise.
     */
    @JsonIgnore
    public boolean isDirty() {
        return isInstanceInfoDirty;
    }

    /**
     * @return the lastDirtyTimestamp if is dirty, null otherwise.
     */
    public synchronized Long isDirtyWithTime() {
        if (isInstanceInfoDirty) {
            return lastDirtyTimestamp;
        } else {
            return null;
        }
    }

    /**
     * @deprecated use {@link #setIsDirty()} and {@link #unsetIsDirty(long)} to set and unset
     *
     * Sets the dirty flag so that the instance information can be carried to
     * the discovery server on the next heartbeat.
     *
     * @param isDirty true if dirty, false otherwise.
     */
    @Deprecated
    public synchronized void setIsDirty(boolean isDirty) {
        if (isDirty) {
            setIsDirty();
        } else {
            isInstanceInfoDirty = false;
            // else don't update lastDirtyTimestamp as we are setting isDirty to false
        }
    }

    /**
     * Sets the dirty flag so that the instance information can be carried to
     * the discovery server on the next heartbeat.
     */
    public synchronized void setIsDirty() {
        isInstanceInfoDirty = true;
        lastDirtyTimestamp = System.currentTimeMillis();
    }


    /**
     * Unset the dirty flag iff the unsetDirtyTimestamp matches the lastDirtyTimestamp. No-op if
     * lastDirtyTimestamp > unsetDirtyTimestamp
     *
     * @param unsetDirtyTimestamp the expected lastDirtyTimestamp to unset.
     */
    public synchronized void unsetIsDirty(long unsetDirtyTimestamp) {
        if (lastDirtyTimestamp <= unsetDirtyTimestamp) {
            isInstanceInfoDirty = false;
        } else {
        }
    }

    /**
     * Sets a flag if this instance is the same as the discovery server that is
     * return the instances. This flag is used by the discovery clients to
     * identity the discovery server which is coordinating/returning the
     * information.
     */
    public void setIsCoordinatingDiscoveryServer() {
        String instanceId = getId();
        if ((instanceId != null)
                && (instanceId.equals(ApplicationInfoManager.getInstance()
                .getInfo().getId()))) {
            isCoordinatingDiscoveryServer = Boolean.TRUE;
        } else {
            isCoordinatingDiscoveryServer = Boolean.FALSE;
        }
    }

    /**
     * Finds if this instance is the coordinating discovery server.
     *
     * @return - true, if this instance is the coordinating discovery server,
     *         false otherwise.
     */
    @JsonProperty("isCoordinatingDiscoveryServer")
    public Boolean isCoordinatingDiscoveryServer() {
        return isCoordinatingDiscoveryServer;
    }

    /**
     * Returns the type of action done on the instance in the server.Primarily
     * used for updating deltas in the {@link com.netflix.discovery.EurekaClient}
     * instance.
     *
     * @return action type done on the instance.
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Set the action type performed on this instance in the server.
     *
     * @param actionType
     *            action type done on the instance.
     */
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * Get AWS autoscaling group name if any.
     *
     * @return autoscaling group name of this instance.
     */
    @JsonProperty("asgName")
    public String getASGName() {
        return this.asgName;
    }

    /**
     * Returns the specification version of this application.
     *
     * @return the string indicating the version of the application.
     */
    @Deprecated
    @JsonIgnore
    public String getVersion() {
        return version;
    }

    public enum ActionType {
        ADDED, // Added in the discovery server
        MODIFIED, // Changed in the discovery server
        DELETED
        // Deleted from the discovery server
    }

    /**
     * Register application specific metadata to be sent to the discovery
     * server.
     *
     * @param runtimeMetadata
     *            Map containing key/value pairs.
     */
    synchronized void registerRuntimeMetadata(
            Map<String, String> runtimeMetadata) {
        metadata.putAll(runtimeMetadata);
        setIsDirty();
    }

    /**
     * Convert <code>VIPAddress</code> by substituting environment variables if
     * necessary.
     *
     * @param vipAddressMacro
     *            the macro for which the interpolation needs to be made.
     * @return a string representing the final <code>VIPAddress</code> after
     *         substitution.
     */
    private static String resolveDeploymentContextBasedVipAddresses(
            String vipAddressMacro) {
        String result = vipAddressMacro;

        if (vipAddressMacro == null) {
            return null;
        }

        Matcher matcher = VIP_ATTRIBUTES_PATTERN.matcher(result);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = DynamicPropertyFactory.getInstance()
                    .getStringProperty(key, "").get();

            logger.debug("att:" + matcher.group());
            logger.debug(", att key:" + key);
            logger.debug(", att value:" + value);
            logger.debug("");
            result = result.replaceAll("\\$\\{" + key + "\\}", value);
            matcher = VIP_ATTRIBUTES_PATTERN.matcher(result);
        }

        return result;
    }

    /**
     * Get the zone that a particular instance is in.
     * Note that for AWS deployments, myInfo should contain AWS dataCenterInfo which should contain
     * the AWS zone of the instance, and availZones is ignored.
     *
     * @param availZones the list of available zones for non-AWS deployments
     * @param myInfo
     *            - The InstanceInfo object of the instance.
     * @return - The zone in which the particular instance belongs to.
     */
    public static String getZone(String[] availZones, InstanceInfo myInfo) {
        String instanceZone = ((availZones == null || availZones.length == 0) ? "default"
                : availZones[0]);
        if (myInfo != null
                && myInfo.getDataCenterInfo().getName() == DataCenterInfo.Name.Amazon) {

            String awsInstanceZone = ((AmazonInfo) myInfo.getDataCenterInfo())
                    .get(AmazonInfo.MetaDataKey.availabilityZone);
            if (awsInstanceZone != null) {
                instanceZone = awsInstanceZone;
            }

        }
        return instanceZone;
    }
}
