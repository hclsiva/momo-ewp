package studies.ericsson.momo.demo.model;


import lombok.Getter;
import lombok.Setter;

public class Response {
    @Getter
    @Setter
    private int status;
    @Getter
    @Setter
    private String body;
    @Getter
    @Setter
    private String reason;
}
