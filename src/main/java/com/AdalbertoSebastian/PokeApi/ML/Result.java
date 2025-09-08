package com.AdalbertoSebastian.PokeApi.ML;

import java.util.List;


public class Result<T> {

    public boolean correct;
    public String errorMessage;
    public String ex;
    public T object;
    public List<T> objects;
    
}
