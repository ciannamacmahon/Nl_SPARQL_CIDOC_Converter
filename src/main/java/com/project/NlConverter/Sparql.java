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


        // String select=selectSection(pred,obj);
        String person=personQuery(entityType,entity);
        String body=bodyQuery(subj,pred,obj);
        String startSelect="select distinct";
      ///  String fullQuery=prefixQuery+startSelect+"\n"+select+" Where {"+"\n"+person+"\n"+body+"} Limit 5";
      ///  System.out.println(fullQuery);
        return "fullQuery";

    }

    public static String selectSection(String pred,String obj){
        // based on whats the target
        String finalSelect="?"+pred+obj.substring(1,obj.length());
        return finalSelect;
    }

    public static String bodyQuery(String subj,String pred,String obj){
        // whats the target?
        String cidoc="";
        String bodySPARQLQuery="";
        if(subj.contains("?")){
            // subject is the target
            if (obj.matches("\\d+")){
                cidoc=pred+"Date";
            }
            else{
                cidoc=pred+"Place";
            }

        }
        else if (obj.contains("?")){
            //object is the target
            cidoc=pred+obj.substring(1,obj.length());
        }
        bodySPARQLQuery=findCIDOC(cidoc);



        System.out.println(cidoc);
        System.out.println(bodySPARQLQuery);
        return bodySPARQLQuery;
        // after this it should then be determined what substitution needs to be made based on the target of the question
        // Eg. Object target
        //    - need to subsitute either the date/place into the cidoc notation
        // Subject target
        //     - need to subsitute the name of the person
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
        if(entityType.equals("DATE")){

        }
     //   System.out.println("Person thing------"+findPerson);
        return findPerson;
    }
    public static String findCIDOC(String cidoc){
        String mapResult=cidoc_dict.get(cidoc);
       // System.out.println(mapResult);
        return mapResult;
    }

    public static void populateCIDOCDictionary(){

        cidoc_dict.put("bearDate","?birth rdf:type crm:E67_Birth;\n"+
                "       crm:P4_has_time-span ?timespanA;\n"+
                "       crm:P98_brought_into_life ?person.\n " +
                "       ?timespanA crm:P82a_begin_of_the_begin ?bearDate.");

        cidoc_dict.put("bearPlace","?birth rdf:type crm:E67_Birth;\n" +
                "       crm:P7_took_place_at ?bearPlace.");

        cidoc_dict.put("dieDate","?death rdf:type crm:E69_Death;\n"+
        "       crm:P4_has_time-span ?timespanA;\n"+
                "       crm:P93_took_out_of_existence ?person.\n " +
                "       ?timespanA crm:P82b_end_of_the_end ?dieDate.");

        cidoc_dict.put("diePlace","?death rdf:type crm:E69_Death;\n"+
                "       crm:P7_took_place_at ?diePlace."  );

    }
}
