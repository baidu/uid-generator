package jp.ne.paypay.uid.worker.dao;

import jp.ne.paypay.uid.worker.entity.WorkerNodeEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class WorkerNodeDAOImpl implements WorkerNodeDAO {
    private static final Logger LOGGER = LogManager.getLogger(WorkerNodeDAOImpl.class);

    private final Connection connection;

    public WorkerNodeDAOImpl(DataSource dataSource) throws SQLException {
        connection = dataSource.getConnection();
    }

    @Override
    public WorkerNodeEntity getWorkerNodeByHostPort(String host, String port) {
        if (connection == null) {
            return null;
        }

        ResultSet resultSet = getWorkerNodeByHostPort(connection, host, port);


        if (resultSet == null) {
            return null;
        }

        return getWorkerNode(resultSet);

    }

    @Override
    public void addWorkerNode(WorkerNodeEntity workerNodeEntity) {
        LOGGER.info("WorkerNodeDAOImpl -  addWorkerNode");
        System.out.println("WorkerNodeDAOImpl -  addWorkerNode");
        addWorkerNode(
            connection,
            workerNodeEntity.getHostName(),
            workerNodeEntity.getPort(),
            workerNodeEntity.getType(),
            workerNodeEntity.getLaunchDate()
        );

    }


    private ResultSet getWorkerNodeByHostPort(Connection con, String host, String port) {
        ResultSet result = null;
        String query = "SELECT ID, HOST_NAME, PORT, TYPE, LAUNCH_DATE, MODIFIED, CREATED FROM WORKER_NODE WHERE HOST_NAME = ? AND PORT = ?";
        try (PreparedStatement selectStatement = con.prepareStatement(query)) {
            selectStatement.setString(1, host);
            selectStatement.setString(2, port);
            result = selectStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static WorkerNodeEntity getWorkerNode(ResultSet resultSet) {
        try {
            while (resultSet.next()) {
                long id = resultSet.getLong("ID");
                String hostName = resultSet.getString("HOST_NAME");
                String port = resultSet.getString("PORT");
                int type = resultSet.getInt("TYPE");
                Date launchDate = resultSet.getDate("LAUNCH_DATE");
                Date modified = resultSet.getDate("MODIFIED");
                Date created = resultSet.getDate("CREATED");

                WorkerNodeEntity workerNodeEntity = new WorkerNodeEntity();
                workerNodeEntity.setId(id);
                workerNodeEntity.setHostName(hostName);
                workerNodeEntity.setPort(port);
                workerNodeEntity.setType(type);
                workerNodeEntity.setLaunchDateDate(launchDate);
                workerNodeEntity.setModified(modified);
                workerNodeEntity.setCreated(created);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addWorkerNode(Connection connection, String hostName, String port, int type, Date launchDate) {

        String query = "INSERT INTO WORKER_NODE (HOST_NAME, PORT, TYPE, LAUNCH_DATE, MODIFIED, CREATED) VALUES (?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement insertStatement = connection.prepareStatement(query)) {
            insertStatement.setString(1, hostName);
            insertStatement.setString(2, port);
            insertStatement.setInt(3, type);
            insertStatement.setDate(4, new java.sql.Date(launchDate.getTime()));

            insertStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
