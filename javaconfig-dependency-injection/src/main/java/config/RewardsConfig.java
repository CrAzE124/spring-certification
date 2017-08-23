package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import rewards.RewardNetwork;
import rewards.internal.RewardNetworkImpl;
import rewards.internal.account.JdbcAccountRepository;
import rewards.internal.restaurant.JdbcRestaurantRepository;
import rewards.internal.reward.JdbcRewardRepository;

import javax.sql.DataSource;

@Configuration
public class RewardsConfig {
    private final DataSource dataSource;

    @Autowired
    public RewardsConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public RewardNetwork rewardNetwork() {
        return new RewardNetworkImpl(
                accountRepository(),
                restaurantRepository(),
                rewardRepository()
        );
    }

    @Bean
    public JdbcAccountRepository accountRepository() {
        JdbcAccountRepository accountRepository = new JdbcAccountRepository();
        accountRepository.setDataSource(dataSource);

        return accountRepository;
    }

    @Bean
    public JdbcRestaurantRepository restaurantRepository() {
        JdbcRestaurantRepository restaurantRepository = new JdbcRestaurantRepository();
        restaurantRepository.setDataSource(dataSource);

        return restaurantRepository;
    }

    @Bean
    public JdbcRewardRepository rewardRepository() {
        JdbcRewardRepository rewardRepository = new JdbcRewardRepository();
        rewardRepository.setDataSource(dataSource);

        return rewardRepository;
    }
}
