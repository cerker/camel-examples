import model.Order;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.commons.io.FileUtils;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.OrderService;

import javax.naming.NamingException;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class FileImportRoutesTest {

    private static final Logger LOG = LoggerFactory.getLogger( FileImportRoutesTest.class );

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private final OrderService orderService = createMock( OrderService.class );

    private File importDir;

    private static final String XML_ORDER = "<order><orderNumber>1234</orderNumber></order>";

    @Before
    public void setUp() throws Exception {
        // create temp dir which will automatically be deleted after the test run
        importDir = tempFolder.newFolder();

        // create CamelContext and add routes
        ModelCamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes( new FileImportRoutes( importDir.getAbsolutePath(), 2000L ) );

        // create replacement for the SaveOrderBean to be able to inject a Mock for OrderService
        final FileImportRoutes.SaveOrderBean saveOrderBean = new FileImportRoutes.SaveOrderBean() {
            @Override
            protected OrderService getOrderService() throws NamingException {
                return orderService;
            }
        };
        camelContext.getRouteDefinition( "read-order" ).adviceWith( camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                // replace the original SaveOrderBean in the route
                weaveById( "save-order-bean" ).replace().bean( saveOrderBean );
            }
        } );
        // start CamelContext
        camelContext.start();
    }

    @Test
    public void savesOrder() throws Exception {

        orderService.saveOrder( anyObject( Order.class ) );
        final AtomicBoolean saveOrderCalled = new AtomicBoolean();
        // set saveOrderCalled to true as soon as OrderService.saveOrder() is called; thus we are notified when we go on during the test, see below
        expectLastCall().andAnswer( new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                saveOrderCalled.set( true );
                return null;
            }
        } );
        replay( orderService );

        // write a new order to be imported by the route
        File file = new File( importDir, "order.xml" );
        LOG.info( "Writing file: " + file );
        FileUtils.writeStringToFile( file, XML_ORDER );

        // waits at most 10 Seconds (when something goes wrong and saveOrder() is not called, else stops waiting when saveOrderCalled is set to true
        await().timeout( 10, TimeUnit.SECONDS ).untilTrue( saveOrderCalled );

        // verify that OrderService.saveOrder() was called
        verify( orderService );
    }
}
