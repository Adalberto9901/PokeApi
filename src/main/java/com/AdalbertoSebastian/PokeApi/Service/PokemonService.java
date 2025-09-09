package com.AdalbertoSebastian.PokeApi.Service;

import com.AdalbertoSebastian.PokeApi.ML.PokeApiResponse;
import com.AdalbertoSebastian.PokeApi.ML.PokemonDetail;
import com.AdalbertoSebastian.PokeApi.ML.PokemonSpecies;
import com.AdalbertoSebastian.PokeApi.ML.Results;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PokemonService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";

    private final List<Results> allPokemons = new ArrayList<>();
    private final Map<Integer, PokemonDetail> pokemonDetailsCacheById = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(20); // cantidad de hilos

    public List<Results> getAllPokemons() {
        return allPokemons;
    }

    public Collection<PokemonDetail> getAllDetails() {
        return pokemonDetailsCacheById.values();
    }

    public PokemonDetail getPokemonDetail(String key) {
        if (isNumeric(key)) {
            return pokemonDetailsCacheById.get(key);
        }

        // buscar por nombre
        return pokemonDetailsCacheById.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    @PostConstruct
    public void loadPokemonPages() {
        int limit = 25;// pokemons conusltados
        for (int i = 0; i < 41; i++) {// numero de ciclos para los 1025 pokes
            int offset = i * limit;
            String url = BASE_URL + "?offset=" + offset + "&limit=" + limit;

            try {//consulta general
                ResponseEntity<PokeApiResponse> response = restTemplate.getForEntity(url, PokeApiResponse.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    List<Results> pageResults = response.getBody().getResults();
                    allPokemons.addAll(pageResults);
                }

            } catch (Exception e) {
                System.err.println("Error al cargar página " + (i + 1) + ": " + e.getMessage());
            }
        }

        // PARALELIZAR carga de detalles
        List<Callable<Void>> tasks = allPokemons.stream().map(result -> (Callable<Void>) () -> {
            String name = result.getName();
            try {
                // Detalles del Pokémon
                ResponseEntity<PokemonDetail> detailResponse = restTemplate.getForEntity(BASE_URL + name, PokemonDetail.class);
                PokemonDetail detail = detailResponse.getBody();

                // Especie (descripción por cada pokemon)
                String speciesUrl = detail.getSpecies().getUrl();
                ResponseEntity<PokemonSpecies> speciesResponse = restTemplate.getForEntity(speciesUrl, PokemonSpecies.class);
                PokemonSpecies species = speciesResponse.getBody();

                if (species != null && species.getFlavor_text_entries() != null) {
                    species.getFlavor_text_entries().stream()
                            .filter(entry -> "es".equals(entry.getLanguage().getName()))
                            .findFirst()
                            .ifPresent(entry -> {
                                String cleanText = entry.getFlavor_text().replace("\n", " ").replace("\f", " ");
                                detail.setDescripcion(cleanText);
                            });
                }

                // Guardar en caché
                if (detail != null) {
                    // consulta por id o por nombre del pokemon
                    pokemonDetailsCacheById.put(detail.getId(), detail);

                }

            } catch (Exception ex) {
                System.err.println("Error cargando detalles de " + name + ": " + ex.getMessage());
            }

            return null;
        }).collect(Collectors.toList());

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error ejecutando tareas: " + e.getMessage());
        }

        executor.shutdown();
        System.out.println(" Pokemones y detalles cargados en memoria: " + pokemonDetailsCacheById.size());
    }
// evaluar si el dato recibido es un int o string 

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
    //metodo de paginacion 
    public List<PokemonDetail> getPokemonsByPage(int page, int size) {
        int fromIndex = Math.max(0, (page - 1) * size);
        int toIndex = Math.min(fromIndex + size, pokemonDetailsCacheById.size());

        return new ArrayList<>(pokemonDetailsCacheById.values())
                .subList(fromIndex, toIndex);
    }
    // guardar el numero total de paginas
    public int getTotalPages(int pageSize) {
        return (int) Math.ceil((double) pokemonDetailsCacheById.size() / pageSize);
    }

}
