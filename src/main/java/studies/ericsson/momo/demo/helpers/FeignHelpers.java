package studies.ericsson.momo.demo.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import studies.ericsson.momo.demo.model.Response;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public class FeignHelpers {
    public static ResponseEntity<studies.ericsson.momo.demo.model.Response> userCreationResponseConvertor(InvocationContext feignResponseContext){
        feign.Response r =feignResponseContext.response();
        ResponseEntity<studies.ericsson.momo.demo.model.Response> responseEntity = null;
        String body = null;
        try {
            body = new BufferedReader(new InputStreamReader(r.body().asInputStream())).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Error while reading the body ", e);
        }
        Response localResponse = new studies.ericsson.momo.demo.model.Response();
        localResponse.setStatus(feignResponseContext.response().status());
        localResponse.setBody(body);
        localResponse.setReason(r.reason());
        responseEntity = ResponseEntity.status(feignResponseContext.response().status()).body(localResponse);
        return responseEntity;
    }

    public static <T> T  convert(ResponseEntity response, Class<T> clazz){
        Object o = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        if(o != null) {
            try {
                String body = ((Response)o).getBody();
                if(body != null && !body.isBlank() && !body.isEmpty()) {
                    return mapper.readValue(((Response) o).getBody(), clazz);
                }
            } catch (JsonProcessingException e) {
                log.error("Error Parsing Json ",e);
            }
        }
        return null;
    }
    public static <T> T  convert(String jsonString, Class<T> clazz){
        ObjectMapper mapper = new ObjectMapper();
        if(jsonString != null) {
            try {
                return mapper.readValue(jsonString, clazz);
            } catch (JsonProcessingException e) {
                log.error("Error Parsing Json ",e);
            }
        }
        return null;
    }
    public static String readJsonContentFrom(String jsonFileName) throws IOException{
        return new String(FeignHelpers.class
                .getClassLoader()
                .getResourceAsStream(jsonFileName)
                .readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
