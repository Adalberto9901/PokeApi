package com.AdalbertoSebastian.PokeApi.Service;

import com.AdalbertoSebastian.PokeApi.ML.PokeApiResponse;
import com.AdalbertoSebastian.PokeApi.ML.PokemonDetail;
import com.AdalbertoSebastian.PokeApi.ML.PokemonSpecies;
import com.AdalbertoSebastian.PokeApi.ML.Results;
import com.AdalbertoSebastian.PokeApi.ML.Sprites;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
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

    private final ExecutorService executor = Executors.newFixedThreadPool(40); // cantidad de hilos

    private static final Map<String, String> TYPE_COLORS = Map.ofEntries(
            Map.entry("grass", "#78C850"),
            Map.entry("fire", "#F08030"),
            Map.entry("water", "#4068c7"),
            Map.entry("bug", "#A8B820"),
            Map.entry("normal", "#A8A878"),
            Map.entry("poison", "#A040A0"),
            Map.entry("electric", "#F8D030"),
            Map.entry("ground", "#E0C068"),
            Map.entry("fairy", "#EE99AC"),
            Map.entry("fighting", "#C03028"),
            Map.entry("flying", "#9ad1d6"),
            Map.entry("psychic", "#F85888"),
            Map.entry("rock", "#B8A038"),
            Map.entry("ghost", "#705898"),
            Map.entry("ice", "#6dc0d1"),
            Map.entry("dragon", "#7038F8"),
            Map.entry("dark", "#705848"),
            Map.entry("steel", "#B8B8D0")
    );

    public List<Results> getAllPokemons() {
        return allPokemons;
    }

    public Collection<PokemonDetail> getAllDetails() {
        return pokemonDetailsCacheById.values();
    }

    public PokemonDetail getPokemonDetail(String key) {
        if (isNumeric(key)) {
            int id = Integer.parseInt(key);
            return pokemonDetailsCacheById.getOrDefault(id, null);
        }

        // buscar por nombre
        return pokemonDetailsCacheById.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    public List<PokemonDetail> getPokemonDetailsByTipoAndNombre(String key, List<String> tipos) {
        return pokemonDetailsCacheById.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(key))
                .filter(pokemon -> {
                    List<String> tiposPokemon = pokemon.getTypes().stream()
                            .map(type -> type.getType().getName())
                            .collect(Collectors.toList());
                    return tipos.containsAll(tiposPokemon) || tiposPokemon.containsAll(tipos);
                })
                .collect(Collectors.toList());
    }

    public List<PokemonDetail> getPokemonDetailByTipo(List<String> tipos) {
        return pokemonDetailsCacheById.values().stream()
                .filter(pokemon -> {
                    List<String> tiposPokemon = pokemon.getTypes().stream()
                            .map(type -> type.getType().getName())
                            .collect(Collectors.toList());
                    return tiposPokemon.stream().allMatch(tipos::contains);
                })
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void loadPokemonPages() {
        int limit = 25;// pokemons consultados
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

                // Asignamos el degradado
                if (detail != null) {
                    List<String> tiposPokemon = detail.getTypes().stream()
                            .map(type -> type.getType().getName())
                            .collect(Collectors.toList());

                    String background = generateBackground(tiposPokemon);
                    detail.setBackground(background);

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

    // Método actualizado para asignar todos los sprites tipo .gif
    public void assignValidSprites(List<PokemonDetail> pokemons) {
        for (PokemonDetail pokemon : pokemons) {
            int id = pokemon.getId();

            String frontGif = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/" + id + ".gif";
            String backGif = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/back/" + id + ".gif";
            String frontShinyGif = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/shiny/" + id + ".gif";
            String backShinyGif = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/back/shiny/" + id + ".gif";

            String frontPng = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
            String backPng = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/back/" + id + ".png";
            String frontShinyPng = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/shiny/" + id + ".png";
            String backShinyPng = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/back/shiny/" + id + ".png";

            Sprites sprite = pokemon.getSprites();

            sprite.setFront_default(urlExists(frontGif) ? frontGif : frontPng);
            sprite.setBack_default(urlExists(backGif) ? backGif : backPng);
            sprite.setFront_shiny(urlExists(frontShinyGif) ? frontShinyGif : frontShinyPng);
            sprite.setBack_shiny(urlExists(backShinyGif) ? backShinyGif : backShinyPng);
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

    //METODO PARA GENERAR EL DEGRADADO EN LA CARD DEL POKEMON
    private String generateBackground(List<String> types) {
        if (types == null || types.isEmpty()) {
            return "#fff"; // fallback
        }

        if (types.size() == 1) {
            return TYPE_COLORS.getOrDefault(types.get(0).toLowerCase(), "#fff");
        }

        String color1 = TYPE_COLORS.getOrDefault(types.get(0).toLowerCase(), "#fff");
        String color2 = TYPE_COLORS.getOrDefault(types.get(1).toLowerCase(), "#fff");

        return "linear-gradient(135deg, " + color1 + ", " + color2 + ")";
    }
}
