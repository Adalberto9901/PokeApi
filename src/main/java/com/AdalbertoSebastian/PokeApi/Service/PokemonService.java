package com.AdalbertoSebastian.PokeApi.Service;

import com.AdalbertoSebastian.PokeApi.ML.PokeApiResponse;
import com.AdalbertoSebastian.PokeApi.ML.Results;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class PokemonService {
private final RestTemplate restTemplate = new RestTemplate();

    private final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";
    
    private List<Results> allPokemons = new ArrayList<>();

    public List<Results> getAllPokemons() {
        return allPokemons;
    }

    @PostConstruct
    public void loadPokemonPages() {
        int limit = 20;
        for (int i = 0; i < 10; i++) {
            int offset = i * limit;
            String url = BASE_URL + "?offset=" + offset + "&limit=" + limit;

            try {
                ResponseEntity<PokeApiResponse> response = restTemplate.getForEntity(url, PokeApiResponse.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    List<Results> pageResults = response.getBody().getResults();
                    allPokemons.addAll(pageResults);
                    System.out.println("Página " + (i + 1) + " cargada con " + pageResults.size() + " pokémones.");
                }
            } catch (Exception e) {
                System.err.println("Error al cargar página " + (i + 1) + ": " + e.getMessage());
            }
        }

        System.out.println(" Total pokémones cargados: " + allPokemons.size());
    }
}
