package co.uk.savourly.recipes.repository;

import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Repository("inMemoryRecipesRepositoryStub")
public class InMemoryRecipesRepositoryStub implements RecipesRepository {

    @Value("#{systemProperties['totalRecipes'] ?: 12}")
    private String totalRecipeToBuild;

    private final List<Recipe> dynamicRecipes = new ArrayList<>();
    private final Map<String, User> userMap = new HashMap<>();
    private boolean customized = false;
    private volatile List<Recipe> cachedDefaultRecipes;

    @Override
    public synchronized Recipes findAll() {
        List<Recipe> recipeList;
        if (customized) {
            recipeList = new ArrayList<>(dynamicRecipes);
        } else {
            recipeList = getDefaultRecipes();
        }
        Recipes recipes = new Recipes();
        recipes.setRecipes(recipeList);
        recipes.setTotalItems(recipeList.size());
        return recipes;
    }

    @Override
    public synchronized Recipe findRandom() {
        return findRandom(null);
    }

    @Override
    public synchronized Recipe findRandom(String excludeId) {
        List<Recipe> source = customized ? dynamicRecipes : getDefaultRecipes();
        if (source == null || source.isEmpty()) {
            return null;
        }

        boolean hasExcludeId = excludeId != null && !excludeId.trim().isEmpty();
        if (hasExcludeId) {
            Recipe selected = selectRandomMatching(source, excludeId);
            if (selected != null) {
                return selected;
            }
        }

        return selectRandomMatching(source, null);
    }

    /**
     * Selects a random recipe from the given list, optionally excluding a specific recipe ID.
     *
     * @param source the list of recipes to choose from
     * @param excludeId the recipe ID to exclude from selection, or null if no recipe should be excluded
     * @return a randomly selected matching recipe, or null if no matching recipe is available
     */
    private Recipe selectRandomMatching(List<Recipe> source, String excludeId) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        int size = source.size();
        if (excludeId == null) {
            return source.get(ThreadLocalRandom.current().nextInt(size));
        }

        int idx = ThreadLocalRandom.current().nextInt(size);
        Recipe candidate = source.get(idx);
        if (candidate != null && !excludeId.equals(candidate.getId())) {
            return candidate;
        }

        if (size == 1) {
            return null;
        }

        int candidateIdx = ThreadLocalRandom.current().nextInt(size - 1);
        if (candidateIdx >= idx) {
            candidateIdx++;
        }
        candidate = source.get(candidateIdx);
        if (candidate != null && !excludeId.equals(candidate.getId())) {
            return candidate;
        }

        List<Recipe> matching = new ArrayList<>(size);
        for (Recipe r : source) {
            if (r != null && !excludeId.equals(r.getId())) {
                matching.add(r);
            }
        }
        if (matching.isEmpty()) {
            return null;
        }
        return matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
    }

    private List<Recipe> getDefaultRecipes() {
        List<Recipe> result = cachedDefaultRecipes;
        if (result == null) {
            synchronized (this) {
                result = cachedDefaultRecipes;
                if (result == null) {
                    int totalRecipes = 12;
                    try {
                        if (totalRecipeToBuild != null) {
                            totalRecipes = Integer.parseInt(totalRecipeToBuild);
                        }
                    } catch (NumberFormatException ignored) {
                    }

                    if (totalRecipes == 0) {
                        result = Collections.emptyList();
                    } else {
                        List<Recipe> all = createSampleRecipes();
                        if (totalRecipes >= all.size()) {
                            result = all;
                        } else {
                            result = new ArrayList<>(all.subList(0, totalRecipes));
                        }
                    }
                    cachedDefaultRecipes = result;
                }
            }
        }
        return result;
    }

    private List<Recipe> createSampleRecipes() {
        List<Recipe> list = new ArrayList<>();
        list.add(new Recipe("1", "Lemon Chicken", 30, "Lemon", "Chicken", "Thyme"));
        list.add(new Recipe("2", "Beef Stroganoff", 30, "Beef", "Mustard", "Mushrooms"));
        list.add(new Recipe("3", "Caesar Salad", 25, "Lettuce", "Croutons", "Parmesan"));
        list.add(new Recipe("4", "Lemon Rice", 15, "Lemon", "Rice", "Green Chillies"));
        list.add(new Recipe("5", "Ghee Rice", 20, "Rice", "Ghee"));
        list.add(new Recipe("6", "Coriander Rice", 25, "Coriander", "Rice"));
        list.add(new Recipe("7", "Chicken Pizza", 30, "Chicken", "Flour", "Onions"));
        list.add(new Recipe("8", "Cheese Pizza", 30, "Mozarella Cheese", "Flour", "Mushrooms"));
        list.add(new Recipe("9", "Supreme Pizza", 25, "Cheddar Cheese", "Flour", "Onions"));
        list.add(new Recipe("10", "Dosa", 90, "Rice flour", "Urad Dhal"));
        list.add(new Recipe("11", "Idly", 90, "Rice flour", "Urad Dhal"));
        list.add(new Recipe("12", "Roti", 40, "Wheat flour"));
        return list;
    }

    @Override
    public synchronized void save(Recipe recipe) {
        customized = true;
        dynamicRecipes.removeIf(r -> r.getName().equalsIgnoreCase(recipe.getName()));
        dynamicRecipes.add(recipe);
    }

    @Override
    public synchronized void saveAll(List<Recipe> recipes) {
        customized = true;
        for (Recipe r : recipes) {
            save(r);
        }
    }

    @Override
    public synchronized Recipe findByName(String name) {
        List<Recipe> source = customized ? dynamicRecipes : getDefaultRecipes();
        return source.stream()
                .filter(r -> r.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public synchronized void clear() {
        customized = true;
        dynamicRecipes.clear();
        userMap.clear();
    }

    @Override
    public synchronized User findUserByName(String username) {
        return userMap.computeIfAbsent(username, User::new);
    }

    @Override
    public synchronized void saveUser(User user) {
        userMap.put(user.getName(), user);
    }

    @Override
    public synchronized List<Recipe> findStarredRecipesForUser(String username) {
        User user = userMap.get(username);
        if (user == null || user.getStarredRecipeNames().isEmpty()) {
            return Collections.emptyList();
        }
        List<Recipe> starred = new ArrayList<>();
        for (String recipeName : user.getStarredRecipeNames()) {
            Recipe r = findByName(recipeName);
            if (r != null) {
                starred.add(r);
            }
        }
        return starred;
    }
}
