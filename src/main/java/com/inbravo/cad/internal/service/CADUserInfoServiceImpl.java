package com.inbravo.cad.internal.service;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.rest.constants.CRMConstants;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADUserInfoServiceImpl implements CADUserInfoService {

  private final Logger logger = Logger.getLogger(CADUserInfoServiceImpl.class.getName());

  private String splitString;


  public CADUserInfoServiceImpl(final String splitString) {
    this.splitString = splitString;
  }

  @Override
  public final CADUser getInfo(final String agentId) throws Exception {
    logger.debug("---Inside getInfo: agent: " + agentId);

    /* Status of agent */
    boolean agentFound = false;

    /* Create new agent */
    CADUser agent = null;

    if (agentId != null) {

      /* Split the string to get tenant/agent info */
      final String[] idInfo = agentId.split(splitString);

      /* Create LDAP search string */
      String screenPopProperty = "uid=" + agentId.trim() + ",wpj-tenant-obj-id=" + idInfo[0].trim() + ",o=whitepj.net";

      /* Create directry search control */
      final SearchControls searchControls = new SearchControls();
      searchControls.setReturningObjFlag(true);
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      NamingEnumeration<?> answer = null;

      try {
        try {

          /* Step 1: Get Agent information */
          answer = lDAPService.search(screenPopProperty, "(objectclass=wpj-agent)", searchControls, true);

        } catch (final NameNotFoundException nameNotFoundException) {

          /* log this error */
          logger.debug("---Inside getInfo: Node is not found at LDAP: " + screenPopProperty);

          /* Inform user about missing LDAP property */
          throw new CADException(CADResponseCodes._1009 + "Agent information is not found ", nameNotFoundException);
        }

        /* If records found */
        if (answer != null && answer.hasMoreElements()) {

          while (answer.hasMoreElements()) {

            final SearchResult searchResult = (SearchResult) answer.next();

            /* Instantiate the agent object */
            agent = new CADUser();

            /* Set name of agent */
            agent.setName(idInfo[1]);

            /* Agent is found */
            agentFound = true;

            logger.debug("---Inside getInfo: agent: '" + agentId + "' is found");

            /* Set tenant name */
            agent.setTenantName(idInfo[0].trim());

            /* Check for external login id */
            if (searchResult.getAttributes().get("wpj-agent-ext-login") != null) {

              /* Set CRM login id */
              agent.setCrmUserid((String) searchResult.getAttributes().get("wpj-agent-ext-login").get());
            } else {

              logger.debug("---Inside getInfo CRM credentials not found: user id");
            }

            /* Check for Sales Force password */
            if (searchResult.getAttributes().get("wpj-agent-ext-password") != null) {

              /* Set CRM password */
              agent.setCrmPassword((String) searchResult.getAttributes().get("wpj-agent-ext-password").get());
            } else {

              logger.debug("---Inside getInfo CRM credentials not found: password");
            }
          }
        } else {

          /* Inform user about missing property */
          throw new CADException(CADResponseCodes._1009 + "Agent information is not found");
        }

        /* If agent is found then only go for CRM information */
        if (agentFound) {

          try {

            /* Check if any external CRM is enabled */
            screenPopProperty = "cnt-screenpop-id=1,wpj-tenant-obj-id=" + idInfo[0].trim() + ",o=whitepj.net";

            /* Get Tenant information */
            answer = lDAPService.search(screenPopProperty, "(objectclass=cnt-screenpop)", searchControls, true);
          } catch (final NameNotFoundException nameNotFoundException) {

            /* Log this error */
            logger.debug("---Inside getInfo: Node is not found at LDAP: " + screenPopProperty);

            /* Inform user about missing LDAP property */
            throw new CADException(CADResponseCodes._1009 + "External CRM information for Agent is not found ", nameNotFoundException);
          }

          if (answer != null && answer.hasMoreElements()) {

            while (answer.hasMoreElements()) {

              /* Get next search result */
              final SearchResult searchResult = (SearchResult) answer.next();

              /* Check if screen pop is enabled */
              if (searchResult.getAttributes().get("cnt-screenpop-enabled") != null) {

                /* See if CRM pop up is enabled */
                boolean screenPopEnabled = false;
                if (((String) searchResult.getAttributes().get("cnt-screenpop-enabled").get()).equalsIgnoreCase("yes"))

                  screenPopEnabled = true;

                /* Value is false due to 'yes' from LDAP */
                if (!screenPopEnabled) {
                  logger.debug("---Inside getInfo: external CRM is enabled for agent: " + agentId);

                  /* Set if CRM pop up is enabled or not */
                  agent.setCrmPopEnabled(!screenPopEnabled);
                }

                /*
                 * After 7.0 release 'cnt-sp-int-type' will be used instead of 'cnt-sp-target-type'.
                 * This trick is for backward compatibility
                 */
                if (searchResult.getAttributes().get("cnt-sp-int-type") != null) {

                  /* Set CRM name */
                  agent.setCrmName((String) searchResult.getAttributes().get("cnt-sp-int-type").get());
                } else if (searchResult.getAttributes().get("cnt-sp-target-type") != null) {

                  /* Set CRM name */
                  agent.setCrmName((String) searchResult.getAttributes().get("cnt-sp-target-type").get());
                } else {
                  throw new CADException(
                      CADResponseCodes._1009
                          + "External CRM is enabled but CRM information: 'cnt-sp-target-type' or 'cnt-sp-int-type' not found : Used for finding external CRM type");
                }

                /* Set target URL only if sp enabled */
                if (screenPopEnabled) {
                  agent.setCrmURL((String) searchResult.getAttributes().get("cnt-sp-target-url").get());
                } else {
                  agent.setCrmURL(CRMConstants.eightByEightCRM);

                }
                /* Set 88 as CRM type if sp is not enabled */
                if (!screenPopEnabled)
                  agent.setCrmName(CRMConstants.eightByEightCRM);
                /* Set other CRM properties; if present */
                if (searchResult.getAttributes().get("cnt-sp-int-properties") != null) {

                  /* Set all CRM related properties */
                  agent.setCrmProperties((String) searchResult.getAttributes().get("cnt-sp-int-properties").get());
                }

                /* Set account id: for NetSuite integration */
                if (searchResult.getAttributes().get("cnt-sp-account") != null) {

                  /* Set NS CRM specific account id */
                  agent.setCrmAccountId((String) searchResult.getAttributes().get("cnt-sp-account").get());
                }

                /* Ignore CRM credendials for local CRM only */
                if ((agent.getCrmUserid() == null && agent.getCrmPassword() == null)) {

                  logger.debug("---Inside getInfo: external CRM credentials are not found, checking if its 8x8 CRM");

                  /* Check for internal CRM */
                  if (!this.isLocalCRM(agent.getCrmName())) {

                    /* Inform user about missing property */
                    throw new CADException(CADResponseCodes._1009 + "CRM credentials not found: Used for external CRM login");
                  }
                }

              } else {

                /* Inform user about missing property */
                throw new CADException(CADResponseCodes._1009 + "External CRM is not enabled for the Agent");
              }
            }
          }
        } else {

          /* Inform user about missing property */
          throw new CADException(CADResponseCodes._1009 + "Agent information is not found");
        }
      } catch (final NamingException namingException) {

        /* log this error */
        logger.error("=*=Exception at getInfo: ", namingException);

        /* Inform user about missing LDAP node */
        throw new CADException(CADResponseCodes._1009 + "Node not found : " + screenPopProperty, namingException);
      } finally {

        try {
          /* Close the naming enumeration */
          if (answer != null) {
            answer.close();
          }
        } catch (final NamingException e) {

          /* Inform user about missing LDAP node */
          throw new CADException(CADResponseCodes._1007 + "Not able to close LDAP connection");
        }
      }
    } else {

      /* Throw user experience */
      throw new CADException(CADResponseCodes._1008 + "Agent id in HTTP request");
    }

    logger.debug("---Inside getInfo: " + agent);

    if (agent == null) {

      /* Inform user about unauthorized agent */
      throw new CADException(CADResponseCodes._1012);
    }
    return agent;
  }

  /**
   * 
   * @param crmName
   * @return
   */
  private final boolean isLocalCRM(final String crmName) {

    /* Check for internal CRM */
    if (crmName == null || CRMConstants.customCRM.equalsIgnoreCase(crmName) || CRMConstants.coCRM.equalsIgnoreCase(crmName)
        || CRMConstants.eightByEightCRM.equalsIgnoreCase(crmName) || CRMConstants.contactualCRM.equalsIgnoreCase(crmName)) {

      return true;
    } else {

      return false;
    }
  }

  public final String getSplitString() {
    return splitString;
  }

  public final void setSplitString(final String splitString) {
    this.splitString = splitString;
  }
}
