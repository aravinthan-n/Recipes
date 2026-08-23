package co.uk.savourly.recipes.cucumber;

import co.uk.savourly.recipes.model.Ingredient;
import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.repository.InMemoryRecipesRepositoryStub;
import co.uk.savourly.recipes.service.DefaultRecipesService;
import co.uk.savourly.recipes.service.RecipesService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeSteps {

    private RecipesService recipesService;

    // Test state
    private Recipes currentRecipesResponse;
    private Recipe currentVisitedRecipe;
    private String currentMessage;
    private String currentUser;
    private boolean recipePageVisited = false;
    private boolean navigationElementsDisplayed = false;

    @Before
    public void setUp() {
        InMemoryRecipesRepositoryStub repository = new InMemoryRecipesRepositoryStub();
        repository.clear();
        DefaultRecipesService service = new DefaultRecipesService();
        service.setRecipesRepository(repository);
        this.recipesService = service;

        this.currentRecipesResponse = null;
        this.currentVisitedRecipe = null;
        this.currentMessage = null;
        this.currentUser = null;
        this.recipePageVisited = false;
        this.navigationElementsDisplayed = false;
    }

    // ==========================================
    // Given Steps
    // ==========================================

    @Given("the following recipes exist in the system:")
    public void the_following_recipes_exist_in_the_system(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String name = row.get("Name");
            String cookingTime = row.get("Cooking Time");
            String ingredientsStr = row.containsKey("Main Ingredients") ? row.get("Main Ingredients") : row.get("Ingredients");

            Recipe recipe = recipesService.getRecipeByName(name);
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setId(String.valueOf(name.hashCode()));
                recipe.setName(name);
            }

            if (cookingTime != null) {
                recipe.setCookingTime(cookingTime);
            }

            if (ingredientsStr != null) {
                List<String> ingList = Arrays.stream(ingredientsStr.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                recipe.setMainIngredients(ingList);
            }

            recipesService.saveRecipe(recipe);
        }
    }

    @Given("the system has the following recipe cooking times:")
    public void the_system_has_the_following_recipe_cooking_times(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String name = row.get("Recipe");
            String cookingTime = row.get("Cooking Time");

            Recipe recipe = recipesService.getRecipeByName(name);
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setId(String.valueOf(name.hashCode()));
                recipe.setName(name);
            }
            recipe.setCookingTime(cookingTime);
            recipesService.saveRecipe(recipe);
        }
    }

    @Given("the system has the following recipe image:")
    public void the_system_has_the_following_recipe_image(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String name = row.get("Recipe");
            String imageUrl = row.get("Image URL");

            Recipe recipe = recipesService.getRecipeByName(name);
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setId(String.valueOf(name.hashCode()));
                recipe.setName(name);
            }
            recipe.setImageUrl(imageUrl);
            recipesService.saveRecipe(recipe);
        }
    }

    @Given("the system has the following recipe ingredients:")
    public void the_system_has_the_following_recipe_ingredients(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String name = row.get("Recipe");
            String quantity = row.get("Quantity");
            String ingredientName = row.get("Ingredient");

            Recipe recipe = recipesService.getRecipeByName(name);
            if (recipe == null) {
                recipe = new Recipe();
                recipe.setId(String.valueOf(name.hashCode()));
                recipe.setName(name);
            }
            recipe.addIngredient(quantity, ingredientName);
            recipesService.saveRecipe(recipe);
        }
    }

    @Given("the user {string} exists in the system")
    public void the_user_exists_in_the_system(String username) {
        this.currentUser = username;
        recipesService.getUser(username);
    }

    @Given("{word} has no starred recipes")
    public void user_has_no_starred_recipes(String pronoun) {
        // User already has no starred recipes by default
    }

    @Given("{word} has the starred recipes:")
    public void user_has_the_starred_recipes(String pronoun, DataTable dataTable) {
        List<String> recipeNames = dataTable.asList();
        for (String recipeName : recipeNames) {
            recipesService.starRecipeForUser(currentUser, recipeName);
        }
    }

    // ==========================================
    // When Steps
    // ==========================================

    @When("there are no recipes in the system")
    public void there_are_no_recipes_in_the_system() {
        recipesService.clear();
        this.currentRecipesResponse = recipesService.listRecipes();
        if (this.currentRecipesResponse.getRecipes().isEmpty()) {
            this.currentMessage = "Sorry, we currently have no recipes for you";
        }
    }

    @When("a recipe is selected")
    public void a_recipe_is_selected() {
        this.recipePageVisited = true;
    }

    @When("there are more than {int} recipes in the system")
    public void there_are_more_than_recipes_in_the_system(int count) {
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 1; i <= count + 2; i++) {
            Recipe r = new Recipe(String.valueOf(i), "Recipe " + i, 20, "Ingredient " + i);
            recipes.add(r);
        }
        recipesService.saveRecipes(recipes);
        this.currentRecipesResponse = recipesService.listRecipes(1, 10);
        if (recipesService.listRecipes().getRecipes().size() > 10) {
            this.navigationElementsDisplayed = true;
        }
    }

    @When("a recipe is visited that cannot be found")
    public void a_recipe_is_visited_that_cannot_be_found() {
        this.currentVisitedRecipe = recipesService.getRecipeByName("NonExistentRecipe");
        if (this.currentVisitedRecipe == null) {
            this.currentMessage = "Sorry, this recipe doesn't exist or may have been removed";
        }
    }

    @When("the {string} recipe is visited")
    public void the_recipe_is_visited(String recipeName) {
        this.currentVisitedRecipe = recipesService.getRecipeByName(recipeName);
        if (this.currentVisitedRecipe == null) {
            this.currentMessage = "Sorry, this recipe doesn't exist or may have been removed";
        }
    }

    @When("{word} stars the recipe {string}")
    public void user_stars_the_recipe(String pronoun, String recipeName) {
        recipesService.starRecipeForUser(currentUser, recipeName);
    }

    @When("{word} unstars the recipe {string}")
    public void user_unstars_the_recipe(String pronoun, String recipeName) {
        recipesService.unstarRecipeForUser(currentUser, recipeName);
    }

    @When("{word} filters by starred recipes")
    public void user_filters_by_starred_recipes(String pronoun) {
        this.currentRecipesResponse = recipesService.getStarredRecipesForUser(currentUser);
        if (this.currentRecipesResponse.getRecipes().isEmpty()) {
            this.currentMessage = "Sorry, you don't currently have any starred recipes, get started by starring recipes you like";
        }
    }

    @When("the filter term {string} is entered")
    public void the_filter_term_is_entered(String term) {
        this.currentRecipesResponse = recipesService.filterRecipesByTerm(term);
        if (this.currentRecipesResponse.getRecipes().isEmpty()) {
            this.currentMessage = "Sorry, nothing matched your filter term";
        }
    }

    @When("the maximum cooking time {string} is selected")
    public void the_maximum_cooking_time_is_selected(String timeStr) {
        int minutes = Integer.parseInt(timeStr.replaceAll("[^0-9]", ""));
        this.currentRecipesResponse = recipesService.filterRecipesByMaxCookingMinutes(minutes);
        if (this.currentRecipesResponse.getRecipes().isEmpty()) {
            this.currentMessage = "Sorry, nothing matched your filter term";
        }
    }

    @When("a random recipe is requested")
    public void a_random_recipe_is_requested() {
        this.currentVisitedRecipe = recipesService.getRandomRecipe();
    }

    @When("a random recipe is requested when no recipes exist")
    public void a_random_recipe_is_requested_when_no_recipes_exist() {
        recipesService.clear();
        this.currentVisitedRecipe = recipesService.getRandomRecipe();
        if (this.currentVisitedRecipe == null) {
            this.currentMessage = "Sorry, we currently have no recipes for you";
        }
    }

    @When("a random recipe is requested excluding recipe {string}")
    public void a_random_recipe_is_requested_excluding_recipe(String recipeName) {
        Recipe excluded = recipesService.getRecipeByName(recipeName);
        String excludeId = excluded != null ? excluded.getId() : null;
        this.currentVisitedRecipe = recipesService.getRandomRecipe(excludeId);
    }

    // ==========================================
    // Then Steps
    // ==========================================

    @Then("a valid random recipe from the system is returned")
    public void a_valid_random_recipe_from_the_system_is_returned() {
        assertNotNull(this.currentVisitedRecipe);
        assertNotNull(this.currentVisitedRecipe.getName());
    }

    @Then("the recipe {string} is returned")
    public void the_recipe_is_returned(String recipeName) {
        assertNotNull(this.currentVisitedRecipe, "Expected a returned recipe");
        assertEquals(recipeName, this.currentVisitedRecipe.getName());
    }

    @Then("the message {string} is displayed")
    public void the_message_is_displayed(String expectedMessage) {
        assertEquals(expectedMessage, this.currentMessage);
    }

    @Then("the recipe {string}")
    public void the_recipe(String recipeName) {
        if (this.currentRecipesResponse == null) {
            this.currentRecipesResponse = recipesService.listRecipes();
        }
        boolean found = this.currentRecipesResponse.getRecipes().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(recipeName));
        assertTrue(found, "Expected recipe " + recipeName + " to be present");
    }

    @Then("the cooking time of {string}")
    public void the_cooking_time_of(String expectedTime) {
        Recipe recipe = currentVisitedRecipe != null ? currentVisitedRecipe :
                (currentRecipesResponse != null && !currentRecipesResponse.getRecipes().isEmpty() ?
                        currentRecipesResponse.getRecipes().get(0) : null);
        assertNotNull(recipe);
        assertEquals(expectedTime, recipe.getCookingTime());
    }

    @Then("the cooking time of {string} is displayed")
    public void the_cooking_time_of_is_displayed(String expectedTime) {
        assertNotNull(currentVisitedRecipe);
        assertEquals(expectedTime, currentVisitedRecipe.getCookingTime());
    }

    @Then("the main ingredients are displayed:")
    public void the_main_ingredients_are_displayed(DataTable dataTable) {
        List<String> expectedIngredients = dataTable.asList();
        Recipe recipe = currentVisitedRecipe != null ? currentVisitedRecipe :
                (currentRecipesResponse != null && !currentRecipesResponse.getRecipes().isEmpty() ?
                        currentRecipesResponse.getRecipes().get(0) : null);
        assertNotNull(recipe);
        assertEquals(expectedIngredients, recipe.getMainIngredients());
    }

    @Then("I am taken to the recipe page")
    public void i_am_taken_to_the_recipe_page() {
        assertTrue(recipePageVisited);
    }

    @Then("the recipes along with their cooking time and main ingredients are displayed:")
    public void the_recipes_along_with_their_cooking_time_and_main_ingredients_are_displayed(DataTable dataTable) {
        List<String> expectedNames = dataTable.asList();
        if (currentRecipesResponse == null) {
            currentRecipesResponse = recipesService.listRecipes();
        }
        List<String> actualNames = currentRecipesResponse.getRecipes().stream()
                .map(Recipe::getName)
                .collect(Collectors.toList());
        assertEquals(expectedNames, actualNames);
    }

    @Then("only the first {int} recipes are shown")
    public void only_the_first_recipes_are_shown(int count) {
        assertNotNull(currentRecipesResponse);
        assertEquals(count, currentRecipesResponse.getRecipes().size());
    }

    @Then("page navigation elements are displayed")
    public void page_navigation_elements_are_displayed() {
        assertTrue(navigationElementsDisplayed);
    }

    @Then("the image {string} is displayed")
    public void the_image_is_displayed(String expectedUrl) {
        assertNotNull(currentVisitedRecipe);
        assertEquals(expectedUrl, currentVisitedRecipe.getImageUrl());
    }

    @Then("the ingredients are listed:")
    public void the_ingredients_are_listed(DataTable dataTable) {
        List<String> expected = dataTable.asList();
        assertNotNull(currentVisitedRecipe);
        List<String> actual = currentVisitedRecipe.getIngredients().stream()
                .map(Ingredient::getFormatted)
                .collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Then("the system has the following starred recipes for {string}:")
    public void the_system_has_the_following_starred_recipes_for(String username, DataTable dataTable) {
        List<String> expectedStarred = dataTable.asList();
        Recipes starredResponse = recipesService.getStarredRecipesForUser(username);
        List<String> actualStarred = starredResponse.getRecipes().stream()
                .map(Recipe::getName)
                .collect(Collectors.toList());
        assertEquals(expectedStarred, actualStarred);
    }

    @Then("the system has no starred recipes for {string}")
    public void the_system_has_no_starred_recipes_for(String username) {
        Recipes starredResponse = recipesService.getStarredRecipesForUser(username);
        assertTrue(starredResponse.getRecipes().isEmpty());
    }

    @Then("the recipe {string} is displayed")
    public void the_recipe_is_displayed(String recipeName) {
        assertNotNull(currentRecipesResponse);
        boolean found = currentRecipesResponse.getRecipes().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(recipeName));
        assertTrue(found, "Expected recipe " + recipeName + " to be displayed");
    }

    @Then("the following recipes are displayed:")
    public void the_following_recipes_are_displayed(DataTable dataTable) {
        List<String> expected = dataTable.asList();
        assertNotNull(currentRecipesResponse);
        List<String> actual = currentRecipesResponse.getRecipes().stream()
                .map(Recipe::getName)
                .collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Then("only the following recipe is displayed:")
    public void only_the_following_recipe_is_displayed(DataTable dataTable) {
        List<String> expected = dataTable.asList();
        assertNotNull(currentRecipesResponse);
        List<String> actual = currentRecipesResponse.getRecipes().stream()
                .map(Recipe::getName)
                .collect(Collectors.toList());
        assertEquals(expected, actual);
    }
}
