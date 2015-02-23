package model;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

import static org.fest.assertions.Assertions.assertThat;

public class UnmarshalOrderTest {

    @Test
    public void unmarshal() throws Exception {
        String xml = "<order><orderNumber>1234</orderNumber></order>";

        JAXBContext jaxbContext = JAXBContext.newInstance( Order.class );
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Order order = (Order) unmarshaller.unmarshal( new StringReader( xml ) );

        assertThat( order.getOrderNumber() ).isEqualTo( "1234" );
    }
}
