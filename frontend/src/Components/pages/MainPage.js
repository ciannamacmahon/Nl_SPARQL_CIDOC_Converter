import "./MainPage.css";
import React, { useState } from 'react';
import axios from 'axios';


function MainPage() {
    const [searchInput,setSearchInput]=useState("");
    const [buttonColour, setButtonColour]=useState("white");
    const[sparqlQuery,setSparqlQuery]=useState("");
    const[showSparqlQuery,setShowSparqlQuery]=useState(false);
    const config={headers:{'Content-Type':'application/json'}};

    const handleChange=(event)=>{
      setSearchInput(event.target.value);
    }

    const handleSubmit= async(event)=>{
      event.preventDefault();
      setButtonColour("black");
      try {
        console.log("Sending request with searchInput:", searchInput); // Log the searchInput value
        const response = await axios.post("http://localhost:8080/searchEngine",  searchInput,config );
        console.log("Response from backend:", response.data); // Log the response from the backend
      } catch (error) {
        console.error("Error processing:", error);
      }
    }

    const handleViewSparqlQuery=()=>{
      setShowSparqlQuery(!showSparqlQuery);
    }

    const fetchQuery=async () =>{
      setSparqlQuery("Loading.....");
      const response=await axios.get("http://localhost:8080/sparqlQuery",config);
      setSparqlQuery(response.data.sparqlQuery);
      setShowSparqlQuery(true);
      
    }

    return(
      <div className='MainPage'>
          <div className="SearchBar">
            <form onSubmit={handleSubmit}
                        className="form">
              <input 
                type="text" 
                placeholder="Enter your search query on the VRTI KG"
                onChange={handleChange} 
                value={searchInput}
                className="input"
                />
              <button type="submit" className="searchButton" style={{backgroundColor: buttonColour}}
              >Search</button>
            </form>
          </div>
          <div className='resultBox'>
            <p>The Natural Language Result will be shown here</p>

          </div>
          <div className='rawSparqlResult'>
            <button>View Raw SPARQL Result</button>
            <button onClick={handleViewSparqlQuery}>View SPARQL Query</button>
            {showSparqlQuery && (
              <div>
                <p> SPARQL Query: {sparqlQuery}</p>
                </div>
            )}
          </div>
          
      </div>
    );
      
  }
export default MainPage;