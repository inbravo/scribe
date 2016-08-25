package com.inbravo.scribe.external.junit.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.inbravo.scribe.external.junit.MicroSoftAccountTest;
import com.inbravo.scribe.external.junit.MicroSoftContactTest;
import com.inbravo.scribe.external.junit.MicroSoftIncidentTest;
import com.inbravo.scribe.external.junit.MicroSoftLeadTest;
import com.inbravo.scribe.external.junit.MicroSoftOpportunityTest;
import com.inbravo.scribe.external.junit.MicroSoftTaskTest;
import com.inbravo.scribe.external.junit.SalesForceAccountTest;
import com.inbravo.scribe.external.junit.SalesForceCampaignTest;
import com.inbravo.scribe.external.junit.SalesForceCaseTest;
import com.inbravo.scribe.external.junit.SalesForceContactTest;
import com.inbravo.scribe.external.junit.SalesForceLeadTest;
import com.inbravo.scribe.external.junit.SalesForceObjectCountTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({SalesForceAccountTest.class, SalesForceContactTest.class, SalesForceLeadTest.class, SalesForceCaseTest.class,
    SalesForceCampaignTest.class, SalesForceObjectCountTest.class, MicroSoftAccountTest.class, MicroSoftContactTest.class, MicroSoftTaskTest.class,
    MicroSoftIncidentTest.class, MicroSoftOpportunityTest.class, MicroSoftLeadTest.class})
public class ScribeTestSuite {
  /* Following class will not hold any content */
}
