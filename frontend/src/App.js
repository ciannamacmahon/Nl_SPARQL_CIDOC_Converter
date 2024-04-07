import React, { useState } from 'react';
import {BrowserRouter as Router,Routes,Route}from 'react-router-dom';
import './App.css';
import Home from "./Components/pages/Home";
import MainPage from './Components/pages/MainPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path='/' exact element={<Home/>}/>
        <Route path='/searchCase' exact element={<MainPage/>}/>
      </Routes>
    </Router>
  );
}

export default App;
