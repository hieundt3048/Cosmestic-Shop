package com.cosmeticshop.cosmetic.Config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Đảm bảo check constraint cũ của SQL Server trên cột orders.status
 * cho phép toàn bộ ordinal hiện tại của enum trạng thái đơn hàng.
 *
 * Một số database legacy vẫn còn constraint chỉ cho phép 0..4,
 * khiến thao tác chuyển trạng thái của nhân viên sang REFUNDED/PACKING bị lỗi.
 */
@Component
public class OrderStatusConstraintMigration implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusConstraintMigration.class);

    private final DataSource dataSource;

    public OrderStatusConstraintMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            if (!isSqlServer(connection)) {
                return;
            }

            if (!hasLegacyStatusConstraint(connection)) {
                return;
            }

            dropStatusConstraints(connection);
            createStatusConstraint(connection);
            logger.info("Migrated orders.status check constraint to allow 0..6");
        } catch (Exception ex) {
            // Không chặn quá trình khởi động; chỉ ghi log để vận hành kiểm tra.
            logger.warn("Could not migrate orders.status check constraint", ex);
        }
    }

    private boolean isSqlServer(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        String productName = metadata.getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("sql server");
    }

    private boolean hasLegacyStatusConstraint(Connection connection) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM sys.check_constraints
                WHERE parent_object_id = OBJECT_ID('dbo.orders')
                  AND definition LIKE '%[status]%'
                  AND definition NOT LIKE '%<=(6)%'
                """;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                return false;
            }
            return resultSet.getInt(1) > 0;
        }
    }

    private void dropStatusConstraints(Connection connection) throws SQLException {
        String find = """
                SELECT name
                FROM sys.check_constraints
                WHERE parent_object_id = OBJECT_ID('dbo.orders')
                  AND definition LIKE '%[status]%'
                """;

        try (Statement query = connection.createStatement();
             ResultSet constraints = query.executeQuery(find)) {
            while (constraints.next()) {
                String constraintName = constraints.getString("name");
                try (Statement drop = connection.createStatement()) {
                    drop.execute("ALTER TABLE dbo.orders DROP CONSTRAINT [" + constraintName + "]");
                }
            }
        }
    }

    private void createStatusConstraint(Connection connection) throws SQLException {
        String create = """
                ALTER TABLE dbo.orders
                WITH CHECK ADD CONSTRAINT CK_orders_status_valid
                CHECK ([status] >= 0 AND [status] <= 6)
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(create);
        }
    }
}
