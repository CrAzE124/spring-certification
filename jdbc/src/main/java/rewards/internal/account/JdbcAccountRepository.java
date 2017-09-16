package rewards.internal.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

import common.money.MonetaryAmount;
import common.money.Percentage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

/**
 * Loads accounts from a data source using the JDBC API.
 */

public class JdbcAccountRepository implements AccountRepository {
	private JdbcTemplate jdbcTemplate;

	public JdbcAccountRepository(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private class AccountExtractor implements ResultSetExtractor<Account> {
		@Override
		public Account extractData(ResultSet rs) throws SQLException, DataAccessException {
			Account account = null;
			while (rs.next()) {
				if (account == null) {
					String number = rs.getString("ACCOUNT_NUMBER");
					String name = rs.getString("ACCOUNT_NAME");
					account = new Account(number, name);
					// set internal entity identifier (primary key)
					account.setEntityId(rs.getLong("ID"));
				}
				account.restoreBeneficiary(mapBeneficiary(rs));
			}

			if (account == null) {
				// no rows returned - throw an empty result exception
				throw new EmptyResultDataAccessException(1);
			}

			return account;
		}

		/**
		 * Maps the beneficiary columns in a single row to an AllocatedBeneficiary object.
		 *
		 * @param rs the result set with its cursor positioned at the current row
		 * @return an allocated beneficiary
		 * @throws SQLException an exception occurred extracting data from the result set
		 */
		private Beneficiary mapBeneficiary(ResultSet rs) throws SQLException {
			String name = rs.getString("BENEFICIARY_NAME");
			MonetaryAmount savings = MonetaryAmount.valueOf(rs.getString("BENEFICIARY_SAVINGS"));
			Percentage allocationPercentage = Percentage.valueOf(rs.getString("BENEFICIARY_ALLOCATION_PERCENTAGE"));

			return new Beneficiary(name, allocationPercentage, savings);
		}
	}

	public Account findByCreditCard(String creditCardNumber) {
		String sql = "select a.ID as ID, a.NUMBER as ACCOUNT_NUMBER, a.NAME as ACCOUNT_NAME, c.NUMBER as CREDIT_CARD_NUMBER, " +
			"	b.NAME as BENEFICIARY_NAME, b.ALLOCATION_PERCENTAGE as BENEFICIARY_ALLOCATION_PERCENTAGE, b.SAVINGS as BENEFICIARY_SAVINGS " +
			"from T_ACCOUNT a, T_ACCOUNT_CREDIT_CARD c " +
			"left outer join T_ACCOUNT_BENEFICIARY b " +
			"on a.ID = b.ACCOUNT_ID " +
			"where c.ACCOUNT_ID = a.ID and c.NUMBER = ?";

		return jdbcTemplate.query(sql, new AccountExtractor(), creditCardNumber);
	}

	public void updateBeneficiaries(Account account) {
		String sql = "update T_ACCOUNT_BENEFICIARY SET SAVINGS = ? where ACCOUNT_ID = ? and NAME = ?";

		jdbcTemplate.batchUpdate(
				sql,
				account.getBeneficiaries()
						.stream()
						.map(beneficiary -> new Object[]{
								beneficiary.getSavings().asBigDecimal(),
								account.getEntityId(),
								beneficiary.getName()
						})
						.collect(Collectors.toList())
		);
	}
}
