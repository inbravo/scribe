package com.inbravo.cad.external.junit.suite;

import org.junit.runner.RunWith;

import org.junit.runners.Suite;

import com.inbravo.cad.external.junit.MicroSoftAccountTest;
import com.inbravo.cad.external.junit.MicroSoftContactTest;
import com.inbravo.cad.external.junit.MicroSoftIncidentTest;
import com.inbravo.cad.external.junit.MicroSoftLeadTest;
import com.inbravo.cad.external.junit.MicroSoftOpportunityTest;
import com.inbravo.cad.external.junit.MicroSoftTaskTest;
import com.inbravo.cad.external.junit.SalesForceAccountTest;
import com.inbravo.cad.external.junit.SalesForceCampaignTest;
import com.inbravo.cad.external.junit.SalesForceCaseTest;
import com.inbravo.cad.external.junit.SalesForceContactTest;
import com.inbravo.cad.external.junit.SalesForceLeadTest;
import com.inbravo.cad.external.junit.SalesForceObjectCountTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({SalesForceAccountTest.class, SalesForceContactTest.class, SalesForceLeadTest.class, SalesForceCaseTest.class,
    SalesForceCampaignTest.class, SalesForceObjectCountTest.class, MicroSoftAccountTest.class, MicroSoftContactTest.class, MicroSoftTaskTest.class,
    MicroSoftIncidentTest.class, MicroSoftOpportunityTest.class, MicroSoftLeadTest.class})
public class CADTestSuite {
  /* Following class will not hold any content */
}
