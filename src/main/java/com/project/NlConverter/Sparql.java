package com.project.NlConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.LinkedList;


public class Sparql {
    public static Map<String, String> cidoc_dict=new Hashtable<>();
    public static String cidocTarget="";
    public static String prefixQuery="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "        PREFIX crm: <http://erlangen-crm.org/current/>\n" +
            "        PREFIX vt: <https://kb.virtualtreasury.ie/>\n" +
            "        PREFIX vt_ont: <https://ont.virtualtreasury.ie/ontology#>\n";

            
    public static String createSPARQLQuery(LinkedList<String> entityType, LinkedList<String> entity,String question,String subj,String pred,LinkedList <String> obj,boolean rangeFilter)
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
        else if(obj.size()==1 && obj.get(0).contains("?")){
            String objExtracted=obj.get(0);
            //object is the intent of the question
            // normaly the pred combined with object question is intent
            //bearPlaceName
            select=selectSection(pred+objExtracted.substring(1,objExtracted.length()));
        }
        for(String word:obj){
            System.out.println("objects are ; "+word);
        }

        String body=bodyQuery(subj,pred,obj);
        String filterPart=filterQuery(entity,entityType,rangeFilter);

            //substitute the name in the person part of the query
        // person is the constant in all queries
        String person=personQuery(entityType,entity);

        //the substitution has to be based on the entity type
        if (entityType.contains("PERSON")){
            //
        }
        String queryType="";
        if(question.contains("How many")){
            queryType="select(count "+select+")";
        }
        else{
            queryType="select distinct"+select;
        }
        String fullQuery=prefixQuery+queryType+" Where {"+"\n"+person+"\n"+body+filterPart+"} Limit 5";
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
    public static String filterQuery(LinkedList<String> entityT,LinkedList<String> entityString,boolean rangeFilter){
        String filterStringEnd=").";
        String filterString="filter(?";
        // add in filter based on cidocTarget and replace it with the entityString
        if(rangeFilter){

        }
        else{
            if(entityT.size()>1){
                for(int i=0;i<entityT.size()-1;i++){
                    if(i!=0){
                        filterString=filterString+"%%";

                    }
                    if(entityT.get(i).contains("DATE")){
                    }
                    else if (entityT.get(i).contains("CITY")){
                        filterString=filterString+cidocTarget+"=='"+entityString.get(i)+"'";
                    }
                }
                filterString=filterString+filterStringEnd;
                System.out.println("Final "+filterString);
            }
        }

        //if entity equals place then replace the cidoc part in the returned bodySPARQLQuery with the entity
        
        return "";
    }
    public static String bodyQuery(String subj,String pred,LinkedList<String> obj){
        // whats the target?
        String bodySPARQLQuery="";
        if(subj.contains("?")){
            // subject is the target
            String extractedObj=obj.get(0);
            System.out.println("first object is "+extractedObj);
            if (obj.get(0).matches(".*\\d+.*")){
                cidocTarget=pred+"Date";
            }
            else{
                cidocTarget=pred+"PlaceName";
            }
        }
        else if (obj.get(0).contains("?")){
            String objExtract=obj.get(0);
            //object is the target
            cidocTarget=pred+objExtract.substring(1,objExtract.length());
        }
        bodySPARQLQuery=findCIDOC(cidocTarget);
        return bodySPARQLQuery;

    }
    
    public static String dateQuery(String bodyQuery,String entity,String cidoc){
        String inputDate=entity;
                if(isFullFormat(inputDate)){
                    String filterString="\nfilter(?"+cidoc+"='"+inputDate+"'^^xsd:date).";
                    String finalTest=bodyQuery+filterString;
                    bodyQuery=finalTest;
                    System.out.println("????????????????????? \n"+bodyQuery);
                }
                //deafult to start of the year
                else{
                    String filterString="\nfilter(?"+cidoc+">='"+inputDate+"-01-01'^^xsd:date).\n" +
                            "filter(?"+cidoc+"<='"+inputDate+"-12-31'^^xsd:date).";
                    String finalTest=bodyQuery+filterString;
                    bodyQuery=finalTest;
                    System.out.println("????????????????????? \n"+bodyQuery);
                }

        return "empty";
    }
    public static String placeQuery(String bodyQuery,String enity){
        return bodyQuery=bodyQuery.replace("?bearPlaceName","'"+enity+"'");

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
    public static String personQuery(LinkedList<String> entityType, LinkedList<String> entity){
        String nameAppellation="normalized-appellation-surname-forename";
        String findPerson="?person crm:P1_is_identified_by ?appellation.\n"+
                "       ?appellation rdfs:label ?personName. ";
        if(entityType.equals("PERSON")){
          //  String []personN=entity.split(" ");
          //  String surname_forename=personN[1]+", "+personN[0];
          //  findPerson=findPerson.replace("?personName","'"+surname_forename+"'.");
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
                "       crm:P7_took_place_at ?bearPlace;\n" +
                "       crm:P98_brought_into_life ?person.\n" +
                "?bearPlace crm:P1_is_identified_by ?bearPlaceLink.\n" +
                "?bearPlaceLink rdfs:label ?bearPlaceName.");

        cidoc_dict.put("dieDate","?death rdf:type crm:E69_Death;\n"+
        "       crm:P4_has_time-span ?timespanA;\n"+
                "       crm:P93_took_out_of_existence ?person.\n " +
                "       ?timespanA crm:P82b_end_of_the_end ?dieDate.");

        cidoc_dict.put("diePlaceName","?death rdf:type crm:E69_Death;\n"+
                "       crm:P7_took_place_at ?diePlace;" +
                "       crm:P93_took_out_of_existence ?person."+
                "?diePlace crm:P1_is_identified_by ?diePlaceLink." +
                "?diePlaceLink rdfs:label ?diePlaceName.");

    }

    
}
