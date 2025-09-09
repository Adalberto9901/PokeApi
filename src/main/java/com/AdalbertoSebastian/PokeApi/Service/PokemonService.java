package com.AdalbertoSebastian.PokeApi.Service;

import com.AdalbertoSebastian.PokeApi.ML.Flavor_text_entries;
import com.AdalbertoSebastian.PokeApi.ML.PokeApiResponse;
import com.AdalbertoSebastian.PokeApi.ML.Pokemon;
import com.AdalbertoSebastian.PokeApi.ML.SpeciesDetail;
import com.AdalbertoSebastian.PokeApi.ML.Sprites;
import com.AdalbertoSebastian.PokeApi.ML.PokemonDetail;
import com.AdalbertoSebastian.PokeApi.ML.Results;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PokemonService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";

    private List<Results> allPokemons = new ArrayList<>();
    private Map<String, PokemonDetail> pokemonDetailsCache = new HashMap<>();
    private Map<String, PokemonDetail> pokemonDetailsCacheById = new HashMap<>();

    public List<Results> getAllPokemons() {
        return allPokemons;
    }

    public Collection<PokemonDetail> getAllDetails() {
        return pokemonDetailsCache.values();
    }

    public PokemonDetail getPokemonDetail(String name) {
        if (isNumeric(name)) {
            return pokemonDetailsCacheById.get(name);
        } else {
        }
        return pokemonDetailsCache.get(name.toLowerCase());
    }

    @PostConstruct
    public void loadPokemonPages() {
        int limit = 20;
        for (int i = 0; i < 52; i++) {
            int offset = i * limit;
            String url = BASE_URL + "?offset=" + offset + "&limit=" + limit;

            try {
                ResponseEntity<PokeApiResponse> response = restTemplate.getForEntity(url, PokeApiResponse.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    List<Results> pageResults = response.getBody().getResults();
                    allPokemons.addAll(pageResults);

                    // Cargar detalles en caché ya sea por nombre o por id
                    for (Results result : pageResults) {
                        String name = result.getName();
                        String detailUrl = BASE_URL + name;
                        try {
                            ResponseEntity<PokemonDetail> detailResponse = restTemplate.getForEntity(detailUrl, PokemonDetail.class);
                            PokemonDetail detail = detailResponse.getBody();
                            pokemonDetailsCache.put(name.toLowerCase(), detail);

                            pokemonDetailsCacheById.put(String.valueOf(detail.getId()), detail);
                        } catch (Exception ex) {
                            System.err.println("Error cargando detalles de " + name + ": " + ex.getMessage());
                        }
                    }

                }
            } catch (Exception e) {
                System.err.println("Error al cargar página " + (i + 1) + ": " + e.getMessage());
            }
        }

        System.out.println(" Pokemones y detalles cargados en memoria: " + pokemonDetailsCache.size());
    }
//
//    public String getDescription(String nameOrId) {
//
//        try {
//            ResponseEntity<SpeciesDetail> response = restTemplate.getForEntity(nameOrId, SpeciesDetail.class);
//            if (response.getStatusCode().is2xxSuccessful()) {
//                List<Flavor_text_entries> entries = response.getBody().getFlavor_text_entries();
//                for (Flavor_text_entries entry : entries) {
////                    if (entry.getLenguage().getName().equals("es")) {
//                    if (entry.getLenguage().getName().equals("es")) {
//                        return entry.getFlavor_text().replaceAll("\n", " ").replaceAll("\f", " ");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error obteniendo descripción: " + e.getMessage());
//        }
//
//        return "Descripción no disponible.";
//    }

    public boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //Metodo para validacion y la extraccion del Sprite, en caso de no tener .gif trae el estatico .png
    public void assignValidSprites(List<PokemonDetail> pokemons) {
        for (PokemonDetail p : pokemons) {
            int id = p.getId();

            String gifUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/" + id + ".gif";
            String pngUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";

            if (urlExists(gifUrl)) {
                p.getSprites().setFront_default(gifUrl);
            } else {
                p.getSprites().setFront_default(pngUrl);
            }
        }
    }

    private boolean urlExists(String url) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.HEAD, null, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
}
