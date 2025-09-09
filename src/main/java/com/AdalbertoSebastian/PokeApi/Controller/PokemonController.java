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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/pokemon")
public class PokemonController {

    @Autowired
    private PokemonService PokemonService;

    @GetMapping("index")
    public String Index(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 25;

        int totalPages = PokemonService.getTotalPages(pageSize);
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        List<PokemonDetail> pokemons = PokemonService.getPokemonsByPage(page, pageSize);
        PokemonService.assignValidSprites(pokemons);

        model.addAttribute("pokemons", pokemons);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "PokemonIndex";
    }

    @GetMapping("/detalle/{name}")
    public String detalle(@PathVariable String name, Model model) {
        PokemonDetail detail = PokemonService.getPokemonDetail(name);

        model.addAttribute("pokemon", detail);
        return "PokemonDetalle";
    }

    @GetMapping("/prueba")
    public String prueba() {

        return "PokemonDetalle";
    }

}
