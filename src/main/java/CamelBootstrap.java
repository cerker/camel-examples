import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger( CamelBootstrap.class );

    private static final String DIR_TO_POLL = "/Users/carsten/tmp/camel-test";

    private static final long POLLING_INTERVAL_IN_MS = 20000L;

    private DefaultCamelContext camelContext;

    public void startCamel() throws Exception {
        LOG.info( "Starting Camel" );
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes( new FileImportRoutes( DIR_TO_POLL, POLLING_INTERVAL_IN_MS ) );
        camelContext.start();
    }

    public void stopCamel() throws Exception {
        LOG.info( "Stopping Camel" );
        camelContext.stop();
    }
}
