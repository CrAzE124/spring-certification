package rewards.internal.restaurant;


import org.springframework.stereotype.Repository;

/**
 * Loads restaurant aggregates. Called by the reward network to find and reconstitute Restaurant entities from an
 * external form such as a set of RDMS rows.
 * 
 * Objects returned by this repository are guaranteed to be fully-initialized and ready to use.
 */
@Repository
public interface RestaurantRepository extends org.springframework.data.repository.Repository<Restaurant, Long> {
    Restaurant findByNumber(String number);
}
