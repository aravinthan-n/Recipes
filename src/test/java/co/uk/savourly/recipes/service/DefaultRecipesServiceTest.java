package co.uk.savourly.recipes.service;

import co.uk.savourly.recipes.builder.RecipeBuilder;
import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.model.User;
import co.uk.savourly.recipes.repository.RecipesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test Suite for DefaultRecipesService
 */
@ExtendWith(MockitoExtension.class)
public class DefaultRecipesServiceTest {

    @InjectMocks
    private DefaultRecipesService recipesService;

    @Mock
    private RecipesRepository mockRecipesRepository;

    @Test
    @DisplayName("Should return empty list when no recipes exist in system")
    public void testListRecipesWithNoRecipeInSystem() {
        when(mockRecipesRepository.findAll()).thenReturn(new Recipes());

        Recipes recipesOne = recipesService.listRecipes();
        assertNotNull(recipesOne);
        assertTrue(recipesOne.getRecipes().isEmpty());
        assertEquals(0, recipesOne.getTotalItems());

        Recipes recipesTwo = recipesService.listRecipes(1, 10);
        assertNotNull(recipesTwo);
        assertTrue(recipesTwo.getRecipes().isEmpty());
        assertEquals(0, recipesTwo.getTotalItems());

        Recipes recipesThree = recipesService.listRecipes(3, 11);
        assertNotNull(recipesThree);
        assertTrue(recipesThree.getRecipes().isEmpty());
        assertEquals(0, recipesThree.getTotalItems());
    }

