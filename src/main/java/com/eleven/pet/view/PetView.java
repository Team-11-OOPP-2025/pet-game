package com.eleven.pet.view;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.model.PetModel;
import javafx.scene.layout.Pane;

public class PetView {
    public PetView(PetModel petModel, PetController controller) {
    }

    public Pane initializeUI() {
        return new Pane();
    }
}
