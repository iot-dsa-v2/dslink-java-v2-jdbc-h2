package org.iot.dsa.dslink.jdbc.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.tools.Server;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.jdbc.DBConnectionNode;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;

/**
 * Class designed for handling user-friendly simple local H2 databases.
 *
 * @author James (Juris) Puchin
 * Created on 10/13/2017
 */
@SuppressWarnings("SqlNoDataSourceInspection")
public class ManagedH2DBConnectionNode extends DBConnectionNode {

    public static final String EXT_ACCESS = "Allow Access";
    private static String NO_URL = "No Access";
    private final DSInfo extrnl = getInfo(EXT_ACCESS);
    private Server server;

    @SuppressWarnings("unused")
    public ManagedH2DBConnectionNode() {

    }

    ManagedH2DBConnectionNode(DSMap params) {
        super(params);
    }

    @Override
    protected void checkConfig() {
        if (usr_name.getElement().toString().isEmpty()) {
            throw new IllegalStateException("Empty username");
        }
        if (db_name.getElement().toString().isEmpty()) {
            throw new IllegalStateException("Empty db name");
        }
    }

    @Override
    protected void closeConnections() {
        stopTCPServer();
    }

    @Override
    protected void createDatabaseConnection() {
        if (!canConnect()) {
            return;
        }
        if (extrnl.getValue().toElement().toBoolean()) {
            startTCPServer();
        }
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(EXT_ACCESS, DSBool.make(false));
        //declareDefault(SHOW_TABLES, makeShowTablesAction());
    }

    @Override
    protected ActionResults edit(DSMap parameters) {
        DSElement newUsr = parameters.get(DB_USER);
        DSElement newPass = parameters.get(DB_PASSWORD);
        //noinspection UnusedAssignment
        String newUsrStr = null;
        String curUserStr = usr_name.getValue().toString();

        Connection data = null;
        Statement chg_usr = null;
        Statement chg_pass = null;

        try {
            data = getConnection();
            try {
                if (newUsr != null) {
                    newUsrStr = newUsr.toString();
                    if (!newUsrStr.toUpperCase().equals(curUserStr.toUpperCase())) {
                        chg_usr = data.createStatement();
                        chg_usr.execute("ALTER USER " + curUserStr + " RENAME TO " + newUsrStr);
                        data.commit();
                    }
                } else {
                    newUsrStr = curUserStr;
                }

                if (newPass == null) {
                    newPass = DSElement.make(getCurPass());
                }

                //Password must always be re-set after change of user name (JDBC quirk)
                chg_pass = data.createStatement();
                chg_pass.execute(
                        "ALTER USER " + newUsrStr + " SET PASSWORD '" + newPass.toString() + "'");
                data.commit();

            } catch (Exception ex) {
                warn("User/Pass change error:", ex);
            }
        } catch (SQLException e) {
            warn("Failed to get connection.", e);
            connDown(e.getMessage());
        } finally {
            cleanClose(null, chg_pass, data, this);
            cleanClose(null, chg_usr, data, this);
        }

        setParameters(parameters);
        testConnection();
        DSMainNode par = (DSMainNode) getParent();
        return null;
    }

    @Override
    protected Connection getConnection() throws SQLException {
        try {
            updateServerURL();
            return DriverManager.getConnection("jdbc:h2:" + getCurDBName(),
                                               usr_name.getValue().toString(),
                                               getCurPass());
        } catch (Exception x) {
            warn("Failed to login:", x);
        }
        return null;
    }

    @Override
    protected void onChildChanged(DSInfo info) {
        super.onChildChanged(info);
        if (info.getName().equals(EXT_ACCESS)) {
            if (info.getElement().toBoolean()) {
                startTCPServer();
            } else {
                stopTCPServer();
            }
        }
    }

    @Override
    protected void onStable() {
        super.onStable();
        if (extrnl.getElement().toBoolean()) {
            startTCPServer();
        } else {
            stopTCPServer();
        }
    }

    private String getCurDBName() {
        return "./db/" + db_name.getValue().toString();
    }

    private String getServerURL() {
        return (server != null) ? "jdbc:h2:" + server.getURL() + "/" + getCurDBName() : NO_URL;
    }

    private void startTCPServer() {
        try {
            server = Server.createTcpServer("-tcpAllowOthers").start();
        } catch (SQLException e) {
            warn("Cannot start Web Server", e);
        }
        updateServerURL();
    }

    private void stopTCPServer() {
        if (server != null) {
            server.stop();
        }
        server = null;
        put(db_url, DSElement.make(NO_URL));
    }

    private void updateServerURL() {
        put(db_url, DSElement.make(getServerURL()));
    }

}
