package com.project.NlConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        String select="";
        //select phrases
        if(subj.contains("?")){
            //subject is the intent and should want the name if its person
            String typeQ=subj.substring(1,subj.length());
            if (typeQ.equals("Person")){
                select=selectSection("personName");
            }
        }
        else if(obj.contains("?")){
            //object is the intent of the question
            // normaly the pred combined with object question is intent
            //bearPlaceName
            select=selectSection(pred+obj.substring(1,obj.length()));
        }

        String body=bodyQuery(subj,pred,obj,entity,entityType);

            //substitute the name in the person part of the query
        // person is the constant in all queries
        String person=personQuery(entityType,entity);

        //the substitution has to be based on the entity type
        if (entityType.contains("PERSON")){
            //
        }


        String startSelect="select distinct";
        String fullQuery=prefixQuery+startSelect+"\n"+select+" Where {"+"\n"+person+"\n"+body+"} Limit 5";
        System.out.println("---------------------------------");
        System.out.println("SPARQL Query: "+fullQuery);
        // System.out.println(fullQuery);
        return fullQuery;

    }

    public static String selectSection(String selection){
        // based on whats the target
        String finalSelect="?"+selection;
        return finalSelect;
    }

    public static String bodyQuery(String subj,String pred,String obj,String entity,String entityType){
        // whats the target?
        String cidoc="";
        String bodySPARQLQuery="";
        if(subj.contains("?")){
            // subject is the target
            if (obj.matches(".*\\d+.*")){
                cidoc=pred+"Date";
            }
            else{
                cidoc=pred+"PlaceName";
            }

        }
        else if (obj.contains("?")){
            //object is the target
            cidoc=pred+obj.substring(1,obj.length());
        }
        bodySPARQLQuery=findCIDOC(cidoc);

        //if entity equals place then replace the cidoc part in the returned bodySPARQLQuery with the entity
        if(subj.contains("?")){
            if(entityType.contains("DATE")){
                String inputDate=entity;
                if(isFullFormat(inputDate)){
                    String filterString="\nfilter(?"+cidoc+"='"+inputDate+"'^^xsd:date).";
                    String finalTest=bodySPARQLQuery+filterString;
                    bodySPARQLQuery=finalTest;
                    System.out.println("????????????????????? \n"+bodySPARQLQuery);
                }
                else{
                    String filterString="\nfilter(?"+cidoc+">='"+inputDate+"-01-01'^^xsd:date).\n" +
                            "filter(?"+cidoc+"<='"+inputDate+"-12-31'^^xsd:date).";
                    String finalTest=bodySPARQLQuery+filterString;
                    bodySPARQLQuery=finalTest;
                    System.out.println("????????????????????? \n"+bodySPARQLQuery);
                }


            }

        }
        return bodySPARQLQuery;

    }

    public static boolean isFullFormat(String date){
        SimpleDateFormat fullFormat=new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Date: "+date);
        try{
            Date parsedFull=fullFormat.parse(date);
            System.out.println(parsedFull);
            return true;
        }
        catch (ParseException e){
            return false;
        }
    }
    public static String personQuery(String entityType, String entity){
        String nameAppellation="normalized-appellation-surname-forename";
        String findPerson="?person crm:P1_is_identified_by ?appellation.\n"+
                "       ?appellation rdfs:label ?personName ";
        if(entityType.equals("PERSON")){
            String []personN=entity.split(" ");
            String surname_forename=personN[1]+", "+personN[0];
            findPerson=findPerson.replace("?personName","'"+surname_forename+"'.");
        }
        else{
            findPerson=findPerson+"FILTER(CONTAINS(str(?appellation),'"+nameAppellation+"')).";
        }
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

        cidoc_dict.put("bearPlaceName","?birth rdf:type crm:E67_Birth;\n" +
                "       crm:P7_took_place_at ?bearPlace." +
                "?bearPlace crm:P1_is_identified_by ?bearPlaceLink." +
                "?bearPlaceLink rdfs:label ?bearPlaceName.");

        cidoc_dict.put("dieDate","?death rdf:type crm:E69_Death;\n"+
        "       crm:P4_has_time-span ?timespanA;\n"+
                "       crm:P93_took_out_of_existence ?person.\n " +
                "       ?timespanA crm:P82b_end_of_the_end ?dieDate.");

        cidoc_dict.put("diePlaceName","?death rdf:type crm:E69_Death;\n"+
                "       crm:P7_took_place_at ?diePlace."+
                "?diePlace crm:P1_is_identified_by ?diePlaceLink." +
                "?diePlaceLink rdfs:label ?diePlaceName.");

    }
}
