package xreliquary.compat.jei.potions;

import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MortarRecipeJEI extends BlankRecipeWrapper {
    @Nonnull
    private final List<ItemStack> inputs;

    @Nonnull
    private final ItemStack output;

    @SuppressWarnings("unchecked")
    public MortarRecipeJEI(@Nonnull List<ItemStack> inputs, @Nonnull ItemStack output)
    {
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public List getInputs()
    {
        return inputs;
    }

    @Override
    public List getOutputs(){ return Collections.singletonList(output);
    }
}
