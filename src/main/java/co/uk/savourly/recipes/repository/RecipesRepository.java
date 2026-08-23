package co.uk.savourly.recipes.repository;

import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.model.User;

import java.util.List;

public interface RecipesRepository {
    Recipes findAll();
    Recipe findRandom();
    Recipe findRandom(String excludeId);
    void save(Recipe recipe);
    void saveAll(List<Recipe> recipes);
    Recipe findByName(String name);
    void clear();

    User findUserByName(String username);
    void saveUser(User user);
    List<Recipe> findStarredRecipesForUser(String username);
}
