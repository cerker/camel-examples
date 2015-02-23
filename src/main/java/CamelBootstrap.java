import org.apache.camel.impl.DefaultCamelContext;

public class CamelBootstrap {

    private static final String DIR_TO_POLL = "/Users/carsten/tmp/camel-test";

    private static final long POLLING_INTERVAL_IN_MS = 20000L;

    private DefaultCamelContext camelContext;

    public void startCamel() throws Exception {
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes( new FileImportRoutes( DIR_TO_POLL, POLLING_INTERVAL_IN_MS ) );
    }

    public void stopCamel() throws Exception {
        camelContext.stop();
    }
}
