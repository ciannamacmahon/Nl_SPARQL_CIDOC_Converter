import "./MainPage.css";
import React, { useState } from 'react';


function MainPage() {
    const [query,setQuery]=useState("");

    return(
        <div className='SearchBar'>
      <input placeholder='Enter you search query on the VRTI KG'
        onChange={(event)=>setQuery(event.target.value)}
        value={query}
      />
    </div>
    );
    
  }
export default MainPage;