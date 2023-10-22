package studies.ericsson.momo.demo.config;

import feign.Feign;
import feign.RequestInterceptor;
import feign.ResponseInterceptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Configuration;
import studies.ericsson.momo.demo.helpers.FeignHelpers;

@Configuration
@Slf4j
public class MoMoUserCreationClientConfiguration {
    @Value("${provisioning.subscription.key}")
    private String provisioningSubcriptionKey = null;
    public <T> T feignClientForMoMo(Class<T> clazz, String url){
        return Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new feign.jackson.JacksonEncoder())
                .decoder(new feign.jackson.JacksonDecoder())
                .requestInterceptor(getRequestInterceptor())
                .responseInterceptor(getResponseInterceptor())
                .target(clazz,url);
    }

    private RequestInterceptor getRequestInterceptor(){
        return requestTemplate -> {
            requestTemplate.header("Ocp-Apim-Subscription-Key",provisioningSubcriptionKey);
        };
    }
    private ResponseInterceptor getResponseInterceptor(){
        return responseTemplate -> {
            return FeignHelpers.userCreationResponseConvertor(responseTemplate);
        };
    }
}
