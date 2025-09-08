package com.AdalbertoSebastian.PokeApi.ML;

import java.util.List;


public class SpeciesDetail {
private int base_happiness;
private int capture_rate;

private Color color;
private List<Egg_groups> egg_groups;
private Evolution_chain evolution_chain;
private List<Flavor_text_entries> flavor_text_entries;

    public int getBase_happiness() {
        return base_happiness;
    }

    public void setBase_happiness(int base_happiness) {
        this.base_happiness = base_happiness;
    }

    public int getCapture_rate() {
        return capture_rate;
    }

    public void setCapture_rate(int capture_rate) {
        this.capture_rate = capture_rate;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Egg_groups> getEgg_groups() {
        return egg_groups;
    }

    public void setEgg_groups(List<Egg_groups> egg_groups) {
        this.egg_groups = egg_groups;
    }

    public Evolution_chain getEvolution_chain() {
        return evolution_chain;
    }

    public void setEvolution_chain(Evolution_chain evolution_chain) {
        this.evolution_chain = evolution_chain;
    }

    public List<Flavor_text_entries> getFlavor_text_entries() {
        return flavor_text_entries;
    }

    public void setFlavor_text_entries(List<Flavor_text_entries> flavor_text_entries) {
        this.flavor_text_entries = flavor_text_entries;
    }


}
