package il.ac.technion.cs.eldery.system.userInformation;

import org.junit.Test;

/** @author Inbal Zukerman
 * @since Dec 28, 2016 */
public class ContactTest {

    private final Contact contactA = new Contact("123", "Alon", "0508080123", "alon@gmail.com");

    @Test public void initializationTest() {

        assert contactA.getId() == "123";
        assert contactA.getName() == "Alon";
    }

    @Test public void phoneNumberTest() {
        assert contactA.getPhoneNumber() == "0508080123";
        contactA.setPhoneNumber("026798080");
        assert contactA.getPhoneNumber() == "026798080";
    }

    @Test public void emailAddressTest() {
        assert contactA.getEmailAddress() == "alon@gmail.com";
        contactA.setEmailAddress("alon100@gmail.com");
        assert contactA.getEmailAddress() == "alon100@gmail.com";
    }

}
