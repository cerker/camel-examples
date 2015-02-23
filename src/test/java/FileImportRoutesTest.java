import model.Order;
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.io.File;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class FileImportRoutesTest {

    private static final Logger LOG = LoggerFactory.getLogger( FileImportRoutesTest.class );

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private final InitialContext initialContext = createMock( InitialContext.class );

    private final OrderService orderService = createMock( OrderService.class );

    private final ModelCamelContext camelContext = new DefaultCamelContext();

    private File importDir;

    private static final String XML_ORDER = "<order><orderNumber>1234</orderNumber></order>";

    @Before
    public void setUp() throws Exception {
        System.setProperty( "java.naming.initial.factory", MockInitialContextFactory.class.getName() );
        expect( initialContext.lookup( anyString() ) ).andReturn( orderService );
        replay( initialContext );

        importDir = tempFolder.newFolder();

        LOG.info( "Polling folder: " + importDir );

        camelContext.addRoutes( new FileImportRoutes( importDir.getAbsolutePath(), 2000L ) );

        camelContext.start();
    }

    @Test
    public void testName() throws Exception {
        final AtomicBoolean orderServiceCalled = new AtomicBoolean();
        orderService.saveOrder( anyObject( Order.class ) );
        expectLastCall().andAnswer( new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                orderServiceCalled.set( true );
                return null;
            }
        } );
        replay( orderService );

        File file = new File( importDir, "order.xml" );
        LOG.info( "Writing file: " + file );
        FileUtils.writeStringToFile( file, XML_ORDER );

        await().timeout( 10, TimeUnit.SECONDS ).untilTrue( orderServiceCalled );

        verify( orderService );
    }

    public class MockInitialContextFactory implements InitialContextFactory {

        @Override
        public Context getInitialContext( Hashtable<?, ?> properties ) throws NamingException {
            return initialContext;
        }
    }
}
