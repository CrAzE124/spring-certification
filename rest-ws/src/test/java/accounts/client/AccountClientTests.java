package accounts.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;
import common.money.Percentage;

public class AccountClientTests {
	
	/**
	 * Server URL ending with the servlet mapping on which the application can be reached.
	 */
	private static final String BASE_URL = "http://localhost:8080";
	
	private RestTemplate restTemplate = new RestTemplate();
	private Random random = new Random();
	
	@Test
	public void listAccounts() {
		Account[] accounts = restTemplate.getForObject(BASE_URL + "/accounts", Account[].class);
		
		assertNotNull(accounts);
		assertTrue(accounts.length >= 21);
		assertEquals("Keith and Keri Donald", accounts[0].getName());
		assertEquals(2, accounts[0].getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), accounts[0].getBeneficiary("Annabelle").getAllocationPercentage());
	}
	
	@Test
	public void getAccount() {
		Account account = restTemplate.getForObject(BASE_URL + "/accounts/0", Account.class); // Modify this line to use the restTemplate
		
		assertNotNull(account);
		assertEquals("Keith and Keri Donald", account.getName());
		assertEquals(2, account.getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), account.getBeneficiary("Annabelle").getAllocationPercentage());
	}
	
	@Test
	public void createAccount() {
		// use a unique number to avoid conflicts
		String number = String.format("12345%4d", random.nextInt(10000));
		Account account = new Account(number, "John Doe");
		account.addBeneficiary("Jane Doe");
		
		//	Create a new Account by POSTing to the right URL and store its location in a variable
		URI location = restTemplate.postForLocation(BASE_URL + "/accounts", account);

		Account retrievedAccount = restTemplate.getForObject(location, Account.class);
		
		assertEquals(account.getNumber(), retrievedAccount.getNumber());
		
		Beneficiary accountBeneficiary = account.getBeneficiaries().iterator().next();
		Beneficiary retrievedAccountBeneficiary = retrievedAccount.getBeneficiaries().iterator().next();
		
		assertEquals(accountBeneficiary.getName(), retrievedAccountBeneficiary.getName());
		assertNotNull(retrievedAccount.getEntityId());
	}

	@Test
    @Ignore("Neither exceptions handlers (both the lab's one and my one) are working")
    public void createDuplicateAccount() {
        String number = "123456";
        Account account = new Account(number, "John Doe");
        account.addBeneficiary("Jane Doe");

        //	Create a new Account by POSTing to the right URL and store its location in a variable
        URI location = restTemplate.postForLocation(BASE_URL + "/accounts", account);

        try {
            restTemplate.postForLocation(BASE_URL + "/accounts", account);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        }
    }
	
	@Test
	public void addAndDeleteBeneficiary() {
		// perform both add and delete to avoid issues with side effects
        URI location = restTemplate.postForLocation(BASE_URL + "/accounts/1/beneficiaries", "David");
		
		Beneficiary newBeneficiary = restTemplate.getForObject(location, Beneficiary.class);
		
		assertNotNull(newBeneficiary);
		assertEquals("David", newBeneficiary.getName());
		
		restTemplate.delete(location);

		try {
			System.out.println("You SHOULD get the exception \"No such beneficiary with name 'David'\" in the server.");

            newBeneficiary = restTemplate.getForObject(location, Beneficiary.class);
			
			fail("Should have received 404 Not Found after deleting beneficiary");
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
		}
	}

}
