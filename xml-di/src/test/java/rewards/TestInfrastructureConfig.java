package rewards;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@Configuration
@ImportResource({"classpath:config/rewards-config.xml", "classpath:rewards/test-infrastructure-config.xml"})
public class TestInfrastructureConfig {
}
