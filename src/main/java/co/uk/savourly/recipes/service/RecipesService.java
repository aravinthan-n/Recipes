package co.uk.savourly.recipes.service;

import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.model.User;

import java.util.List;

public interface RecipesService {
    Recipes listRecipes();
    Recipes listRecipes(int pageNo, int itemsPerPage);

    Recipe getRecipeByName(String name);

    /**
     * Retrieves a random recipe from the repository.
     *
     * @return a random Recipe, or null if no recipes exist
     */
    Recipe getRandomRecipe();

    /**
     * Retrieves a random recipe from the repository, excluding the specified recipe ID.
     *
     * @param excludeId optional recipe ID to exclude from selection
     * @return a random Recipe not matching excludeId, or null if no matching recipe exists
     */
    Recipe getRandomRecipe(String excludeId);
    Recipes filterRecipesByTerm(String term);
    Recipes filterRecipesByMaxCookingMinutes(int maxMinutes);

    void starRecipeForUser(String username, String recipeName);
    void unstarRecipeForUser(String username, String recipeName);
    Recipes getStarredRecipesForUser(String username);

    void saveRecipe(Recipe recipe);
    void saveRecipes(List<Recipe> recipes);
    void clear();
    User getUser(String username);
}
