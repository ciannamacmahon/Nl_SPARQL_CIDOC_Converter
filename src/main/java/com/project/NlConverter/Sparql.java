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

            
    public static String createSPARQLQuery(String entityType, String entity,String question,String subj,
    String pred,LinkedList <String> obj,boolean rangeFilter,String filterCondition)
    {
        populateCIDOCDictionary();
        String selectTarget="";
        //select phrases
        if(subj.contains("?")){
                //subject is the intent and should want the name if its person
                String typeQ=subj.substring(1,subj.length());
                if (typeQ.equals("Person")){
                    selectTarget=selectSection("personName");
                    selectTarget=selectTarget+" ?person";
                }
        }
        else if(obj.size()==1 && obj.get(0).contains("?")){
            String objExtracted=obj.get(0);
            //object is the intent of the question
            // normaly the pred combined with object question is intent
            //bearPlaceName
            selectTarget=selectSection(pred+objExtracted.substring(1,objExtracted.length()));
            
        }
        if (entityType.contains("PERSON")){
            selectTarget=selectTarget+" ?person";

        }
        for(String word:obj){
            System.out.println("objects are ; "+word);
        }

        String body=bodyQuery(subj,pred,obj);

        String queryType="";
        // shoudl this go here so can add in list of objects to the constructor if there is loads of objects for the filter ie. between dates
       // String filterPart="";
        if(question.contains("how many")){
            //currently default so will count number of people under the given header 'people'
            queryType="select(count(distinct ?person)as ?people )";
        }
        else{
            queryType="select distinct"+selectTarget;
        }
        String filterPart=filterQuery(entity,entityType,rangeFilter,filterCondition);


            //substitute the name in the person part of the query
        // person is the constant in all queries
        String person=personQuery(entityType,entity);
        
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
    public static String filterQuery(String entityString,String entityT,boolean rangeFilter,String filterCondition){
        String filterStringEnd=").";
        String filterStringStart="\nfilter(";
        String filterString="";
        String filterStringFinal="";
        // add in filter based on cidocTarget and replace it with the entityString
        if(rangeFilter){

        }
        else{
                if (entityString.matches(".*\\d+.*")){
                    System.out.println("check three");
                    filterString=dateQuery(entityString,cidocTarget);
                }
                else if (entityT.contains("PERSON")){
                    String[]name=entityString.split(" ");
                    String appelation=name[1]+", "+name[0];
                    filterString=filterStringStart+"CONTAINS(str(?personName) ,'"+appelation+"')";
                    System.out.println("check four");

                }
                else {
                    filterString=filterStringStart+"CONTAINS(str(?"+cidocTarget+") ,'"+entityString+"')";

                }
                filterStringFinal=filterStringFinal+filterString;
            
        }
        filterStringFinal=filterStringFinal+filterStringEnd;
        System.out.println("Final "+filterStringFinal);


        //if entity equals place then replace the cidoc part in the returned bodySPARQLQuery with the entity
        
        return filterStringFinal;
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
    
    public static String dateQuery(String inputDate,String cidoc){
       // String inputDate=entity;
       String filterString="";
                if(isFullFormat(inputDate)){
                    filterString="\nfilter(?"+cidoc+"='"+inputDate+"'^^xsd:date";
                    
                }
                //deafult to start of the year
                else{
                    filterString="\nfilter(?"+cidoc+">='"+inputDate+"-01-01'^^xsd:date).\n" +
                            "filter(?"+cidoc+"<='"+inputDate+"-12-31'^^xsd:date";
                    
                }

        return filterString;
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
                "       ?appellation rdfs:label ?personName. ";

            findPerson=findPerson+"FILTER(CONTAINS(str(?appellation),'"+nameAppellation+"')).";
        
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
                "       crm:P7_took_place_at ?bearPlaceName;\n" +
                "       crm:P98_brought_into_life ?person.\n" +
                "OPTIONAL{?bearPlace crm:P1_is_identified_by ?bearPlaceLink.\n" +
                "?bearPlaceLink rdfs:label ?bearPlaceName.}");

        cidoc_dict.put("dieDate","?death rdf:type crm:E69_Death;\n"+
        "       crm:P4_has_time-span ?timespanA;\n"+
                "       crm:P93_took_out_of_existence ?person.\n " +
                "       ?timespanA crm:P82b_end_of_the_end ?dieDate.");

        cidoc_dict.put("diePlaceName","?death rdf:type crm:E69_Death;\n"+
                "       crm:P7_took_place_at ?diePlaceName;\n" +
                "       crm:P93_took_out_of_existence ?person.\n"+
                "OPTIONAL{?diePlace crm:P1_is_identified_by ?diePlaceLink." +
                "?diePlaceLink rdfs:label ?diePlaceName.}");

    }

    
}
