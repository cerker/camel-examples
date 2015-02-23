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
         * See Camel file component: http://camel.apache.org/file2.html
         */
        fromF( "file:%s?delay=%s&antInclude=*.xml", dirToPoll, pollingIntervalInMs )
                .routeId( "read-order" )
                        // log header parameter CamelFileName, set by the file producer
                .log( LoggingLevel.INFO, "Reading file: ${header.CamelFileName}" )
                        // before: InputStream, after logging, would not point to the beginning, thus convert to String
                .convertBodyTo( String.class )
                        // log the message body (now the file content)
                .log( LoggingLevel.INFO, "Content: ${body}" )
                        // unmarshal from XML to Java using JAXB
                .unmarshal().jaxb( Order.class.getPackage().getName() )
                // log the message body (now instance of Order)
                .log( LoggingLevel.INFO, "Unmarshalled order: ${body}" )
                        // call bean
                .bean( SaveOrderBean.class ).id( "save-order-bean" );
    }

    public static class SaveOrderBean {

        @Handler
        public void saveOrder( @Body final Order order ) throws NamingException {
            LOG.info( "New order: " + order.getOrderNumber() );
            OrderService orderService = getOrderService();
            orderService.saveOrder( order );
        }

        protected OrderService getOrderService() throws NamingException {
            // we are in the container, look up Service (EJB) in JNDI
            InitialContext initialContext = new InitialContext();
            // need to adjust service name to actual JNDI name
            return (OrderService) initialContext.lookup( "orderService" );
        }
    }
}
