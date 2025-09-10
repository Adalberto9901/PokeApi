package com.AdalbertoSebastian.PokeApi.Controller;

import com.AdalbertoSebastian.PokeApi.ML.PokemonDetail;
import com.AdalbertoSebastian.PokeApi.Service.PokemonService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pokemon")
public class PokemonController {

    @Autowired
    private PokemonService PokemonService;

    @GetMapping("index")
    public String Index(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            Model model) {

        int pageSize = 25;

        // Caso 1: búsqueda
        if (search != null && !search.isEmpty()) {
            PokemonDetail pokemon = PokemonService.getPokemonDetail(search);

            if (pokemon != null) {
                PokemonService.assignValidSprites(List.of(pokemon));

                model.addAttribute("pokemons", List.of(pokemon));
                model.addAttribute("currentPage", 1);
                model.addAttribute("totalPages", 1);
            } else {
                model.addAttribute("pokemons", List.of());
                model.addAttribute("currentPage", 1);
                model.addAttribute("totalPages", 1);
                model.addAttribute("error", "⚠️ No se encontró el Pokémon: " + search);
            }
            return "PokemonIndex";
        }

        // Caso 2: paginación normal
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

        if (detail == null) {
            model.addAttribute("error", "⚠️ No se encontró el Pokémon: " + name);
            return "PokemonIndex";
        }

        model.addAttribute("pokemon", detail);
        return "PokemonDetalle";
    }
}
