package accounts.web;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import accounts.AccountManager;
import common.money.Percentage;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

/**
 * A controller handling requests for CRUD operations on Accounts and their
 * Beneficiaries.
 */
@Controller
public class AccountController {

	private final Logger logger = Logger.getLogger(getClass());

	private AccountManager accountManager;

	/**
	 * Creates a new AccountController with a given account manager.
	 */
	@Autowired
	public AccountController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	/**
	 * Provide a list of all accounts.
	 */
	@GetMapping("/accounts")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Account> accountSummary() {
		return accountManager.getAllAccounts();
	}

	/**
	 * Provide the details of an account with the given id.
	 */
    @GetMapping("/accounts/{id}")
    @ResponseStatus(HttpStatus.OK)
	public @ResponseBody Account accountDetails(@PathVariable int id) {
		return retrieveAccount(id);
	}

	/**
	 * Creates a new Account, setting its URL as the Location header on the
	 * response.
	 */
    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Void> createAccount(@RequestBody Account newAccount) {
		// Saving the account also sets its entity Id
        Account account = accountManager.save(newAccount);

        // Return a ResponseEntity - it will be used to build the
        // HttpServletResponse.
        return entityWithLocation(account.getEntityId());
	}

	/**
	 * Return a response with the location of the new resource. It's URL is
	 * assumed to be a child of the URL just received.
	 * <p>
	 * Suppose we have just received an incoming URL of, say,
	 * <code>http://localhost:8080/accounts</code> and <code>resourceId</code>
	 * is "1111". Then the URL of the new resource will be
	 * <code>http://localhost:8080/accounts/1111</code>.
	 * 
	 * @param resourceId
	 *            Is of the new resource.
	 * @return
	 */
	private ResponseEntity<Void> entityWithLocation(Object resourceId) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(resourceId)
                .toUri();

        return ResponseEntity.created(location).build();
	}

	/**
	 * Returns the Beneficiary with the given name for the Account with the
	 * given id.
	 */
	@GetMapping(value = "/accounts/{accountId}/beneficiaries/{beneficiaryName}")
	public @ResponseBody Beneficiary getBeneficiary(@PathVariable("accountId") int accountId,
			@PathVariable("beneficiaryName") String beneficiaryName) {
		return retrieveAccount(accountId).getBeneficiary(beneficiaryName);
	}

	/**
	 * Adds a Beneficiary with the given name to the Account with the given id,
	 * setting its URL as the Location header on the response.
	 */
    @PostMapping("/accounts/{accountId}/beneficiaries")
    @ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Void> addBeneficiary(@PathVariable long accountId, @RequestBody String beneficiaryName) {
		accountManager.addBeneficiary(accountId, beneficiaryName);

        return entityWithLocation(beneficiaryName);
	}

	/**
	 * Removes the Beneficiary with the given name from the Account with the
	 * given id.
	 */
    @DeleteMapping("/accounts/{accountId}/beneficiaries/{beneficiaryName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeBeneficiary(@PathVariable long accountId, @PathVariable String beneficiaryName) {
		Account account = accountManager.getAccount(accountId);
		Beneficiary b = account.getBeneficiary(beneficiaryName);

		// We ought to reset the allocation percentages, but for now we won't
		// bother. If we are removing the only beneficiary or the beneficiary
		// has an allocation of zero we don't need to worry. Otherwise, throw an
		// exception.
		if (account.getBeneficiaries().size() != 1 && (!b.getAllocationPercentage().equals(Percentage.zero()))) {
			// The solution has the missing logic, if you are interested.
			throw new RuntimeException("Logic to rebalance Beneficiaries not defined.");
		}

		accountManager.removeBeneficiary(accountId, beneficiaryName, new HashMap<String, Percentage>());
	}

	/**
	 * Maps IllegalArgumentExceptions to a 404 Not Found HTTP status code.
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({ IllegalArgumentException.class })
	public void handleNotFound(Exception ex) {
		logger.error("Exception is: ", ex);
		// just return empty 404
	}

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleIntegrityError(ConstraintViolationException e) {
        logger.error(e.getMessage());
    }
	
	/**
	 * Finds the Account with the given id, throwing an IllegalArgumentException
	 * if there is no such Account.
	 */
	private Account retrieveAccount(long accountId) throws IllegalArgumentException {
		Account account = accountManager.getAccount(accountId);
		if (account == null) {
			throw new IllegalArgumentException("No such account with id " + accountId);
		}
		return account;
	}

}
