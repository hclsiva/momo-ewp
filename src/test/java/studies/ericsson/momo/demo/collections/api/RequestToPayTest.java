package studies.ericsson.momo.demo.collections.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import studies.ericsson.momo.demo.collections.model.RequestToPay;
import studies.ericsson.momo.demo.collections.model.RequestToPayResult;
import studies.ericsson.momo.demo.collections.model.TokenPost200ApplicationJsonResponse;
import studies.ericsson.momo.demo.helpers.FeignHelpers;
import studies.ericsson.momo.demo.config.MoMoUserCreationClientConfiguration;
import studies.ericsson.momo.demo.config.api.V10Api;
import studies.ericsson.momo.demo.config.model.ApiUser;
import studies.ericsson.momo.demo.config.model.ApiUserKeyResult;
import studies.ericsson.momo.demo.model.Response;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.fail;
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RequestToPayTest {
    private V10Api v10Api = null;
    private TokenApi tokenApi = null;
    private studies.ericsson.momo.demo.collections.api.V10Api collectionApi = null;
    @Value("${provisioning.url}")
    private String momoUrl = null;
    @Autowired
    private MoMoUserCreationClientConfiguration config = null;
    private  static String userRefId = null;
    private static String transactionRefId = null;
    private static String transactionOngoingRefId = null;
    private String apiKey = null;
    private String providerHostName = "https://webhook.site/85b56bfe-8221-49dd-8168-db09703af462/";

    private String base64String = null;
    private TokenPost200ApplicationJsonResponse jsonAccessToken = null;


    @BeforeAll
    public static void init(){
        userRefId = UUID.randomUUID().toString();
        transactionRefId = UUID.randomUUID().toString();
        transactionOngoingRefId = UUID.randomUUID().toString();
    }
    @BeforeEach
    public void before() {
        log.info(">>> Before Each!");
    }
    @Test
    @Order(1)
    public void verify_CreateApiUser_isSuccessful(){
        log.info("Send API User Creation request to MoMo Sandbox ProviderHostName is " + providerHostName);
        ApiUser localhostApiUser = new ApiUser();
        localhostApiUser.setProviderCallbackHost(providerHostName);
        v10Api = config.feignClientForMoMo(V10Api.class, momoUrl);
        ResponseEntity<Void> response = v10Api.postV10Apiuser(userRefId,localhostApiUser);
        log.info("Value of response  "+ response);
        Assertions.assertEquals(201,response.getStatusCode().value());
    }
    @Test
    @Order(2)
    public void verify_CreateApiKey_isSuccessful(){
        ResponseEntity apiUserKeyResultResponse = v10Api.postV10ApiuserApikey(userRefId);
        log.info("Value of apiUserKeyResultResponse  "+ apiUserKeyResultResponse);
        ApiUserKeyResult keyResult = FeignHelpers.convert(apiUserKeyResultResponse, ApiUserKeyResult.class);
        this.apiKey = keyResult.getApiKey();
        log.info("API User Key Result "+ keyResult );
        Assertions.assertEquals(201,apiUserKeyResultResponse.getStatusCode().value());
    }
    @Test
    @Order(3)
    public void verify_getApiUser_isSuccessful(){
        ResponseEntity getUserDetailsResponse = v10Api.getV10Apiuser(userRefId);
        log.info("Value of getUserDetailsResponse  "+ getUserDetailsResponse);
        Assertions.assertEquals(200,getUserDetailsResponse.getStatusCode().value());
    }
    @Test
    @Order(4)
    public void verify_GetAccessToken_isSuccessful(){
        base64String = Base64.getEncoder().encodeToString((userRefId + ":"+apiKey).getBytes());
        tokenApi = config.feignClientForMoMo(TokenApi.class, momoUrl+"/collection");
        ResponseEntity accessTokenResponse = tokenApi.createAccessToken("Basic "+base64String);
        jsonAccessToken = FeignHelpers.convert(accessTokenResponse, TokenPost200ApplicationJsonResponse.class);
        log.info(jsonAccessToken.toString());
        Assertions.assertNotNull(jsonAccessToken.getAccessToken());
        Assertions.assertNotEquals("",jsonAccessToken.getAccessToken());
    }
    @Test
    @Order(5)
    public void verify_requestToPay_isSuccessful(){
        String bearerToken = "Bearer "+jsonAccessToken.getAccessToken();
        collectionApi = config.feignClientForMoMo(studies.ericsson.momo.demo.collections.api.V10Api.class,momoUrl+"/collection");
        String jsonString = null;
        try {
            jsonString =FeignHelpers.readJsonContentFrom("data/requestToPay.json");
        } catch (IOException e) {
            fail("Unable to read Json String",e);
        }
        RequestToPay request = FeignHelpers.convert(jsonString,RequestToPay.class);
        ResponseEntity requestToPayResponse = collectionApi.requesttoPay(bearerToken, transactionRefId,"sandbox",null,request);
        Response responseBody = FeignHelpers.convert(requestToPayResponse, Response.class);
        log.info("Value of requestToPayResponse  "+ requestToPayResponse + " response Body = " + responseBody);
        Assertions.assertEquals(202,requestToPayResponse.getStatusCode().value());
    }
    @Test
    @Order(6)
    public void verify_getStatus_requestToPay_isSuccessful(){
        String bearerToken = "Bearer "+jsonAccessToken.getAccessToken();
        ResponseEntity responseFromApi = collectionApi.requesttoPayTransactionStatus(transactionRefId,bearerToken,"sandbox");
        RequestToPayResult requestToPayResult = FeignHelpers.convert(responseFromApi,RequestToPayResult.class);
        log.info("Value of requestToPayStatus Response  "+ responseFromApi + " response Body = " + requestToPayResult);
        Assertions.assertEquals(200,responseFromApi.getStatusCode().value());
    }
    @Test
    @Order(7)
    public void verify_requestToPayOnGoing_isSuccessful(){
        String bearerToken = "Bearer "+jsonAccessToken.getAccessToken();
        collectionApi = config.feignClientForMoMo(studies.ericsson.momo.demo.collections.api.V10Api.class,momoUrl+"/collection");
        String jsonString = null;
        try {
            jsonString =FeignHelpers.readJsonContentFrom("data/requestToPay_ongoing.json");
        } catch (IOException e) {
            fail("Unable to read Json String",e);
        }
        RequestToPay request = FeignHelpers.convert(jsonString,RequestToPay.class);
        ResponseEntity requestToPayResponse = collectionApi.requesttoPay(bearerToken, transactionOngoingRefId,"sandbox",null,request);
        Response responseBody = FeignHelpers.convert(requestToPayResponse, Response.class);
        log.info("Value of requestToPayResponse  "+ requestToPayResponse + " response Body = " + responseBody);
        Assertions.assertEquals(202,requestToPayResponse.getStatusCode().value());
    }
    @Test
    @Order(8)
    public void verify_getStatus_requestToPayOnGoing_isSuccessful(){
        String bearerToken = "Bearer "+jsonAccessToken.getAccessToken();
        ResponseEntity responseFromApi = collectionApi.requesttoPayTransactionStatus(transactionOngoingRefId,bearerToken,"sandbox");
        RequestToPayResult requestToPayResult = FeignHelpers.convert(responseFromApi,RequestToPayResult.class);
        log.info("Value of requestToPayStatus Response  "+ responseFromApi + " response Body = " + requestToPayResult);
        Assertions.assertEquals(200,responseFromApi.getStatusCode().value());
    }
}
