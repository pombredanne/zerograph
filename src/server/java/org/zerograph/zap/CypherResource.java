package org.zerograph.zap;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.PropertyContainer;
import org.zerograph.neo4j.api.DatabaseInterface;
import org.zerograph.service.api.ZerographInterface;
import org.zerograph.zap.api.ResourceInterface;
import org.zerograph.zpp.api.RequestInterface;
import org.zerograph.zpp.api.ResponderInterface;
import org.zerograph.zpp.except.ClientError;
import org.zerograph.zpp.except.ServerError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CypherResource extends AbstractResource implements ResourceInterface {

    final private static String NAME = "Cypher";

    public CypherResource(ZerographInterface zerograph, ResponderInterface responder) {
        super(zerograph, responder);
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
        try {
            ExecutionResult result = database.execute(query);
            HashMap<String, Object> meta = new HashMap<>();
            List<String> columns = result.columns();
            meta.put("columns", columns.toArray(new Object[columns.size()]));
            responder.sendHead(meta);
            PropertyContainer firstEntity = null;
            int rowNumber = 0;
            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                responder.sendBodyPart(values.toArray(new Object[values.size()]));
                if (rowNumber == 0) {
                    Object firstValue = values.get(0);
                    if (firstValue instanceof PropertyContainer) {
                        firstEntity = (PropertyContainer)firstValue;
                    }
                }
                rowNumber += 1;
            }
            return firstEntity;
        } catch (CypherException ex) {
            throw new ClientError(ex.getMessage());
        }
    }

}
