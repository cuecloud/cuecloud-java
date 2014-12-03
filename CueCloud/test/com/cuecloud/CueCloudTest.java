/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cuecloud;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Suite for CueCloud
 * 
 * @author nmvictor
 */
public class CueCloudTest {
    
    private static final String TEST_API_KEY ="42cef2c79a984e34";
    private static final String TEST_API_PASS="2152b0f3cc1649fb";
    
    private CueCloud cueCloud;
    
    public CueCloudTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        try {
            cueCloud = new CueCloud(TEST_API_KEY, TEST_API_PASS);
        } catch (CueCloud.CueCloudException ex) {
            Logger.getLogger(CueCloudTest.class.getName()).log(Level.SEVERE, ex.getMessage(),ex);
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     *  Test to make sure a valid user account
        and that the status code returned is 200.
     * @throws java.lang.Exception       
     */
    @Test
    public void testValidateUser() throws Exception {
        System.out.println("validateUser");
        CueCloud instance = cueCloud;
        String result = instance.validateUser();
        
        assertEquals(result.contains(" \"StatusCode\": 200"), true);
    }

    /**
     *  Get the keywords and confirm that 'facebook' is one
     * of the keywords returned.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testGetKeywords() throws Exception {
        System.out.println("getKeywords");
        CueCloud instance = cueCloud;
        String result = instance.getKeywords();
        assertEquals(result.contains("facebook"), true);
    }


    /**
     *  Make a deposit (add funds to user account) and make sure that:
     * [User's New Balance] = [User's Old Balance] + [Deposit Amount].
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testMakeDeposit() throws Exception {
        System.out.println("makeDeposit");
        double amount = 10.0;
        int creditCardLast4Digits = 111;
        CueCloud instance = cueCloud;
        double prevBalance = new JSONObject(instance.getBalance()).getJSONObject("Data").getDouble("Balance");
        String result = instance.makeDeposit(amount, creditCardLast4Digits);
        double currBalance = new JSONObject(instance.getBalance()).getJSONObject("Data").getDouble("Balance");
        assertEquals(prevBalance == currBalance, false);
    
    }

    /**
     * Test of withdrawFunds method, of class CueCloud.
     * @throws java.lang.Exception
     */
    @Test
    public void testWithdrawFunds() throws Exception {
        System.out.println("withdrawFunds");
        double amountInUSD = 10.0;
        CueCloud instance = cueCloud;
        double prevBalance = new JSONObject(instance.getBalance()).getJSONObject("Data").getDouble("Balance");
        String result = instance.withdrawFunds(amountInUSD);
        double currBalance = new JSONObject(instance.getBalance()).getJSONObject("Data").getDouble("Balance");
        assertEquals(Math.abs(currBalance - prevBalance), amountInUSD,2.0);
    }

     /**
     * Create a Cue and make sure that:
     * (1) The user now has one more cue to fetch.
     * (2) The user's balance is now:
     * [User's New Balance] = [User's Old Balance] - [CueAmount] * [Num Opportunities] * [CueCloud Percent Fee].
     * @throws java.lang.Exception       
     */
    @Test
    public void testCreateCue() throws Exception {
        System.out.println("createCue");
        int prevNum = new JSONObject(cueCloud.getCues(null, null, null, null, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        double prevBal = new JSONObject(cueCloud.getBalance()).getJSONObject("Data").getDouble("Balance");
        String res =  cueCloud.createCue("Newer Cue", 100.0, 4, null, null, null, null, null, null, null, null, null, null, null, null);
        int newNum = new JSONObject(cueCloud.getCues(new JSONObject(res).getJSONObject("Data").getString("CueID"), null, null, null, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        double newBal = new JSONObject(cueCloud.getBalance()).getJSONObject("Data").getDouble("Balance");
        assertEquals(newNum > prevNum, true);
       
    }
    
    /**
     * Submit a Cue Completion and make sure that the number of new
     * cue completions for that cue is greater than the number of cue
     * completions previously. 
     * @throws java.lang.Exception
     */
    @Test
    public void testSubmitCueCompletion() throws Exception {
        System.out.println("submitCueCompletion");
        String lastCueID = new JSONObject(cueCloud.getCues(null, null, null, null, null, null)).getJSONObject("Data").getJSONArray("Cues").getJSONObject(0).getString("ID");
        int prevNumComps = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        String assignID = new JSONObject(cueCloud.assignCue(lastCueID)).getJSONObject("Data").getString("NumTotalResults");
        String result = cueCloud.submitCueCompletion(assignID, null, null, null, null, null);
        int newumComps = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        assertEquals(newumComps > prevNumComps, true);
    }


    
    /**
     * Grant a bonus and make sure that the number
     * of payments that the user has is greater than the number
     * of payments the user previously had.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testGrantBonus() throws Exception {
        System.out.println("grantBonus");
        int prevNumPay = new JSONObject(cueCloud.getPayments(null, null, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        String lastCueID = new JSONObject(cueCloud.getCues(null, null, null, null, null, null)).getJSONObject("Data").getJSONArray("Cues").getJSONObject(0).getString("ID");
        String lastCueCompletionID = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, null, null)).getJSONObject("Data").getJSONArray("CueCompletions").getJSONObject(0).getString("ID");
        cueCloud.grantBonus(lastCueCompletionID, 100.0, null, null);
        int newNumPay = new JSONObject(cueCloud.getPayments(null, null, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        assertEquals(newNumPay > prevNumPay, true);
  
    }
   
    /**
     * Test of approveCueCompletion method, of class CueCloud.
     * @throws java.lang.Exception
     */
    @Test
    public void testApproveCueCompletion() throws Exception {
        System.out.println("approveCueCompletion");
        String lastCueID = new JSONObject(cueCloud.getCues(null, null, null, null, null, null)).getJSONObject("Data").getJSONArray("Cues").getJSONObject(0).getString("ID");
        int prevNumComp = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, null, null)).getJSONObject("Data").getInt("NumberOfCueCompletionsApproved");
        String cueID2Approve = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, CueCloud.CueCompletionStatus.Declined, null)).getJSONObject("Data").getJSONArray("CueCompletions").getJSONObject(0).getString("ID");
        int numComp = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, null, null)).getJSONObject("Data").getInt("NumberOfCueCompletionsApproved");
        assertEquals(numComp > prevNumComp, true);
    }

    /**
     * Decline a Cue Completion and make sure that the number
     * of pending Cue Completions is now less than it was previously.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testDeclineCueCompletion() throws Exception {
        System.out.println("declineCueCompletion");
        String lastCueID = new JSONObject(cueCloud.getCues(null, null, null, null, null, null)).getJSONObject("Data").getJSONArray("Cues").getJSONObject(0).getString("ID");
        int prevNumComp = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, null, null)).getJSONObject("Data").getInt("NumberOfCueCompletionsPendingReview");
        String cueID2Approve = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, CueCloud.CueCompletionStatus.Pending, null)).getJSONObject("Data").getJSONArray("CueCompletions").getJSONObject(0).getString("ID");
        int numComp = new JSONObject(cueCloud.getCueCompletions(lastCueID, null, null, null)).getJSONObject("Data").getInt("NumberOfCueCompletionsPendingReview");
        assertEquals(numComp < prevNumComp, true);
  
    }

    /**
     *   Cancel a Cue and make sure that the number of Active
     * Cues the user has is now one less than it previously was.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCancelCue() throws Exception {
        System.out.println("cancelCue");
        int prevNumActive =  new JSONObject(cueCloud.getCues(null, null, null, CueCloud.CueStatus.Active, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        String lastCueID = new JSONObject(cueCloud.getCues(null, null, null, CueCloud.CueStatus.Active, null, null)).getJSONObject("Data").getJSONArray("Cues").getJSONObject(0).getString("ID");
        cueCloud.cancelCue(lastCueID);
        int numActive =  new JSONObject(cueCloud.getCues(null, null, null, CueCloud.CueStatus.Active, null, null)).getJSONObject("Data").getInt("NumTotalResults");
        assertEquals(numActive < prevNumActive, true);
    }

    
}
