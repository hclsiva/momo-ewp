package studies.ericsson.momo.demo.old;

public enum Environment {
    SANDBOX("sandbox"), PRODUCTION("production");
    private String env;

    Environment(String env) {
        this.env = env;
    }

    public String getEnv() {
        return env;
    }
}
