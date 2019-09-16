package org.iot.dsa.dslink.jdbc.h2;

import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.jdbc.AbstractMainNode;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

public class MainNode extends AbstractMainNode {

    @Override
    protected String getHelpUrl() {
        return "https://github.com/iot-dsa-v2/dslink-java-v2-jdbc-h2";
    }

    @Override
    protected DSAction makeNewConnectionAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                return ((MainNode) req.getTarget()).createNewDatabase(req.getParameters());
            }
        };
        act.addParameter(DB_NAME, DSString.NULL, null);
        act.addParameter(DB_USER, DSString.NULL, null);
        act.addParameter(DB_PASSWORD, DSString.NULL, null).setEditor("password");
        return act;
    }

    private ActionResults createNewDatabase(DSMap parameters) {
        parameters.put(DRIVER, DSElement.make("org.h2.Driver"));
        parameters.put(DB_URL, DSElement.make("Not Started"));
        DSNode nextDB = new ManagedH2DBConnectionNode(parameters);
        add(parameters.getString(DB_NAME), nextDB);
        return null;
    }

}
