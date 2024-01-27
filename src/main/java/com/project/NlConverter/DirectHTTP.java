package com.project.NlConverter;

import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;

public class DirectHTTP {

    public static void main(String[] args) {
        // SPARQL endpoint URL

        // SPARQL query
        String sparqlQuery = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX crm: <http://erlangen-crm.org/current/>\n" +
                "PREFIX vt: <https://kb.virtualtreasury.ie/>\n" +
                "PREFIX vt_ont: <https://ont.virtualtreasury.ie/ontology#>\n" +
                "select distinct\n" +
                "?person_name ?birthDateWhere {\n" +
                "?person crm:P1_is_identified_by ?appellation.\n" +
                "?appellation rdfs:label 'Abbadie, Jacques'. \n" +
                "?birth rdf:type crm:E67_Birth;\n" +
                "crm:P4_has_time-span ?timespanA;\n" +
                "crm:P98_brought_into_life ?person.\n" +
                "?timespanA crm:P82a_begin_of_the_begin ?birthDate.\n}";

        // Execute the SPARQL query
        String result = executeSPARQLQuery(sparqlQuery);

        // Process the result as needed
        System.out.println("SPARQL Query Result:\n" + result);
    }

    private static String executeSPARQLQuery(String sparqlQuery) {
        try {
            URL url = new URL("https://blazegraph.virtualtreasury.ie/blazegraph/namespace/b2022/sparql");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                return response.toString();
            } else {
                System.out.println("HTTP Error: " + connection.getResponseCode());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}