package uk.gov.hmcts.ccd.documentam.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;
import uk.gov.hmcts.befta.util.ReflectionUtils;

public class CaseDocumentAmTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CaseDocumentAmTestAutomationAdapter.class);

    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);

    @Override
    protected BeftaTestDataLoader buildTestDataLoader() {
        return new DefaultBeftaTestDataLoader() {
            @Override
            public void doLoadTestData() {
                CaseDocumentAmTestAutomationAdapter.this.loader.addCcdRoles();
                CaseDocumentAmTestAutomationAdapter.this.loader.importDefinitions();
            }
        };
    }

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        //the docAMUrl is is referring the self link in PR
        String docAmUrl = EnvironmentVariableUtils.getRequiredVariable("TEST_URL");
        switch (key.toString()) {
            case ("documentIdInTheResponse"):
                return getDocumentIdInTheRresponse(scenarioContext);
            case ("validSelfLink"):
                return getValidSelfLink(scenarioContext, docAmUrl);
            case ("validBinaryLink"):
                return getValidBinaryLink(scenarioContext, docAmUrl);
            case ("hashTokenDifferentFromPrevious"):
                return getHashTokenDifferentFromPrevious(scenarioContext);
            default:
                return super.calculateCustomValue(scenarioContext, key);
        }

    }

    private Object getHashTokenDifferentFromPrevious(BackEndFunctionalTestScenarioContext scenarioContext) {
        try {
            String newHashToken = (String) ReflectionUtils
                .deepGetFieldInObject(scenarioContext,
                                     "testData.actualResponse.body.hashToken");

            String previousHashToken = (String) ReflectionUtils
                .deepGetFieldInObject(scenarioContext,
                                     "childContexts.S-064_Get_Hash_Token.testData.actualResponse.body.hashToken");

            BeftaUtils.defaultLog(scenarioContext.getCurrentScenarioTag() + " previousHashToken: " + previousHashToken);

            if (newHashToken != null && !newHashToken.equalsIgnoreCase(previousHashToken)) {
                return newHashToken;
            }
            return previousHashToken;
        } catch (Exception e) {
            throw new FunctionalTestException("Couldn't get previousHashToken from child context response field", e);
        }
    }

    private Object getValidBinaryLink(BackEndFunctionalTestScenarioContext scenarioContext, String docAmUrl) {
        try {
            String binary = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                                                                          "testData.actualResponse.body.documents[0]._links.binary.href");
            BeftaUtils.defaultLog(scenarioContext.getCurrentScenarioTag() + " Binary: " + binary);
            if (binary != null && binary.startsWith(docAmUrl + "/cases/documents/") && binary.endsWith("/binary")) {
                return binary;
            }
            return docAmUrl + "/cases/documents/<a document id>/binary";
        } catch (Exception e) {
            throw new FunctionalTestException("Couldn't get binary link from response field", e);
        }
    }

    private Object getValidSelfLink(BackEndFunctionalTestScenarioContext scenarioContext, String docAmUrl) {
        try {
            String self = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                                                                        "testData.actualResponse.body.documents[0]._links.self.href");
            BeftaUtils.defaultLog(scenarioContext.getCurrentScenarioTag() + " Self: " + self);

            if (self != null && self.startsWith(docAmUrl + "/cases/documents/")) {
                return self;
            }
            return docAmUrl + "/cases/documents/<a document id>";
        } catch (Exception e) {
            throw new FunctionalTestException("Couldn't get self link from response field", e);
        }
    }

    private Object getDocumentIdInTheRresponse(BackEndFunctionalTestScenarioContext scenarioContext) {
        try {
            String href = (String) ReflectionUtils
                .deepGetFieldInObject(scenarioContext,
                                      "testData.actualResponse.body.documents[0]._links.self.href");
            return href.substring(href.length() - 36);
        } catch (Exception exception) {
            logger.error("Exception while getting the Document ID from the response :{}", exception.getMessage());
            return "Error extracting the Document Id";
        }
    }
}
