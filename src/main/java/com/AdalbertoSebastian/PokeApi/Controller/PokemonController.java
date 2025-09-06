package com.AdalbertoSebastian.PokeApi.Controller;

import com.AdalbertoSebastian.PokeApi.ML.PokeApiResponse;
import com.AdalbertoSebastian.PokeApi.ML.Results;
import java.util.List;
import javax.xml.transform.Result;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/pokemon")
public class PokemonController {

    @GetMapping("index") // requeire de services
    @ResponseBody
    public List<Results> Index(Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<PokeApiResponse> response = restTemplate.exchange("https://pokeapi.co/api/v2/pokemon/",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                PokeApiResponse.class
        );

        List<Results> pokemons = response.getBody().getResults();

        model.addAttribute("pokemons", pokemons);
//        return "PokemonIndex";
        return pokemons;
    }
}
