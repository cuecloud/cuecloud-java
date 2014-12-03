package com.cuecloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author nmvictor 
 * 
 */
public class CueCloud {
    private static final String API_VERSION = "api/v1.0/";
    private static final String DEFAULT_BASE_URL = "https://cuecloud.com/";
    private final String baseURL;
    private final String apiKey;
    private final String apiPass;

    /**
     * Concatenates <code>uri</code> with a query string generated from
     * <code>params</code>.
     *
     * @param uri the base URI
     * @param params a <code>Map</code> of key/value pairs
     * @return a new <code>URI</code>
     */
    
    public static String urlWithQueryParams(String uri, Map<String, Object> params) {
        StringBuilder query = new StringBuilder();
        char separator = '?';
        for (Entry<String, Object> param : params.entrySet()) {
            query.append(separator);
            separator = '&';
            try {
                query.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                if (!(param.getValue()+"").isEmpty()) {
                    query.append('=');
                    query.append(URLEncoder.encode(param.getValue()+"", "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
        }
        }
        return uri + query.toString();
    }

    /**
     *  Construct CueCloud wrapper with URL as {@link https://cuecloud.com/api/v1.0/ https://cuecloud.com/api/v1.0/}
     * @param apiKey Your CueCloud API key. If this value if {@code NULL}, we will attempt to read the apiKey from the environment variable <code>CUECLOUD_ACCESS_KEY</code>.
     * @param apiPass Your CueCloud API password. If this value if {@code NULL}, we will attempt to read the apiPassword from the environment variable <code>CUECLOUD_ACCESS_PASSWORD</code>.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if apiKey or apiPass values are null and neither are found in System environment variable.
     */
    public CueCloud(String apiKey, String apiPass) throws  CueCloudException{
        this(apiKey, apiPass, DEFAULT_BASE_URL+API_VERSION);
    }
            
    /**
     *  Construct CueCloud wrapper with custom URL.
     * @param apiKey  Your CueCloud API key. If this value if {@code NULL}, we will attempt to read the apiKey from the environment variable <code>CUECLOUD_ACCESS_KEY</code>.
     * @param apiPass Your CueCloud API password. If this value if {@code NULL}, we will attempt to read the apiPassword from the environment variable <code>CUECLOUD_ACCESS_PASSWORD</code>.
     * @param baseURL Custom URL to call the CueClud API methods. This is important if you want to make the calls from your localhost or if CueCloud server is deployed in a 
     * location other than 'https://cuecloud.com/api/v1.0/'. You can also use this to specify a different CueCloud version to use.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if apiKey or apiPass values are null and neither are found in System environment variable.
     */
    public CueCloud(String apiKey, String apiPass,String baseURL) throws  CueCloudException{
      if(apiKey == null) {
           apiKey = System.getenv("CUECLOUD_ACCESS_KEY");
           if(apiKey == null) {
               throw new CueCloudException("CUECLOUD_ACCESS_KEY not set.");
           }
        }
        
        if(apiPass == null) {
            apiPass = System.getenv("CUECLOUD_ACCESS_PASSWORD");
            if(apiPass == null) {
               throw new CueCloudException("CUECLOUD_ACCESS_PASSWORD not set.");
            }
        }
     
        this.baseURL = baseURL;
        this.apiKey = apiKey;
        this.apiPass = apiPass;
    }
    
    /**
     * Wrapper Exception for all CueCloud operations.
     */
    public class CueCloudException extends  Exception{
        
        public CueCloudException(String message) {
            super(message);
        }
        
    }
    /**\
     *  Get HMAC SHA256 encoding.
     * @param key The key to use for encoding.
     * @param message The message to encode.
     * @return The encoded message in SHA256 form.
     * @throws Exception If any erro occurs during the encoding.
     */
     public static String HMAC_SHA256encode(String key, String message) throws Exception {

        SecretKeySpec keySpec = new SecretKeySpec(
                key.getBytes(),
                "HmacSHA256");

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(message.getBytes());

        return Hex.encodeHexString(rawHmac);
    }
     
    /**
     * Makes a HTTP request to the <code>url</code> provided.
     * @param url The URL to make HTTP request to.
     * @param httpMethod The HTTP method for this request. Can either be <code>GET, DELETE, POST </code> or <code> PUT</code>.
     * @param sig The signature for this request.
     * @param nonce The nonce for this request.
     * @return  Returns the JSON string of this request.
     * @throws MalformedURLException If <code>url</code> is malformed ir invalid.
     * @throws IOException  If we have IO error during the request. Probably due to connection failure.
     */
    private String cueCloudHttpRequest(String url,String httpMethod,String sig,String nonce, String data) throws MalformedURLException, IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(httpMethod);
        con.setRequestProperty("Access-Key", this.apiKey);
        con.setRequestProperty("Access-Signature", sig);
        con.setRequestProperty("Access-Nonce", nonce);
        con.setRequestProperty("Content-Type", "application/json");
        if(data != null && !data.isEmpty()) {
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(data.getBytes());
            os.flush();
        }
        int responseCode = con.getResponseCode();
        
        StringBuilder response;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                    }
        }
        
        //print result
        System.out.println("Result: "+response);
        return  response.toString();
    }
    
    
    /**
     * Builds a HTTP request, signs it and makes the connection request.
     * @param url The URL to make the request for.
     * @param method The CueCloud API method for this request.
     * @param data Any data required by the request body.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException  If URL is not provided, or if an error occurs when signing the request or
     * if any IO error when making HTTP connection of this request.
     */
    public String buildRequest(String url,String method, Object data) throws  CueCloudException{
        if(url == null) {
            throw new CueCloudException("Url must be provided to this method call");
        }
        String nonce =  (System.currentTimeMillis() * 1000) + "";
        String body = "";
        if(data != null){
            if(data instanceof String) {
                body = data.toString();
            } else {
              body= new flexjson.JSONSerializer().serialize(data);
            }
        }
        
        String message = nonce + url + body;
        String sig = null;
        try {
            sig = HMAC_SHA256encode(this.apiPass, message);
        } catch(Exception e) {
            throw new CueCloudException(e.getMessage()+"");
        }
        String result = null;
        
        try {
            result =  cueCloudHttpRequest(url, method, sig, nonce,body);
        } catch(Exception e) {
            throw new CueCloudException(e.getMessage());
        }
        
        return result;
    }
    
    
    /**
     *  This is a test method to make sure that
     * the user has valid API credentials.
     * @return JSON result of this validation.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String validateUser () throws CueCloudException {
        String methodPath = "validate/";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        return  buildRequest(url, httpMethod, null);
    }
    
    
    /**
     * This will return common keywords for Cues.
     * Useful for CueCreation.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String getKeywords() throws CueCloudException {
        String methodPath = "cues/keywords/";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        return  buildRequest(url, httpMethod, null);
    }
    
    /**
     * This will return the user's current balance, in USD
     * @return JSON string containing the balance.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String getBalance() throws CueCloudException {
         String methodPath = "balance/";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        return  buildRequest(url, httpMethod, null);
    }
    
    
    /**
     * Given a valid CC on file in the app,
     * This will deposit that amount into the user's balance.
     * Note, a credit card may only be added within the app. Not the API.
     * @param amount The amount in USD to deposit.
     * @param creditCardLast4Digits The last for digits of users credit card.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String makeDeposit(double  amount, int creditCardLast4Digits) throws CueCloudException {
          String methodPath = "payments/deposit/";
        String httpMethod = "POST";
        HashMap<String, Object> map = new HashMap<>();
        map.put("AmountInUSD", amount);
        map.put("CreditCardLastFourDigits", creditCardLast4Digits);
        
        String url = baseURL +methodPath;
        return  buildRequest(url, httpMethod, map);
    }
    
    /**
     * Given a PayPal email, this will deposit the funds
     * immediately into that user's PayPal account.
     * If no amount is specified, it will try and
     * deduct the entire user's balance.
     * Note, a credit card may only be added within the app. Not the API.
     * @param amountInUSD The amount in USD to withdraw.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String withdrawFunds(double  amountInUSD) throws CueCloudException {
        String methodPath = "payments/withdraw/";
        String httpMethod = "POST";
        HashMap<String, Object> map = new HashMap<>();
        map.put("AmountInUSD", amountInUSD);
        
        String url = baseURL +methodPath;
        return  buildRequest(url, httpMethod, map);
    }
    
    
    /**
     * This will grant a bonus to the user who has completed a particular Cue for us.
     * A reason for the bonus must be specified, though here
     * we default to "Thanks for your hard work!" if none is provided.
     * Note to self can be provided, which is a string that can only be viewed
     * by the person who granted the bonus. An example might be:
     * "Bonus paid here on 2014-01-01 to see if it motivates better work from this person."
     * 
     * @param cueCompletionID The ID of the completed Cue.
     * @param amountInUSD The amount in USD to grant as bonus
     * @param reason Reason for the bonus. You may leave this as <code>null</code> as it is optional.
     * @param noteToSelf Note to be viewed by the person who granted the bonus. You may leave this as <code>null</code> as it is optional.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String grantBonus(String cueCompletionID,Double amountInUSD, String reason, String noteToSelf) throws CueCloudException {
        if(reason == null) {
            reason = "Thanks for your hard work!";
        }
        String methodPath = "payments/bonus/";
        String httpMethod = "POST";
        HashMap<String, Object> map = new HashMap<>();
        map.put("Amount", amountInUSD);
        map.put("CueCompletionID", cueCompletionID);
        map.put("Reason", reason);
        map.put("NoteToSelf", noteToSelf);
        String url = baseURL +methodPath;
        return  buildRequest(url, httpMethod, map);
    }
    
   
    /**
     * List the payments.
     * 
     * @param paymentType The Payment Type to filter. Can either be {@link PaymentType#Deposits Deposits},{@link PaymentType#Withdrawals Withdrawals} or {@link PaymentType#Bonuses Bonuses}
     * @param paymentID The Payment ID to filter
     * @param page Integer indicating the number of pages to items per page. By default, 50 items will be shown per page.
     * @param noteToSelf Note to be viewed by the person who requested the payments.You may leave this as <code>null</code> as it is optional.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String getPayments(PaymentType paymentType,Integer paymentID,String noteToSelf, Integer page) throws CueCloudException {
      
        String methodPath = "payments/";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("PaymentType", paymentType);
        map.put("PaymentID", paymentID);
        map.put("Page", page);
        map.put("NoteToSelf", noteToSelf);
        url  = urlWithQueryParams(url, map);
        return  buildRequest(url, httpMethod, null);
    }
   
    /**
     * This will approve a CueCompletion that has been submitted to the user's Cue.
     * 
     * @param cueCompletionID The ID of the completed Cue.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String approveCueCompletion(String cueCompletionID) throws CueCloudException {
      
        String methodPath = "completions/approve/";
        String httpMethod = "POST";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("CueCompletionID", cueCompletionID);
        return  buildRequest(url, httpMethod, map);
    }
    
    /**
     *  This will decline a CueCompletion that has been submitted to the user's Cue.
     * 
     * @param cueCompletionID The ID of the completed Cue.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String declineCueCompletion(String cueCompletionID) throws CueCloudException {
      
        String methodPath = "completions/decline/";
        String httpMethod = "POST";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("CueCompletionID", cueCompletionID);
        return  buildRequest(url, httpMethod, map);
    }
   
    /**
     *  This will cancel a Cue that you have posted, refunding your balance.
     * 
     * @param cueID The ID of the Cue to cancel.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String cancelCue(String cueID) throws CueCloudException {
      
        String methodPath = "cues/cancel/";
        String httpMethod = "POST";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("CueID", cueID);
        return  buildRequest(url, httpMethod, map);
    }
    
    /**
     * This will return CueCompletions for a particular Cue.
     * 
     * @param cueID The ID of the Cue you want to get completions. This is not optional.
     * @param cueCompletionID The ID of a completed Cue if you want to filter, This is optional and <code>null</code> may be provided to get all.
     * @param cueStatus The status of Cue completions. This is important if you want to filter.Values may be {@link CueCompletionStatus#Accepted Accepted},
     * {@link CueCompletionStatus#Declined Declined} or {@link CueCompletionStatus#Pending Pending}. This is optional, thus <code>null</code> may be provided.
     * @param page Optional Integer indicating the number of pages to items per page. By default, 50 items will be shown per page.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String getCueCompletions(String cueID, Integer cueCompletionID ,CueCompletionStatus cueStatus, Integer page) throws CueCloudException {
      
        String methodPath = "completions/";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("CueID", cueID);
        map.put("CueCompletionID", cueCompletionID);
        map.put("Page", page);
        map.put("Status", cueStatus);
        url  = urlWithQueryParams(url, map);
        return  buildRequest(url, httpMethod, null);
    }
    
    /**
     * Create a new Cue.
     * 
     * @param title The title of the Cue. Must be provided.
     * @param amount The amount for this Cue. Must be provided.
     * @param numOpportunities Number of opportunities for this Cue. Must be provided.
     * @param description The description of this Cue.Optional.
     * @param isAnonymous If this is an anonymous Cue. Optional.
     * @param pushNotificationOnCueCompletion whether to receive push notifications on Cue Completion.Optional.
     * @param disallowAnonymousCueCompletions Whether reject anonymous cue completions.Optional.
     * @param iFrameURL Can be specified if you want a user to fill out a custom form on your site.Optional.
     * @param uRLNotificationOnCueCompletion Whether to receive URL notification on completion.Optional.
     * @param emailNotificationOnCueCompletion Whether to receive email notifications in minutes.
     * @param lifetimeInMinutes Cue Lifetime in minutes.Optional.
     * @param timeLimitToCompleteCueInMinutes Life time to completion in minutes.Optional.
     * @param autoApproveCueCompletionAfterThisManyMinutes Timeout to auto-approve completions.Optional.
     * @param noteToSelf Note to self on cue creation.
     * @param keywords Keywords of this Cue.
     * @return  JSON string in response t this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String createCue(String title, double amount, int numOpportunities, String description,
            Boolean isAnonymous,Boolean pushNotificationOnCueCompletion,Boolean disallowAnonymousCueCompletions,
            String iFrameURL,Boolean uRLNotificationOnCueCompletion,Boolean emailNotificationOnCueCompletion,
            Integer lifetimeInMinutes,Integer timeLimitToCompleteCueInMinutes,
            Integer autoApproveCueCompletionAfterThisManyMinutes, String noteToSelf, String keywords) throws CueCloudException{
          String methodPath = "cues/create";
        String httpMethod = "POST";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("Title", title);
        map.put("Amount",amount);
        map.put("NumOpportunities", numOpportunities);
        map.put("IsAnonymous", isAnonymous);
        map.put("PushNotificationOnCueCompletion", pushNotificationOnCueCompletion);
        map.put("DisallowAnonymousCueCompletions", disallowAnonymousCueCompletions);
        map.put("iFrameURL", iFrameURL);
        map.put("URLNotificationOnCueCompletion", uRLNotificationOnCueCompletion);
        map.put("EmailNotificationOnCueCompletion", emailNotificationOnCueCompletion);
        map.put("LifetimeInMinutes", lifetimeInMinutes); 
        map.put("TimeLimitToCompleteCueInMinutes", timeLimitToCompleteCueInMinutes); 
        map.put("AutoApproveCueCompletionAfterThisManyMinutes", autoApproveCueCompletionAfterThisManyMinutes); 
        map.put("NoteToSelf", noteToSelf); 
        map.put("Keywords", keywords); 
        return buildRequest(url, httpMethod, map);
    }
    
    
    /**
     * This will get all Cues the user has created.
     * 
     * @param cueID The ID of the Cue you want to filter. This is optional.
     * @param cueGroupID The ID of the Group the cue belongs to, if you want to filter, This is optional and <code>null</code> may be provided to get all.
     * @param hasPendingCueCompletions Optional Boolean to include Cues with pending completions.
     * @param cueStatus The status of Cue. This is important if you want to filter.Values may be {@link CueStatus#Active Active},
     * {@link CueStatus#Complete Complete},{@link CueStatus#Canceled Canceled} or {@link CueStatus#Expired Expired}. This is optional, thus <code>null</code> may be provided.
     * @param noteToSelf Optional note for this request.
     * @param page Optional Integer indicating the number of pages to items per page. By default, 50 items will be shown per page.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String getCues(String cueID, Integer cueGroupID ,Boolean hasPendingCueCompletions, CueStatus cueStatus, String noteToSelf, Integer page) throws CueCloudException {
      
        String methodPath = "cues/";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("CueID", cueID);
        map.put("GroupID", cueGroupID);
        map.put("NoteToSelf", noteToSelf);
        map.put("HasPendingCueCompletions", hasPendingCueCompletions);
        map.put("Status", cueStatus);
        map.put("Page", page);
        url  = urlWithQueryParams(url, map);
        return  buildRequest(url, httpMethod, null);
    }
    
    
    /**
     * This will try and check-in or check-out a Cue depending
     * on whether the Cue is already checked out by that user..
     * 
     * @param cueID The ID of the Cue you want to assign. This is not optional.
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String assignCue(String cueID) throws CueCloudException {
      
        String methodPath = "cues/";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("CueID", cueID);
        return  buildRequest(url, httpMethod, map);
    }
    
    
    /**
     * This will submit the CueCompletion data, though
     * In production the method will block any requests without an HTTP_REFERER. 
     * @param assignmentID The Cue assignment ID.
     * @param answerText The answer text for thus submission.Optional.
     * @param videoURL The video URL. Optional.
     * @param videoThumbnailURL The video thumbnail URL. Optional
     * @param imageURL The image URL. Optional
     * @param isAnonymous Boolean flag to indicate whether this submission should be anonymous. Optional
     * @return JSON string in response to this request.
     * @throws com.cuecloud.CueCloud.CueCloudException Thrown if an error occurs during the operation.
     */
    public String submitCueCompletion(String assignmentID,String answerText,String videoURL,String videoThumbnailURL, String imageURL, Boolean isAnonymous) throws CueCloudException {
      
        String methodPath = "cues/complete";
        String httpMethod = "GET";
        String url = baseURL +methodPath;
        HashMap<String, Object> map = new HashMap<>();
        map.put("AssignmentID", assignmentID);
        map.put("AnswerText", answerText);
        map.put("VideoURL", videoURL);
        map.put("VideoThumbnailURL", videoThumbnailURL);
        map.put("ImageURL", imageURL);
        map.put("IsAnonymous", isAnonymous);
        return  buildRequest(url, httpMethod, map);
    }
    /**
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String TEST_API_KEY ="42cef2c79a984e34";
        String TEST_API_PASS="2152b0f3cc1649fb";
        try {
        CueCloud cc = new CueCloud(TEST_API_KEY, TEST_API_PASS);
        System.out.println(cc.getBalance());
        System.out.println(cc.getCues("", 0, null, CueStatus.Active, null, null));
        } catch(CueCloudException c) {
            System.out.println(c.getMessage());
        }
    }
    
    
    /**
     * Enumerated types of CueCloud Payment Types
     * 
     * @author nmvictor 
     */
    public enum PaymentType {
        /**
         * Payments Deposited
         */
        Deposits,
        /**
         * Payments Withdrawn
         */
        Withdrawals,
        /**
         * Payments Granted as Bonus
         */
        Bonuses;
    }
    
     /**
     * Enumerated types of Cue Status.
     * 
     * @author nmvictor 
     */
    public enum CueStatus {
        /**
         * Active Cues.
         */
        Active,
        /**
         * Completed Cues
         */
        Complete,
        /**
         * Canceled Cues
         */
        Canceled,
        /**
         * Expired Cues
         */
        Expired;
    }
    
    
    /**
     * Enumerated types of Cue completion Status.
     * 
     * @author nmvictor 
     */
    public enum CueCompletionStatus {
        /**
         * Cues Pending Acceptance or Decline.
         */
        Pending,
        /**
         * Cues Accepted
         */
        Accepted,
        /**
         * Cues Declined
         */
        Declined;
    }
    
}
