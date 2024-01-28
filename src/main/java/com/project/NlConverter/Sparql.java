package com.project.NlConverter;

import java.util.Hashtable;
import java.util.Map;


public class Sparql {
    public static Map<String, String> cidoc_dict=new Hashtable<>();
    public static String prefixQuery="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "        PREFIX crm: <http://erlangen-crm.org/current/>\n" +
            "        PREFIX vt: <https://kb.virtualtreasury.ie/>\n" +
            "        PREFIX vt_ont: <https://ont.virtualtreasury.ie/ontology#>\n";


    public static String createSPARQLQuery(String entityType, String entity,String subj,String pred,String obj)
    {
        populateCIDOCDictionary();
        String select=selectSection();
        String person=personQuery(entityType,entity);
        String body=bodyQuery(subj,pred,obj);
        String startSelect="select distinct";
        String fullQuery=prefixQuery+startSelect+"\n"+select+" Where {"+"\n"+person+"\n"+body+"}";
        System.out.println(fullQuery);
        return fullQuery;

    }

    public static String selectSection(){
        String select="?person_name ?birthDate";
        return select;
    }

    public static String bodyQuery(String subj,String pred,String obj){

        String cidoc=pred+obj.substring(1,obj.length());
        String bodyQuery=findCIDOC(cidoc);
       // System.out.println(cidoc);


        /*
        Have to do same as i did above but for extracting the dictionary terms to match the intention of the question
        ie. if intention is place, see if subject is birth/death. then combine them to get birthPlace and that beomes
        the dictionary lookup term
         */
        return bodyQuery;
    }
    public static String personQuery(String entityType, String entity){
        String nameAppellation="normalized-appellation-surname-forename";
        String findPerson="?person crm:P1_is_identified_by ?appellation.\n"+
                "       ?appellation rdfs:label ?personName ";
        if(entityType.equals("PERSON")){
            String personN=entity;
            findPerson=findPerson.replace("?personName","'"+personN+"'.");
        }
        else{
            findPerson=findPerson+"FILTER(CONTAINS(str(?appellation),'"+nameAppellation+"')).";
        }
        System.out.println("Person thing------"+findPerson);
        return findPerson;
    }
    public static String findCIDOC(String cidoc){
        String mapResult=cidoc_dict.get(cidoc);
        System.out.println(mapResult);
        return mapResult;
    }

    public static void populateCIDOCDictionary(){

        cidoc_dict.put("bearDate","?birth rdf:type crm:E67_Birth;\n"+
                "       crm:P4_has_time-span ?timespanA;\n"+
                "       crm:P98_brought_into_life ?person.\n " +
                "       ?timespanA crm:P82a_begin_of_the_begin ?birthDate.");

        cidoc_dict.put("bearPlace","?birth rdf:type crm:E67_Birth;\n" +
                "       crm:P7_took_place_at ?birthPlace.");

    }
}
