package co.uk.savourly.recipes.controller;

import co.uk.savourly.recipes.model.Recipe;
import co.uk.savourly.recipes.model.Recipes;
import co.uk.savourly.recipes.service.RecipesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Recipe management actions.
 *
 * @author Aravinthan Narasimhan
 */
@RestController
@RequestMapping("/recipe")
public class RecipesController {

    private static final Logger LOG = LoggerFactory.getLogger(RecipesController.class);
    private static final int ITEMS_PER_PAGE = 10;

    @Autowired
    @Qualifier("defaultRecipesService")
    private RecipesService recipesService;

    /**
     * List recipes with optional pagination, search filtering by term, or cooking time filtering.
     *
     * @param pageNo     optional page number (1-based)
     * @param term       optional search term to match recipe name or ingredients
     * @param maxMinutes optional maximum cooking time in minutes
     * @return Recipes list response
     */
    @GetMapping
    public Recipes list(
            @RequestParam(value = "pageNo", required = false) String pageNo,
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "maxMinutes", required = false) Integer maxMinutes) {

        LOG.debug("Listing recipes - pageNo: {}, term: {}, maxMinutes: {}", pageNo, term, maxMinutes);

        if (term != null && !term.trim().isEmpty()) {
            return recipesService.filterRecipesByTerm(term);
        }

        if (maxMinutes != null && maxMinutes > 0) {
            return recipesService.filterRecipesByMaxCookingMinutes(maxMinutes);
        }

        if (pageNo != null && !pageNo.trim().isEmpty()) {
            try {
                int page = Integer.parseInt(pageNo.trim());
                return recipesService.listRecipes(page, ITEMS_PER_PAGE);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid pageNo parameter: {}", pageNo);
            }
        }

        return recipesService.listRecipes();
    }

    /**
     * Get a specific recipe by name.
     *
     * @param name Recipe name
     * @return Recipe object or 404 if not found
     */
    @GetMapping("/detail")
    public ResponseEntity<Recipe> getByName(@RequestParam("name") String name) {
        LOG.debug("Fetching recipe by name: {}", name);
        Recipe recipe = recipesService.getRecipeByName(name);
        if (recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(recipe);
    }

    /**
     * Get a random recipe.
     *
     * @param excludeId Optional recipe ID to exclude from random selection
     * @return Random Recipe object or 404 if no recipes exist
     */
    @GetMapping("/random")
    public ResponseEntity<Recipe> getRandom(@RequestParam(name = "excludeId", required = false) String excludeId) {
        LOG.debug("Fetching random recipe excluding id: {}", excludeId);
        Recipe recipe = recipesService.getRandomRecipe(excludeId);
        if (recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(recipe);
    }

    /**
     * Filter recipes by search term (name or ingredient).
     */
    @GetMapping("/search")
    public Recipes search(@RequestParam("term") String term) {
        LOG.debug("Searching recipes with term: {}", term);
        return recipesService.filterRecipesByTerm(term);
    }

    /**
     * Filter recipes by maximum cooking time in minutes.
     */
    @GetMapping("/filter")
    public Recipes filterByCookingTime(@RequestParam("maxMinutes") int maxMinutes) {
        LOG.debug("Filtering recipes with maxMinutes: {}", maxMinutes);
        return recipesService.filterRecipesByMaxCookingMinutes(maxMinutes);
    }

    /**
     * Star a recipe for a specified user.
     */
    @PostMapping("/star")
    public ResponseEntity<Map<String, String>> starRecipe(
            @RequestParam("username") String username,
            @RequestParam("recipeName") String recipeName) {
        LOG.debug("User {} starring recipe: {}", username, recipeName);
        recipesService.starRecipeForUser(username, recipeName);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Recipe starred successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Unstar a recipe for a specified user.
     */
    @PostMapping("/unstar")
    public ResponseEntity<Map<String, String>> unstarRecipe(
            @RequestParam("username") String username,
            @RequestParam("recipeName") String recipeName) {
        LOG.debug("User {} unstarring recipe: {}", username, recipeName);
        recipesService.unstarRecipeForUser(username, recipeName);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Recipe unstarred successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get all starred recipes for a specified user.
     */
    @GetMapping("/starred")
    public Recipes getStarredRecipes(@RequestParam("username") String username) {
        LOG.debug("Fetching starred recipes for user: {}", username);
        return recipesService.getStarredRecipesForUser(username);
    }

    /**
     * Create or save a new recipe.
     */
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        LOG.debug("Creating new recipe: {}", recipe != null ? recipe.getName() : null);
        if (recipe == null || recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        recipesService.saveRecipe(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }

    public void setRecipesService(RecipesService recipesService) {
        this.recipesService = recipesService;
    }
}
