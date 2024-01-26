package com.project.NlConverter;

import java.util.Hashtable;


public class sparql {
    public static Hashtable<String, String> cidoc_dict=new Hashtable<>();
    public static String prefixQuery="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "        PREFIX crm: <http://erlangen-crm.org/current/>\n" +
            "        PREFIX vt: <https://kb.virtualtreasury.ie/>\n" +
            "        PREFIX vt_ont: <https://ont.virtualtreasury.ie/ontology#>\n";

    public static void main(String[] args) {
        bodyQuery();
    }

    public static void createSPARQLQuery(){
        String startSelect="select distinct";

    }

    public static String selectSection(String start){
        String select="?person_name";

        return select;
    }

    public static String bodyQuery(){
        Boolean givenName=false;
        String personN="Cianna";
        String nameAppellation="normalized-appellation-surname-forename";
        String findPerson="?person crm:P1_is_identified_by ?appellation.\n"+
                "       ?appellation rdfs:label ?personName ";
        if (givenName){
            findPerson=findPerson.replace("?personName","'"+personN+"'.");
        }else{
            findPerson=findPerson+"FILTER(CONTAINS(str(?appellation),'"+nameAppellation+"')).";
        }
        System.out.println("Person thing------"+findPerson);


        /*
        Have to do same as i did above but for extracting the dictionary terms to match the intention of the question
        ie. if intention is place, see if subject is birth/death. then combine them to get birthPlace and that beomes
        the dictionary lookup term
         */

        String body="Hello";
        return body;
    }

    public static void populateCIDOCDictionary(){
        cidoc_dict.put("birthDate","?birth rdf:type crm:E67_Birth;\n"+
                "       crm:P4_has_time-span ?timespanA;\n"+
                "       crm:P98_brought_into_life ?person.\n " +
                "       ?timespanA crm:P82a_begin_of_the_begin ?birthDate.");

        cidoc_dict.put("birthPlace","?birth rdf:type crm:E67_Birth;\n" +
                "       crm:P7_took_place_at ?birthPlace.");

    }
}
