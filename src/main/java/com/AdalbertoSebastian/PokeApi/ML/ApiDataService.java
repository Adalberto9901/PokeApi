package com.AdalbertoSebastian.PokeApi.ML;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiDataService {

      private static final String API_URL = "https://api.ejemplo.com/datos";
    private static final int TOTAL_REGISTROS = 475;
    private static final int BLOQUE = 25;

    private final RestTemplate restTemplate;

    public ApiDataService() {
        this.restTemplate = new RestTemplate();
    }

    public List<Object> obtenerDatos() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(10); // Ajusta según tu servidor
        List<Future<List<Object>>> futures = new ArrayList<>();

        for (int offset = 0; offset < TOTAL_REGISTROS; offset += BLOQUE) {
            int finalOffset = offset;
            Callable<List<Object>> task = () -> {
                String url = API_URL + "?offset=" + finalOffset + "&limit=" + BLOQUE;
                ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);

                Object[] data = response.getBody();
                if (data == null) return new ArrayList<>();

                List<Object> list = new ArrayList<>();
                for (Object obj : data) {
                    list.add(obj); // o hacer cast/map a tu clase DTO
                }
                return list;
            };
            futures.add(executor.submit(task));
        }

        List<Object> resultadosTotales = new ArrayList<>();
        for (Future<List<Object>> future : futures) {
            resultadosTotales.addAll(future.get());
        }

        executor.shutdown();
        return resultadosTotales;
    }
}
//    public PokemonDetail getPokemonDetail(String name) {
//        if (isNumeric(name)) {
//            return pokemonDetailsCacheById.get(name);
//        }
//        return pokemonDetailsCacheById.get(name.toLowerCase());
//    }

//public PokemonDetail getById(int id) {
//    return pokemonDetailsCacheById.get(String.valueOf(id));
//}
//
//public PokemonDetail getByName(String name) {
//    return pokemonDetailsCacheById.values().stream()
//            .filter(p -> p.getName().equalsIgnoreCase(name))
//            .findFirst()
//            .orElse(null);
//}

