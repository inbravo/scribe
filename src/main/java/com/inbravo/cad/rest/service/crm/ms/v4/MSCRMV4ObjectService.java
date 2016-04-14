package com.inbravo.cad.rest.service.crm.ms.v4;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.CRMConstants.MSCRMObjectType;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.service.crm.ms.MSCRMObjectService;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMV4ObjectService extends MSCRMObjectService implements ApplicationContextAware {

  private final Logger logger = Logger.getLogger(MSCRMV4ObjectService.class.getName());

  private ApplicationContext applicationContext;

  private MSCRMV4AccountService mSCRMV4AccountService;

  private MSCRMV4TaskService mSCRMV4TaskService;

  private MSCRMV4ContactService mSCRMV4ContactService;

  private MSCRMV4LeadService mSCRMV4LeadService;

  private MSCRMV4IncidentService mSCRMV4IncidentService;

  private MSCRMV4OpportunityService mSCRMV4OpportunityService;

  public MSCRMV4ObjectService() {
    super();
  }

  @Override
  public final CADCommandObject createObject(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside createObject");
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).createObject(eDSACommandObject);
  }

  @Override
  public final boolean deleteObject(final CADCommandObject eDSACommandObject, final String idToBeDeleted) throws Exception {
    logger.debug("---Inside deleteObject idToBeDeleted: " + idToBeDeleted);
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).deleteObject(eDSACommandObject, idToBeDeleted);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside getObjects");
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).getObjects(eDSACommandObject);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects query: " + query);
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).getObjects(eDSACommandObject, query);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select) throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select);
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).getObjects(eDSACommandObject, query, select);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select, final String order)
      throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select + " & order: " + order);
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).getObjects(eDSACommandObject, query, select, order);
  }

  @Override
  public CADCommandObject getObjectsCount(CADCommandObject eDSACommandObject) throws Exception {
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).getObjectsCount(eDSACommandObject);
  }

  @Override
  public final CADCommandObject getObjectsCount(CADCommandObject eDSACommandObject, final String query) throws Exception {
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).getObjectsCount(eDSACommandObject, query);
  }

  @Override
  public final CADCommandObject updateObject(final CADCommandObject eDSACommandObject) throws Exception {
    return this.getMSCRMObjectService(eDSACommandObject.getObjectType()).updateObject(eDSACommandObject);
  }

  private final MSCRMObjectService getMSCRMObjectService(final String objectType) {

    logger.debug("---Inside getMSCRMObjectService objectType: " + objectType);
    if (objectType.equalsIgnoreCase(MSCRMObjectType.Account.toString())) {
      return mSCRMV4AccountService;
    } else if (objectType.equalsIgnoreCase(MSCRMObjectType.Task.toString())) {
      return mSCRMV4TaskService;
    } else if (objectType.equalsIgnoreCase(MSCRMObjectType.Contact.toString())) {
      return mSCRMV4ContactService;
    } else if (objectType.equalsIgnoreCase(MSCRMObjectType.Lead.toString())) {
      return mSCRMV4LeadService;
    } else if (objectType.equalsIgnoreCase(MSCRMObjectType.Opportunity.toString())) {
      return mSCRMV4OpportunityService;
    } else if (objectType.equalsIgnoreCase(MSCRMObjectType.Incident.toString())) {
      return mSCRMV4IncidentService;
    } else {
      throw new CADException(CADResponseCodes._1003 + "Following object type is not supported by the EDSA");
    }
  }

  public final MSCRMV4AccountService getmSCRMV4AccountService() {
    return mSCRMV4AccountService;
  }

  public final void setmSCRMV4AccountService(final MSCRMV4AccountService mSCRMV4AccountService) {
    this.mSCRMV4AccountService = mSCRMV4AccountService;
  }

  public final MSCRMV4TaskService getmSCRMV4TaskService() {
    return mSCRMV4TaskService;
  }

  public final void setmSCRMV4TaskService(final MSCRMV4TaskService mSCRMV4TaskService) {
    this.mSCRMV4TaskService = mSCRMV4TaskService;
  }

  public final MSCRMV4ContactService getmSCRMV4ContactService() {
    return mSCRMV4ContactService;
  }

  public final void setmSCRMV4ContactService(final MSCRMV4ContactService mSCRMV4ContactService) {
    this.mSCRMV4ContactService = mSCRMV4ContactService;
  }

  public final MSCRMV4LeadService getmSCRMV4LeadService() {
    return mSCRMV4LeadService;
  }

  public final void setmSCRMV4LeadService(final MSCRMV4LeadService mSCRMV4LeadService) {
    this.mSCRMV4LeadService = mSCRMV4LeadService;
  }

  public final MSCRMV4IncidentService getmSCRMV4IncidentService() {
    return mSCRMV4IncidentService;
  }

  public final void setmSCRMV4IncidentService(final MSCRMV4IncidentService mSCRMV4IncidentService) {
    this.mSCRMV4IncidentService = mSCRMV4IncidentService;
  }

  public final MSCRMV4OpportunityService getmSCRMV4OpportunityService() {
    return mSCRMV4OpportunityService;
  }

  public final void setmSCRMV4OpportunityService(final MSCRMV4OpportunityService mSCRMV4OpportunityService) {
    this.mSCRMV4OpportunityService = mSCRMV4OpportunityService;
  }

  @Override
  public final void setApplicationContext(final ApplicationContext ApplicationContext) throws BeansException {
    this.applicationContext = ApplicationContext;
  }

  public final ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
