package org.zerograph.zapp.resources;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.api.DatabaseInterface;
import org.zerograph.zapp.api.ResourceInterface;
import org.zerograph.zapp.api.RequestInterface;
import org.zerograph.zapp.api.ResponderInterface;
import org.zerograph.zapp.except.ClientError;
import org.zerograph.zapp.except.ServerError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CypherResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "Cypher";

    public CypherResource(ResponderInterface responder) {
        super(responder);
    }

    public String getName() {
        return NAME;
    }

    /**
     * EXECUTE cypher {query} [{params}]
     *
     */
    @Override
    public PropertyContainer execute(RequestInterface request, DatabaseInterface database) throws ClientError, ServerError {
        String query = request.getArgumentAsString("query");
        Map<String, Object> params = request.getArgumentAsMap("params", null);
        try {
            ExecutionResult result;
            if (params == null) {
                result = database.execute(query);
            } else {
                result = database.execute(query, params);
            }
            HashMap<String, Object> meta = new HashMap<>();
            List<String> columns = result.columns();
            meta.put("columns", Arrays.asList(columns.toArray(new Object[columns.size()])));
            responder.sendHead(meta);
            PropertyContainer firstEntity = null;
            int rowNumber = 0;
            responder.startBodyList();
            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                responder.sendBodyItem(Arrays.asList(values.toArray(new Object[values.size()])));
                if (rowNumber == 0) {
                    Object firstValue = values.get(0);
                    if (firstValue instanceof PropertyContainer) {
                        firstEntity = (PropertyContainer)firstValue;
                    }
                }
                rowNumber += 1;
            }
            responder.endBodyList();
            return firstEntity;
        } catch (CypherException ex) {
            throw new ClientError(ex.getMessage());
        }
    }

}
