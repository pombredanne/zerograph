package org.zerograph.resource;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.EntityNotFoundException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.zerograph.api.RequestInterface;
import org.zerograph.api.ResponderInterface;
import org.zerograph.api.TransactionalResourceInterface;
import org.zerograph.api.ZerographInterface;
import org.zerograph.response.status1xx.Continue;
import org.zerograph.response.status2xx.OK;
import org.zerograph.response.status4xx.BadRequest;
import org.zerograph.response.status4xx.NotFound;
import org.zerograph.response.status4xx.Status4xx;
import org.zerograph.response.status5xx.Status5xx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CypherResource extends AbstractTransactionalResource implements TransactionalResourceInterface {

    final private static String NAME = "cypher";

    public CypherResource(ZerographInterface zerograph, ResponderInterface responder, GraphDatabaseService database) {
        super(zerograph, responder, database);
    }

    public String getName() {
        return NAME;
    }

    /**
     * POST cypher {query} [{params}]
     *
     * @param request
     */
    @Override
    public PropertyContainer post(RequestInterface request, Transaction tx) throws Status4xx, Status5xx {
        String query = request.getStringData(0);
        try {
            ExecutionResult result = execute(query);
            List<String> columns = result.columns();
            respond(new Continue(columns.toArray(new Object[columns.size()])));
            PropertyContainer firstEntity = null;
            int rowNumber = 0;
            for (Map<String, Object> row : result) {
                ArrayList<Object> values = new ArrayList<>();
                for (String column : columns) {
                    values.add(row.get(column));
                }
                respond(new Continue(values.toArray(new Object[values.size()])));
                if (rowNumber == 0) {
                    Object firstValue = values.get(0);
                    if (firstValue instanceof PropertyContainer) {
                        firstEntity = (PropertyContainer)firstValue;
                    }
                }
                rowNumber += 1;
            }
            respond(new OK());
            return firstEntity;
        } catch (EntityNotFoundException ex) {
            throw new NotFound(ex.getMessage());
        } catch (CypherException ex) {
            //ex.printStackTrace(System.err);
            throw new BadRequest(ex.getMessage());
        }
    }

}
