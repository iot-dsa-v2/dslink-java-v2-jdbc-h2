package org.iot.dsa.dslink.jdbc.h2;

import org.iot.dsa.dslink.jdbc.AbstractMainNode;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

public class MainNode extends AbstractMainNode {

    @Override
    protected String getHelpUrl() {
        return "https://github.com/iot-dsa-v2/dslink-java-v2-jdbc-h2";
    }

    @Override
    protected DSAction makeNewConnectionAction() {
        DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                return ((MainNode) target.get()).createNewDatabase(invocation.getParameters());
            }
        };
        act.addParameter(DB_NAME, DSValueType.STRING, null);
        act.addParameter(DB_USER, DSValueType.STRING, null);
        act.addParameter(DB_PASSWORD, DSValueType.STRING, null).setEditor("password");
        return act;
    }

    private ActionResult createNewDatabase(DSMap parameters) {
        parameters.put(DRIVER, DSElement.make("org.h2.Driver"));
        parameters.put(DB_URL, DSElement.make("Not Started"));
        DSNode nextDB = new ManagedH2DBConnectionNode(parameters);
        add(parameters.getString(DB_NAME), nextDB);
        return null;
    }

}
