package com.AdalbertoSebastian.PokeApi.ML;

import java.util.List;


public class PokemonSpecies {

    private List<FlavorTextEntry> flavor_text_entries;

    public List<FlavorTextEntry> getFlavor_text_entries() {
        return flavor_text_entries;
    }

    public void setFlavor_text_entries(List<FlavorTextEntry> flavor_text_entries) {
        this.flavor_text_entries = flavor_text_entries;
    }

    public static class FlavorTextEntry {
        private String flavor_text;
        private Language language;

        public String getFlavor_text() {
            return flavor_text;
        }

        public void setFlavor_text(String flavor_text) {
            this.flavor_text = flavor_text;
        }

        public Language getLanguage() {
            return language;
        }

        public void setLanguage(Language language) {
            this.language = language;
        }
    }

    public static class Language {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
