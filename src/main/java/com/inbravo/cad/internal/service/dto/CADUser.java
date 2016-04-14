package com.inbravo.cad.internal.service.dto;

import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.axis.client.Stub;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADUser implements BasicObject {

  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_HTTP_PORT = 80;

  private static final int DEFAULT_HTTPS_PORT = 443;

  private String name;

  private String tenantName;

  private String crmUserid;

  private String crmPassword;

  private String crmSessionId;

  private String crmOrgName;

  private String crmName;

  private String crmURL;

  private String crmServiceURL;

  private String crmServiceProtocol;

  private int crmPort = 0;

  private String crmAPIToken;

  private String crmBrandingURL;

  private String crmProperties;

  private Boolean crmPopEnabled;

  /* This is a NetSuite CRM specific property */
  private String crmAccountId;

  /* SOAP stub for SFDC/NS */
  private Stub soapStub;

  private String[] crmSecurityToken;

  private Map<String, String> additionalInfo;

  public final String getCrmName() {
    return crmName;
  }

  public final void setCrmName(final String crmName) {
    this.crmName = crmName;
  }

  public final String getCrmURL() {
    return crmURL;
  }

  public final void setCrmURL(final String crmURL) {
    this.crmURL = crmURL;
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String name) {
    this.name = name;
  }

  public final void setTenantName(final String tenantName) {
    this.tenantName = tenantName;
  }

  public final String getTenantName() {
    return tenantName;
  }

  public final String getCrmUserid() {
    return crmUserid;
  }

  public final void setCrmUserid(final String crmUserid) {
    this.crmUserid = crmUserid;
  }

  public final String getCrmPassword() {
    return crmPassword;
  }

  public final void setCrmPassword(final String crmPassword) {
    this.crmPassword = crmPassword;
  }

  public final String getCrmSessionId() {
    return crmSessionId;
  }

  public final void setCrmSessionId(final String crmSessionId) {
    this.crmSessionId = crmSessionId;
  }

  public final Boolean getCrmPopEnabled() {
    return crmPopEnabled;
  }

  public final void setCrmPopEnabled(final Boolean crmPopEnabled) {
    this.crmPopEnabled = crmPopEnabled;
  }

  public final String getCrmOrgName() {
    return crmOrgName;
  }

  public final void setCrmOrgName(final String crmOrgName) {
    this.crmOrgName = crmOrgName;
  }

  public final String getCrmServiceURL() {
    return crmServiceURL;
  }

  public final void setCrmServiceURL(final String crmServiceURL) {
    this.crmServiceURL = crmServiceURL;
  }

  public final String getCrmBrandingURL() {
    return crmBrandingURL;
  }

  public final void setCrmBrandingURL(final String crmBrandingURL) {
    this.crmBrandingURL = crmBrandingURL;
  }

  public final String getCrmAPIToken() {
    return crmAPIToken;
  }

  public final void setCrmAPIToken(final String crmAPIToken) {
    this.crmAPIToken = crmAPIToken;
  }

  public final String getCrmServiceProtocol() {
    return crmServiceProtocol;
  }

  public final void setCrmServiceProtocol(final String crmServiceProtocol) {
    this.crmServiceProtocol = crmServiceProtocol;
  }

  public final int getCrmPort() {
    return crmPort;
  }

  public final void setCrmPort(final int crmPort) {
    this.crmPort = crmPort;
  }

  public final String getCrmProperties() {
    return crmProperties;
  }

  public final String getCrmAccountId() {
    return crmAccountId;
  }

  public final void setCrmAccountId(final String crmAccountId) {
    this.crmAccountId = crmAccountId;
  }

  public final Stub getSoapStub() {
    return soapStub;
  }

  public final void setSoapStub(final Stub soapStub) {
    this.soapStub = soapStub;
  }

  public final void setCrmProperties(final String crmProperties) {
    this.crmProperties = crmProperties;

    if (crmProperties != null) {
      /* Tokenize the properties */
      final StringTokenizer stringTokenizer = new StringTokenizer(crmProperties, "||", false);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {
        final String tempToken = stringTokenizer.nextToken();

        /* Get service URL */
        if (tempToken.trim().contains("serviceURL") && tempToken.contains("==")) {
          if (tempToken.split("==").length == 2) {
            /* Trim the URL */
            this.setCrmServiceURL(tempToken.split("==")[1].trim());
          }
        } else /* Get service protocol */
        if (tempToken.trim().contains("protocol") && tempToken.contains("==")) {
          if (tempToken.split("==").length == 2) {
            /* Trim the protocol */
            this.setCrmServiceProtocol(tempToken.split("==")[1].trim());
          }
        } else /* Get alias URL */
        if (tempToken.trim().contains("brandingURL") && tempToken.contains("==")) {
          if (tempToken.split("==").length == 2) {
            /* Trim the URL */
            this.setCrmBrandingURL(tempToken.split("==")[1].trim());
          }
        } else /* Get API Token */
        if (tempToken.trim().contains("apiToken") && tempToken.contains("==")) {
          if (tempToken.split("==").length == 2) {
            /* Trim the api token */
            this.setCrmAPIToken(tempToken.split("==")[1].trim());
          }
        } else /* Get port */
        if (tempToken.trim().contains("port") && tempToken.contains("==")) {
          if (tempToken.split("==").length == 2) {
            /* Trim the api token */
            this.setCrmPort(Integer.parseInt(tempToken.split("==")[1].trim()));
          }
        }
      }

      // to allow protocol to be overriden by the crmServiceURL
      if (this.crmServiceURL != null) {
        this.crmServiceURL = this.crmServiceURL.trim();
        if (this.crmServiceURL.matches("^http[s]*://.*$")) {
          // take the protocol out
          this.crmServiceProtocol = this.crmServiceURL.replaceAll("(^http[s]*)://.*$", "$1");
          this.crmServiceURL = this.crmServiceURL.replaceAll("^http[s]*://(.*)$", "$1");
        }
      }
      if (this.crmPort == 0) {
        // setup default port
        this.crmPort = DEFAULT_HTTP_PORT;
        if (this.crmServiceProtocol != null && this.crmServiceProtocol.equalsIgnoreCase("https")) {
          this.crmPort = DEFAULT_HTTPS_PORT;
        }
      }
    }
  }

  /**
   * To determine if user is using token. If token is used for this user,
   * 
   * @return userid/token if token exists. Otherwise, return original userId.
   */
  public final String getCrmAPIUserId() {
    if (crmAPIToken == null || crmAPIToken.trim().equals("")) {
      return crmUserid;
    } else {
      return crmUserid += "/token";
    }
  }

  /**
   * Get api password.
   * 
   * @return original password if token doesn't exist. Otherwise, return token.
   */
  public final String getCrmAPIPassword() {

    /* Verify if token is not null of emptry string */
    if (crmAPIToken == null || "".equals(crmAPIToken)) {
      return crmPassword;
    }
    return crmAPIToken;
  }

  public final Map<String, String> getAdditionalInfo() {
    return additionalInfo;
  }

  public final void setAdditionalInfo(final Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  /**
   * @return the crmSecurityToken
   */
  public final String[] getCrmSecurityToken() {
    return this.crmSecurityToken;
  }

  /**
   * @param crmSecurityToken the crmSecurityToken to set
   */
  public final void setCrmSecurityToken(final String[] crmSecurityToken) throws Exception {

    if (crmSecurityToken != null) {
      this.crmSecurityToken = crmSecurityToken.clone();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Agent [name=" + this.name + ", tenantName=" + this.tenantName + ", crmUserid=" + this.crmUserid + ", crmPassword=" + this.crmPassword
        + ", crmSessionId=" + this.crmSessionId + ", crmOrgName=" + this.crmOrgName + ", crmName=" + this.crmName + ", crmURL=" + this.crmURL
        + ", crmServiceURL=" + this.crmServiceURL + ", crmServiceProtocol=" + this.crmServiceProtocol + ", crmPort=" + this.crmPort
        + ", crmAPIToken=" + this.crmAPIToken + ", crmBrandingURL=" + this.crmBrandingURL + ", crmProperties=" + this.crmProperties
        + ", crmPopEnabled=" + this.crmPopEnabled + ", crmAccountId=" + this.crmAccountId + ", soapStub=" + this.soapStub + ", crmSecurityToken="
        + Arrays.toString(this.crmSecurityToken) + ", additionalInfo=" + this.additionalInfo + "]";
  }
}
