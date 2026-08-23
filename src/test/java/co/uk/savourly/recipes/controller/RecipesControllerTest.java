package co.uk.savourly.recipes.controller;

import co.uk.savourly.recipes.builder.RecipeBuilder;
import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.service.RecipesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipesController
 */
@ExtendWith(MockitoExtension.class)
public class RecipesControllerTest {

    @InjectMocks
    private RecipesController recipesController;

    @Mock
    private RecipesService recipesService;

    @Test
    @DisplayName("Should list all recipes when no query params are provided")
    public void testListDefault() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getThreeRecipes());
        when(recipesService.listRecipes()).thenReturn(recipes);

        Recipes result = recipesController.list(null, null, null);
        assertNotNull(result);
        assertEquals(3, result.getRecipes().size());
        verify(recipesService, times(1)).listRecipes();
    }

    @Test
    @DisplayName("Should list paginated recipes when pageNo parameter is provided")
    public void testListWithPageNo() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getOneRecipe());
        when(recipesService.listRecipes(1, 10)).thenReturn(recipes);

        Recipes result = recipesController.list("1", null, null);
        assertNotNull(result);
        assertEquals(1, result.getRecipes().size());
        verify(recipesService, times(1)).listRecipes(1, 10);
    }

    @Test
    @DisplayName("Should filter by term when term parameter is provided to list")
    public void testListWithTerm() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getOneRecipe());
        when(recipesService.filterRecipesByTerm("Chicken")).thenReturn(recipes);

        Recipes result = recipesController.list(null, "Chicken", null);
        assertNotNull(result);
        assertEquals(1, result.getRecipes().size());
        verify(recipesService, times(1)).filterRecipesByTerm("Chicken");
    }

    @Test
    @DisplayName("Should filter by maxMinutes when maxMinutes parameter is provided to list")
    public void testListWithMaxMinutes() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getOneRecipe());
        when(recipesService.filterRecipesByMaxCookingMinutes(30)).thenReturn(recipes);

        Recipes result = recipesController.list(null, null, 30);
        assertNotNull(result);
        assertEquals(1, result.getRecipes().size());
        verify(recipesService, times(1)).filterRecipesByMaxCookingMinutes(30);
    }

    @Test
    @DisplayName("Should return recipe detail when recipe name is found")
    public void testGetByNameFound() {
        Recipe recipe = new Recipe("1", "Lemon Chicken", 30);
        when(recipesService.getRecipeByName("Lemon Chicken")).thenReturn(recipe);

        ResponseEntity<Recipe> response = recipesController.getByName("Lemon Chicken");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Lemon Chicken", response.getBody().getName());
    }

    @Test
    @DisplayName("Should return 404 NOT_FOUND when recipe name does not exist")
    public void testGetByNameNotFound() {
        when(recipesService.getRecipeByName("Unknown")).thenReturn(null);

        ResponseEntity<Recipe> response = recipesController.getByName("Unknown");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should return random recipe endpoint response")
    public void testGetRandomSuccess() {
        Recipe recipe = new Recipe("1", "Lemon Chicken", 30);
        when(recipesService.getRandomRecipe(null)).thenReturn(recipe);

        ResponseEntity<Recipe> response = recipesController.getRandom(null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Lemon Chicken", response.getBody().getName());
    }

    @Test
    @DisplayName("Should return 404 NOT_FOUND when random recipe returns null")
    public void testGetRandomNotFound() {
        when(recipesService.getRandomRecipe(null)).thenReturn(null);

        ResponseEntity<Recipe> response = recipesController.getRandom(null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should pass excludeId to service in getRandom endpoint")
    public void testGetRandomWithExcludeId() {
        Recipe recipe = new Recipe("2", "Beef Stroganoff", 30);
        when(recipesService.getRandomRecipe("1")).thenReturn(recipe);

        ResponseEntity<Recipe> response = recipesController.getRandom("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Beef Stroganoff", response.getBody().getName());
        verify(recipesService, times(1)).getRandomRecipe("1");
    }

    @Test
    @DisplayName("Should search recipes by term")
    public void testSearchEndpoint() {
        Recipes recipes = new Recipes();
        when(recipesService.filterRecipesByTerm("Lemon")).thenReturn(recipes);

        Recipes result = recipesController.search("Lemon");
        assertNotNull(result);
        verify(recipesService, times(1)).filterRecipesByTerm("Lemon");
    }

    @Test
    @DisplayName("Should filter recipes by cooking time endpoint")
    public void testFilterEndpoint() {
        Recipes recipes = new Recipes();
        when(recipesService.filterRecipesByMaxCookingMinutes(25)).thenReturn(recipes);

        Recipes result = recipesController.filterByCookingTime(25);
        assertNotNull(result);
        verify(recipesService, times(1)).filterRecipesByMaxCookingMinutes(25);
    }

    @Test
    @DisplayName("Should star recipe for user")
    public void testStarRecipe() {
        ResponseEntity<Map<String, String>> response = recipesController.starRecipe("Joe", "Lemon Chicken");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        verify(recipesService, times(1)).starRecipeForUser("Joe", "Lemon Chicken");
    }

    @Test
    @DisplayName("Should unstar recipe for user")
    public void testUnstarRecipe() {
        ResponseEntity<Map<String, String>> response = recipesController.unstarRecipe("Joe", "Lemon Chicken");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        verify(recipesService, times(1)).unstarRecipeForUser("Joe", "Lemon Chicken");
    }

    @Test
    @DisplayName("Should return starred recipes for user")
    public void testGetStarredRecipes() {
        Recipes recipes = new Recipes();
        when(recipesService.getStarredRecipesForUser("Joe")).thenReturn(recipes);

        Recipes result = recipesController.getStarredRecipes("Joe");
        assertNotNull(result);
        verify(recipesService, times(1)).getStarredRecipesForUser("Joe");
    }

    @Test
    @DisplayName("Should create recipe when valid payload provided")
    public void testCreateRecipeValid() {
        Recipe recipe = new Recipe("5", "Tacos", 20);
        ResponseEntity<Recipe> response = recipesController.createRecipe(recipe);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tacos", response.getBody().getName());
        verify(recipesService, times(1)).saveRecipe(recipe);
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST when create recipe payload is invalid")
    public void testCreateRecipeInvalid() {
        ResponseEntity<Recipe> responseNull = recipesController.createRecipe(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseNull.getStatusCode());

        Recipe emptyName = new Recipe();
        ResponseEntity<Recipe> responseEmpty = recipesController.createRecipe(emptyName);
        assertEquals(HttpStatus.BAD_REQUEST, responseEmpty.getStatusCode());
    }
}
