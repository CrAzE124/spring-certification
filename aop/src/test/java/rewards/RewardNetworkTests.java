package rewards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import common.money.MonetaryAmount;

/**
 * A system test that verifies the components of the RewardNetwork application work together to reward for dining
 * successfully. Uses Spring to bootstrap the application for use in a test environment.
 */
@ContextConfiguration(classes={SystemTestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class RewardNetworkTests {

	// Convenient class from Spring Boot that captures console output
	@Rule
	public OutputCapture capture = new OutputCapture();

	/**
	 * The object being tested.
	 */
	@Autowired
	private RewardNetwork rewardNetwork;

	@Test
	public void testRewardForDining() {
		// create a new dining of 100.00 charged to credit card '1234123412341234' by merchant '123457890' as test input
		Dining dining = Dining.createDining("100.00", "1234123412341234", "1234567890");

		// call the 'rewardNetwork' to test its rewardAccountFor(Dining) method
		RewardConfirmation confirmation = rewardNetwork.rewardAccountFor(dining);

		// assert the expected reward confirmation results
		assertNotNull(confirmation);
		assertNotNull(confirmation.getConfirmationNumber());

		// assert an account contribution was made
		AccountContribution contribution = confirmation.getAccountContribution();
		assertNotNull(contribution);

		// the contribution account number should be '123456789'
		assertEquals("123456789", contribution.getAccountNumber());

		// the total contribution amount should be 8.00 (8% of 100.00)
		assertEquals(MonetaryAmount.valueOf("8.00"), contribution.getAmount());

		// the total contribution amount should have been split into 2 distributions
		assertEquals(2, contribution.getDistributions().size());

		// each distribution should be 4.00 (as both have a 50% allocation)
		assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Annabelle").getAmount());
		assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Corgan").getAmount());
		
		int expectedMatches = 4;
		checkConsoleOutput(expectedMatches);
		
		// TODO-09: Save all your work, and change the expected matches value above from 2 to 4.
		// Rerun the RewardNetworkTests.  It should pass, and you should now see FOUR lines of
		// console output from the LoggingAspect.
	}
	
	/**
	 * Not only must the code run, but the LoggingAspect should generate logging
	 * output to the console.
	 */
	private void checkConsoleOutput(int expectedMatches) {
		// No console output until we implement the LoggingAspect
		if (!TestConstants.CHECK_CONSOLE_OUTPUT)
			return;
		
		// AOP VERIFICATION
		// Expecting 4 lines of output from the LoggingAspect to console
		String[] consoleOutput = capture.toString().split("\n");
		int matches = 0;

		for (String line : consoleOutput) {
			if (line.contains("rewards.internal.aspects.LoggingAspect")) {
				if (line.contains("Before") && line.contains("JdbcAccountRepository") &&  line.contains("findByCreditCard"))
					// Before aspect invoked for
					// JdbcAccountRepository.findByCreditCard
					matches++;
				else if (line.contains("Before") && line.contains("JdbcRestaurantRepository") &&  line.contains("findByMerchantNumber"))
					// Before aspect invoked for
					// JdbcRestaurantRepository.findByMerchantNumber
					matches++;
				else if (line.contains("Around") && line.contains("AccountRepository") &&  line.contains("updateBeneficiaries"))
					// Around aspect invoked for
					// AccountRepository.updateBeneficiaries
					matches++;
				else if (line.contains("Around") && line.contains("RewardRepository") &&  line.contains("updateReward"))
					// Around aspect invoked for
					// RewardRepository.updateReward
					matches++;
			}
		}

		assertEquals(expectedMatches, matches);
	}
}
