package com.AdalbertoSebastian.PokeApi.Controller;

import com.AdalbertoSebastian.PokeApi.ML.PokeApiResponse;
import com.AdalbertoSebastian.PokeApi.ML.Pokemon;
import com.AdalbertoSebastian.PokeApi.ML.PokemonDetail;
import com.AdalbertoSebastian.PokeApi.ML.Results;
import com.AdalbertoSebastian.PokeApi.Service.PokemonService;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/pokemon")
public class PokemonController {

    @Autowired
    private PokemonService PokemonService;

    @GetMapping("index")
    public String Index(Model model) {
//        List<Results> pokemons = PokemonService.getAllPokemons();
        List<PokemonDetail> pokemons = new ArrayList<>(PokemonService.getAllDetails());

        model.addAttribute("pokemons", pokemons);
        return "PokemonIndex";
    }

    @GetMapping("/detalle/{name}")
//       @ResponseBody
//    public PokemonDetail detalle(@PathVariable String name, Model model) {
    public String detalle(@PathVariable String name, Model model) {
        PokemonDetail detail = PokemonService.getPokemonDetail(name);

        model.addAttribute("pokemon", detail);
        return "PokemonDetalle";
//        return detail;
    }

    @GetMapping("/prueba")
    public String prueba() {

        return "PokemonDetalle";
    }

}
