package com.inbravo.cad.rest.service.crm.ns.type;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface NSCRMFieldTypes {

  /* NS custom field reference type for create operation */
  String CREATE_LONG = "Long";
  String CREATE_STRING = "String";
  String CREATE_BOOLEAN = "Boolean";
  String CREATE_CALANDER = "Calander";
  String CREATE_DURATION = "Duration";
  String CREATE_DOUBLE = "Double";
  String CREATE_RECORD_REF = "RecordRef";
  String CREATE_TASK_STATUS = "TaskStatus";
  String CREATE_PHONE_CALL_STATUS = "PhoneCallStatus";

  /* NS custom field reference type for create operation */
  String SEARCH_LONG_FIELD = "SearchLongField";
  String SEARCH_STRING_FIELD = "SearchStringField";
  String SEARCH_DATE_FIELD = "SearchDateField";
  String SEARCH_BOOLEAN_FIELD = "SearchBooleanField";
  String SEARCH_DOUBLE_FIELD = "SearchDoubleField";
  String SEARCH_ENUM_MULTI_SELECT_FIELD = "SearchEnumMultiSelectField";
  String SEARCH_TEXT_NUMBER_FIELD = "SearchTextNumberField";
  String SEARCH_STRING_CUSTOM_FIELD = "SearchStringCustomField";
  String SEARCH_LONG_CUSTOM_FIELD = "SearchLongCustomField";
  String SEARCH_DATE_CUSTOM_FIELD = "SearchDateCustomField";
  String SEARCH_BOOLEAN_CUSTOM_FIELD = "SearchBooleanCustomField";
  String SEARCH_MULTI_SELECT_CUSTOM_FIELD = "SearchMultiSelectCustomField";

  /* NS custom field reference type for create operation */
  String CREATE_STRING_CUSTOM_FIELD_REF = "StringCustomFieldRef";
  String CREATE_MUTISELECT_CUSTOM_FIELD_REF = "MultiSelectCustomFieldRef";
  String CREATE_BOOLEAN_CUSTOM_FIELD_REF = "BooleanCustomFieldRef";
  String CREATE_LONG_CUSTOM_FIELD_REF = "LongCustomFieldRef";
  String CREATE_DATE_CUSTOM_FIELD_REF = "DateCustomFieldRef";
  String CREATE_DOUBLE_CUSTOM_FIELD_REF = "DoubleCustomFieldRef";

  String CRM_FIELD_ID = "id";
  String CRM_FIELD_INTERNALID = "INTERNALID";
  String CRM_FIELD_REGARDINGOBJECTID = "REGARDINGOBJECTID";
  String CRM_FIELD_ATTRIBUTE_TYPE = "type";
  String CRM_FIELD_ATTRIBUTE_REFERENCE = "reference";
  String CRM_FIELD_STATUS = "STATUS";
  String CRM_FIELD_COMPANY = "COMPANY";
  String CRM_FIELD_CONTACT = "CONTACT";
  String CRM_FIELD_ACTUALTIME = "ACTUALTIME";
  String CRM_FIELD_DUEDATE = "DUEDATE";
  String CRM_FIELD_STARTDATE = "STARTDATE";
  String CRM_FIELD_COMPLETEDDATE = "COMPLETEDDATE";
  String CRM_FIELD_ENDDATE = "ENDDATE";
}
