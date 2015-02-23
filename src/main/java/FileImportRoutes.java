import model.Order;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.OrderService;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class FileImportRoutes extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( FileImportRoutes.class );

    private final String dirToPoll;
    private final long pollingIntervalInMs;

    public FileImportRoutes( final String dirToPoll, final long pollingIntervalInMs ) {
        this.dirToPoll = dirToPoll;
        this.pollingIntervalInMs = pollingIntervalInMs;
    }

    @Override
    public void configure() throws Exception {
        /**
         * Polls a directory and reads in new XML files containing orders.
         */
        fromF( "file:%s?delay=%s", dirToPoll, pollingIntervalInMs )
                .routeId( "read-order" )
                .log( LoggingLevel.INFO, "Reading file: ${header.CamelFileName}" )
                .unmarshal().jaxb()
                .log( LoggingLevel.INFO, "Unmarshalled order: ${body}" )
                .bean( SaveOrderBean.class );
    }

    public static class SaveOrderBean {

        @Handler
        public void saveOrder( @Body final Order order ) throws NamingException {
            LOG.info( "New order: " + order.getOrderNumber() );
            InitialContext initialContext = new InitialContext();
            OrderService orderService = (OrderService) initialContext.lookup( "ejb/OrderService" );
            orderService.saveOrder( order );
        }
    }
}
