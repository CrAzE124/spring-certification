package rewards.internal.account;


import org.springframework.stereotype.Repository;

/**
 * Loads account aggregates. Called by the reward network to find and
 * reconstitute Account entities from an external form such as a set of RDMS
 * rows.
 * 
 * Objects returned by this repository are guaranteed to be fully initialized
 * and ready to use.
 */
@Repository
public interface AccountRepository extends org.springframework.data.repository.Repository<Account, Long> {
    Account findByCreditCardNumber(String creditCardNumber);
}
