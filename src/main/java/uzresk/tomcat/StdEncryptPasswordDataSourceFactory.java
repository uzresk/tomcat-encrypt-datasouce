package uzresk.tomcat;

import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.XADataSource;

import uzresk.crypto.Encryptor;
import uzresk.crypto.StdEncryptor;

public class StdEncryptPasswordDataSourceFactory extends DataSourceFactory {

	// private static final Log log = LogFactory
	// .getLog(StdEncryptPasswordDataSourceFactory.class);

	private Encryptor encryptor = null;

	public StdEncryptPasswordDataSourceFactory() {
		encryptor = new StdEncryptor();
	}

	@Override
	public DataSource createDataSource(Properties properties, Context context,
			boolean XA) throws SQLException {

		PoolConfiguration poolProperties = StdEncryptPasswordDataSourceFactory
				.parsePoolProperties(properties);
		poolProperties.setPassword(encryptor.decrypt(poolProperties
				.getPassword()));

		if (poolProperties.getDataSourceJNDI() != null
				&& poolProperties.getDataSource() == null) {
			performJNDILookup(context, poolProperties);
		}
		org.apache.tomcat.jdbc.pool.DataSource dataSource = XA ? new XADataSource(
				poolProperties) : new org.apache.tomcat.jdbc.pool.DataSource(
				poolProperties);
		dataSource.createPool();

		return dataSource;
	}
}
