package co.uk.savourly.recipes.service;

import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.model.User;
import co.uk.savourly.recipes.repository.RecipesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("defaultRecipesService")
public class DefaultRecipesService implements RecipesService {

    @Value("#{inMemoryRecipesRepositoryStub}")
    private RecipesRepository recipesRepository;

    @Override
    public Recipes listRecipes() {
        Recipes original = recipesRepository.findAll();
        List<Recipe> list = original != null && original.getRecipes() != null ? original.getRecipes() : new ArrayList<>();
        Recipes result = new Recipes();
        result.setRecipes(new ArrayList<>(list));
        result.setTotalItems(list.size());
        return result;
    }

    @Override
    public Recipes listRecipes(int pageNo, int itemsPerPage) {
        Recipes original = recipesRepository.findAll();
        List<Recipe> allRecipes = original != null && original.getRecipes() != null ? original.getRecipes() : new ArrayList<>();
        int totalRecipes = allRecipes.size();

        Recipes recipes = new Recipes();
        recipes.setTotalItems(totalRecipes);

        int fromIndex = (pageNo - 1) * itemsPerPage;
        int toIndex = fromIndex + itemsPerPage;

        if (fromIndex >= totalRecipes) {
            recipes.setRecipes(new ArrayList<>());
            return recipes;
        }

        if (toIndex > totalRecipes) {
            toIndex = totalRecipes;
        }

        recipes.setRecipes(new ArrayList<>(allRecipes.subList(fromIndex, toIndex)));
        return recipes;
    }

    @Override
    public Recipe getRecipeByName(String name) {
        return recipesRepository.findByName(name);
    }

    @Override
    public Recipe getRandomRecipe() {
        return getRandomRecipe(null);
    }

    @Override
    public Recipe getRandomRecipe(String excludeId) {
        // Delegates random selection to the repository so each implementation can avoid
        // loading the full dataset into memory (e.g. a DB-backed repo can use COUNT + OFFSET).
        // Returns null — not a fallback recipe — when no recipes exist, so callers can
        // respond with an explicit 404 rather than silently navigating to a wrong record.
        Recipe recipe = recipesRepository.findRandom(excludeId);
        if (recipe == null) {
            // No recipes are stored; surface this explicitly rather than returning an
            // arbitrary placeholder that would cause a silent failure.
            return null;
        }
        if (recipe.getId() == null || recipe.getId().trim().isEmpty()) {
            // Guard against recipes with a missing ID to prevent navigation to an
            // arbitrary fallback path in the frontend.
            return null;
        }
        return recipe;
    }

    @Override
    public Recipes filterRecipesByTerm(String term) {
        Recipes all = recipesRepository.findAll();
        List<Recipe> source = all != null && all.getRecipes() != null ? all.getRecipes() : new ArrayList<>();
        if (term == null || term.trim().isEmpty()) {
            return listRecipes();
        }
        String lowerTerm = term.trim().toLowerCase();
        List<Recipe> matched = new ArrayList<>();
        for (Recipe r : source) {
            boolean nameMatch = r.getName() != null && r.getName().toLowerCase().contains(lowerTerm);
            boolean mainIngMatch = r.getMainIngredients() != null && r.getMainIngredients().stream()
                    .anyMatch(ing -> ing.toLowerCase().contains(lowerTerm));
            boolean detailedIngMatch = r.getIngredients() != null && r.getIngredients().stream()
                    .anyMatch(ing -> (ing.getName() != null && ing.getName().toLowerCase().contains(lowerTerm))
                            || (ing.getFormatted() != null && ing.getFormatted().toLowerCase().contains(lowerTerm)));

            if (nameMatch || mainIngMatch || detailedIngMatch) {
                matched.add(r);
            }
        }
        Recipes result = new Recipes();
        result.setRecipes(matched);
        result.setTotalItems(matched.size());
        return result;
    }

    @Override
    public Recipes filterRecipesByMaxCookingMinutes(int maxMinutes) {
        Recipes all = recipesRepository.findAll();
        List<Recipe> source = all != null && all.getRecipes() != null ? all.getRecipes() : new ArrayList<>();
        List<Recipe> matched = new ArrayList<>();
        for (Recipe r : source) {
            if (r.getCookingMinutes() <= maxMinutes) {
                matched.add(r);
            }
        }
        Recipes result = new Recipes();
        result.setRecipes(matched);
        result.setTotalItems(matched.size());
        return result;
    }

    @Override
    public void starRecipeForUser(String username, String recipeName) {
        User user = recipesRepository.findUserByName(username);
        if (user == null) {
            user = new User(username);
        }
        user.starRecipe(recipeName);
        recipesRepository.saveUser(user);
    }

    @Override
    public void unstarRecipeForUser(String username, String recipeName) {
        User user = recipesRepository.findUserByName(username);
        if (user != null) {
            user.unstarRecipe(recipeName);
            recipesRepository.saveUser(user);
        }
    }

    @Override
    public Recipes getStarredRecipesForUser(String username) {
        List<Recipe> starred = recipesRepository.findStarredRecipesForUser(username);
        Recipes result = new Recipes();
        result.setRecipes(starred != null ? starred : new ArrayList<>());
        result.setTotalItems(result.getRecipes().size());
        return result;
    }

    @Override
    public void saveRecipe(Recipe recipe) {
        recipesRepository.save(recipe);
    }

    @Override
    public void saveRecipes(List<Recipe> recipes) {
        recipesRepository.saveAll(recipes);
    }

    @Override
    public void clear() {
        recipesRepository.clear();
    }

    @Override
    public User getUser(String username) {
        return recipesRepository.findUserByName(username);
    }

    public void setRecipesRepository(RecipesRepository recipesRepository) {
        this.recipesRepository = recipesRepository;
    }
}
