package com.project.NlConverter;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
        import org.apache.jena.query.ResultSet;

        import java.util.Iterator;

public class EndpointExecution {

    public static void main(String[] args) {
     //   String sparqlQuery = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
     //           "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
     //           "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
     //           "PREFIX crm: <http://erlangen-crm.org/current/>\n" +
     //           "PREFIX vt: <https://kb.virtualtreasury.ie/>\n" +
     //           "PREFIX vt_ont: <https://ont.virtualtreasury.ie/ontology#>\n" +
     //           "\n" +
     //           "select distinct ?person_name ?appellation ?birthDate\n" +
     //           "WHERE{\n" +
     //           "                     ?person crm:P1_is_identified_by ?appellation.\n" +
     //           "                     ?appellation rdfs:label 'Abbadie, Jacques'.\n" +
     //           "  \n" +
     //           "     ?birth rdf:type crm:E67_Birth;\n" +
     //           "            crm:P4_has_time-span ?timespanA;\n" +
     //           "            crm:P98_brought_into_life ?person.\n" +
     //           "                 ?timespanB crm:P82a_begin_of_the_begin ?birthDate.\n" +
     //           "\n" +
     //           "  }";
     //   searchGraph(sparqlQuery);
    }

    public static void searchGraph(String sparqlString) {
        String sparqlEndpoint = "https://blazegraph.virtualtreasury.ie/blazegraph/namespace/b2022/sparql";
        QueryExecution q = QueryExecution.service(sparqlEndpoint, sparqlString);
        ResultSet result = q.execSelect();
        System.out.println("The results are as follows:\n");
        while (result.hasNext()) {
            QuerySolution solution = result.next();
            Iterator<String> variables = solution.varNames();
            while (variables.hasNext()) {
                String selectVariable = variables.next();
                String resultVariable = solution.get(selectVariable).toString();
                System.out.println(selectVariable + ": " + resultVariable);
            }
        }

    }
}
