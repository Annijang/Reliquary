package xreliquary.init;

import com.sun.javafx.sg.prism.NGShape;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import xreliquary.lib.Names;
import xreliquary.lib.Reference;

import java.util.ArrayList;

public class ItemModels {
    private static ItemModels instance;

    public static ItemModels getInstance() {
        if (instance == null) {
            instance = new ItemModels();
        }

        return instance;
    }

    private ArrayList<ModelResourceLocation> models = new ArrayList<>();

    public static int INFERNAL_TEAR = 0;
    public static int INFERNAL_TEAR_EMPTY = 1;

    private ItemModels() {
        models.add(new ModelResourceLocation(Reference.DOMAIN + Names.infernal_tear, "inventory"));
        models.add(new ModelResourceLocation(Reference.DOMAIN + Names.infernal_tear_empty, "inventory"));
    }

    public ModelResourceLocation getModel(int modelIndex) {
        return models.get(modelIndex);
    }
}
