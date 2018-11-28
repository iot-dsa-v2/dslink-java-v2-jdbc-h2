package org.iot.dsa.dslink.jdbc.h2;

import org.iot.dsa.dslink.jdbc.AbstractMainNode;
import org.iot.dsa.dslink.jdbc.JDBCv2Helpers;
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
	protected DSAction makeAddDatabaseAction() {
		DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                return ((MainNode) target.get()).createNewDatabase(invocation.getParameters());
            }
        };
        act.addParameter(JDBCv2Helpers.DB_NAME, DSValueType.STRING, null);
        act.addParameter(JDBCv2Helpers.DB_USER, DSValueType.STRING, null);
        act.addParameter(JDBCv2Helpers.DB_PASSWORD, DSValueType.STRING, null).setEditor("password");
        //TODO: add default timeout/poolable options
        //action.addParameter(new Parameter(JdbcConstants.DEFAULT_TIMEOUT, ValueType.NUMBER));
        //action.addParameter(new Parameter(JdbcConstants.POOLABLE, ValueType.BOOL, new Value(true)));
        return act;
	}
	
	@Override
	protected String getHelpUrl() {
		return "https://github.com/iot-dsa-v2/dslink-java-v2-jdbc-h2";
	}
	
	private ActionResult createNewDatabase(DSMap parameters) {
        parameters.put(JDBCv2Helpers.DRIVER, DSElement.make("org.h2.Driver"));
        parameters.put(JDBCv2Helpers.DB_URL, DSElement.make("Not Started"));
        DSNode nextDB = new ManagedH2DBConnectionNode(parameters);
        add(parameters.getString(JDBCv2Helpers.DB_NAME), nextDB);
        return null;
    }

}