    @Test
    @DisplayName("Should list single recipe and handle single page pagination")
    public void testListRecipesWithOneRecipeInSystem() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getOneRecipe());
        when(mockRecipesRepository.findAll()).thenReturn(recipes);

        Recipes recipesOne = recipesService.listRecipes();
        assertNotNull(recipesOne);
        assertEquals(1, recipesOne.getRecipes().size());
        assertEquals(1, recipesOne.getTotalItems());

        Recipes recipesTwo = recipesService.listRecipes(1, 1);
        assertNotNull(recipesTwo);
        assertEquals(1, recipesTwo.getRecipes().size());
        assertEquals(1, recipesTwo.getTotalItems());

        Recipes recipesThree = recipesService.listRecipes(2, 12);
        assertNotNull(recipesThree);
        assertEquals(0, recipesThree.getRecipes().size());
        assertEquals(1, recipesThree.getTotalItems());
    }

    @Test
    @DisplayName("Should paginate multiple recipes correctly across pages")
    public void testListRecipesWithMultipleRecipesInSystem() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getTwelveRecipes());
        when(mockRecipesRepository.findAll()).thenReturn(recipes);

        Recipes recipesOne = recipesService.listRecipes();
        assertNotNull(recipesOne);
        assertEquals(12, recipesOne.getRecipes().size());
        assertEquals(12, recipesOne.getTotalItems());

        Recipes recipesTwo = recipesService.listRecipes(1, 10);
        assertNotNull(recipesTwo);
        assertEquals(10, recipesTwo.getRecipes().size());
        assertEquals(12, recipesTwo.getTotalItems());

        Recipes recipesThree = recipesService.listRecipes(2, 10);
        assertNotNull(recipesThree);
        assertEquals(2, recipesThree.getRecipes().size());
        assertEquals(12, recipesThree.getTotalItems());

        Recipes recipesFour = recipesService.listRecipes(3, 10);
        assertNotNull(recipesFour);
        assertEquals(0, recipesFour.getRecipes().size());
        assertEquals(12, recipesFour.getTotalItems());

        Recipes recipesFive = recipesService.listRecipes(4, 3);
        assertNotNull(recipesFive);
        assertEquals(3, recipesFive.getRecipes().size());
        assertEquals(12, recipesFive.getTotalItems());
    }

    @Test
    @DisplayName("Should retrieve recipe by name when exists")
    public void testGetRecipeByNameFound() {
        Recipe lemonChicken = new Recipe("1", "Lemon Chicken", 30, "Lemon", "Chicken");
        when(mockRecipesRepository.findByName("Lemon Chicken")).thenReturn(lemonChicken);

        Recipe result = recipesService.getRecipeByName("Lemon Chicken");
        assertNotNull(result);
        assertEquals("Lemon Chicken", result.getName());
        assertEquals(30, result.getCookingMinutes());
    }

    @Test
    @DisplayName("Should return null when recipe is not found")
    public void testGetRecipeByNameNotFound() {
        when(mockRecipesRepository.findByName("Unknown")).thenReturn(null);

        Recipe result = recipesService.getRecipeByName("Unknown");
        assertNull(result);
    }

    @Test
    @DisplayName("Should return random recipe when recipes exist")
    public void testGetRandomRecipeWhenRecipesExist() {
        Recipe expectedRecipe = RecipeBuilder.getThreeRecipes().get(0);
        when(mockRecipesRepository.findRandom(null)).thenReturn(expectedRecipe);

        Recipe randomRecipe = recipesService.getRandomRecipe();
        assertNotNull(randomRecipe);
        assertEquals(expectedRecipe.getName(), randomRecipe.getName());
        verify(mockRecipesRepository, times(1)).findRandom(null);
    }

    @Test
    @DisplayName("Should return null when getting random recipe from empty system")
    public void testGetRandomRecipeWhenNoRecipesExist() {
        when(mockRecipesRepository.findRandom(null)).thenReturn(null);

        Recipe randomRecipe = recipesService.getRandomRecipe();
        assertNull(randomRecipe);
        verify(mockRecipesRepository, times(1)).findRandom(null);
    }

    @Test
    @DisplayName("Should pass excludeId to repository when getting random recipe")
    public void testGetRandomRecipeWithExcludeId() {
        Recipe expectedRecipe = RecipeBuilder.getThreeRecipes().get(1);
        when(mockRecipesRepository.findRandom("1")).thenReturn(expectedRecipe);

        Recipe randomRecipe = recipesService.getRandomRecipe("1");
        assertNotNull(randomRecipe);
        assertEquals(expectedRecipe.getName(), randomRecipe.getName());
        verify(mockRecipesRepository, times(1)).findRandom("1");
    }

    @Test
    @DisplayName("Should filter recipes by term matching recipe name")
    public void testFilterRecipesByTermMatchesName() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getThreeRecipes());
        when(mockRecipesRepository.findAll()).thenReturn(recipes);

        Recipes matched = recipesService.filterRecipesByTerm("Chicken");
        assertNotNull(matched);
        assertEquals(1, matched.getRecipes().size());
        assertEquals("Lemon Chicken", matched.getRecipes().get(0).getName());
    }

    @Test
    @DisplayName("Should filter recipes by term matching ingredient")
    public void testFilterRecipesByTermMatchesIngredient() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getThreeRecipes());
        when(mockRecipesRepository.findAll()).thenReturn(recipes);

        Recipes matched = recipesService.filterRecipesByTerm("Mustard");
        assertNotNull(matched);
        assertEquals(1, matched.getRecipes().size());
        assertEquals("Beef Stroganoff", matched.getRecipes().get(0).getName());
    }

    @Test
    @DisplayName("Should return all recipes when filter term is null or empty")
    public void testFilterRecipesByTermNullOrEmptyReturnsAll() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getThreeRecipes());
        when(mockRecipesRepository.findAll()).thenReturn(recipes);

        Recipes resultNull = recipesService.filterRecipesByTerm(null);
        assertEquals(3, resultNull.getRecipes().size());

        Recipes resultEmpty = recipesService.filterRecipesByTerm("   ");
        assertEquals(3, resultEmpty.getRecipes().size());
    }

    @Test
    @DisplayName("Should return empty list when filter term matches no recipes")
    public void testFilterRecipesByTermNoMatch() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getThreeRecipes());
        when(mockRecipesRepository.findAll()).thenReturn(recipes);

        Recipes result = recipesService.filterRecipesByTerm("Pineapple");
        assertNotNull(result);
        assertTrue(result.getRecipes().isEmpty());
        assertEquals(0, result.getTotalItems());
    }

    @Test
    @DisplayName("Should filter recipes by maximum cooking minutes")
    public void testFilterRecipesByMaxCookingMinutes() {
        Recipes recipes = new Recipes();
        recipes.setRecipes(RecipeBuilder.getThreeRecipes());
        when(mockRecipesRepository.findAll()).thenReturn(recipes);

        Recipes matched = recipesService.filterRecipesByMaxCookingMinutes(25);
        assertNotNull(matched);
        assertEquals(1, matched.getRecipes().size());
        assertEquals("Caesar Salad", matched.getRecipes().get(0).getName());
    }

    @Test
    @DisplayName("Should star recipe for user and save user state")
    public void testStarRecipeForUser() {
        User joe = new User("Joe");
        when(mockRecipesRepository.findUserByName("Joe")).thenReturn(joe);

        recipesService.starRecipeForUser("Joe", "Beef Stroganoff");

        assertTrue(joe.getStarredRecipeNames().contains("Beef Stroganoff"));
        verify(mockRecipesRepository, times(1)).saveUser(joe);
    }

    @Test
    @DisplayName("Should unstar recipe for existing user")
    public void testUnstarRecipeForUser() {
        User joe = new User("Joe");
        joe.starRecipe("Beef Stroganoff");
        when(mockRecipesRepository.findUserByName("Joe")).thenReturn(joe);

        recipesService.unstarRecipeForUser("Joe", "Beef Stroganoff");

        assertFalse(joe.getStarredRecipeNames().contains("Beef Stroganoff"));
        verify(mockRecipesRepository, times(1)).saveUser(joe);
    }

    @Test
    @DisplayName("Should retrieve starred recipes for user")
    public void testGetStarredRecipesForUser() {
        Recipe beefStroganoff = new Recipe("2", "Beef Stroganoff", 30, "Beef", "Mustard");
        when(mockRecipesRepository.findStarredRecipesForUser("Joe"))
                .thenReturn(Collections.singletonList(beefStroganoff));

        Recipes starred = recipesService.getStarredRecipesForUser("Joe");
        assertNotNull(starred);
        assertEquals(1, starred.getRecipes().size());
        assertEquals("Beef Stroganoff", starred.getRecipes().get(0).getName());
    }

    @Test
    @DisplayName("Should save single recipe to repository")
    public void testSaveRecipe() {
        Recipe recipe = new Recipe("10", "Dosa", 15, "Rice");
        recipesService.saveRecipe(recipe);

        verify(mockRecipesRepository, times(1)).save(recipe);
    }

    @Test
    @DisplayName("Should save multiple recipes to repository")
    public void testSaveRecipes() {
        List<Recipe> list = RecipeBuilder.getThreeRecipes();
        recipesService.saveRecipes(list);

        verify(mockRecipesRepository, times(1)).saveAll(list);
    }

    @Test
    @DisplayName("Should clear repository state and get user")
    public void testClearAndGetUser() {
        User user = new User("Alice");
        when(mockRecipesRepository.findUserByName("Alice")).thenReturn(user);

        recipesService.clear();
        verify(mockRecipesRepository, times(1)).clear();

        User result = recipesService.getUser("Alice");
        assertEquals("Alice", result.getName());
    }
}
